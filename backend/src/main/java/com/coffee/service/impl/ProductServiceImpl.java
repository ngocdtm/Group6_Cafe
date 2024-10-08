
package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.Category;
import com.coffee.entity.Product;
import com.coffee.repository.ProductRepository;
import com.coffee.security.JwtRequestFilter;
import com.coffee.service.FileStorageService;
import com.coffee.service.ProductService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Override
    public ResponseEntity<String> addProduct(Map<String, String> requestMap) {
        try{
            if(jwtRequestFilter.isAdmin()){
                if(validateProductMap(requestMap, false)){
                    Product product = productRepository.findByNameProduct(requestMap.get(CafeConstants.NAMEPRODUCT));
                    if (Objects.isNull(product)) {
                        productRepository.save(getProductFromMap(requestMap, false));
                        return CafeUtils.getResponseEntity("Product Added successfully", HttpStatus.OK);
                    }
                    else {
                        return CafeUtils.getResponseEntity(CafeConstants.NAMEPRODUCT_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
                    }
                }
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        return requestMap.containsKey("name") && (requestMap.containsKey("id") || !validateId);
    }

    private Product getProductFromMap(Map<String, String> requestMap, boolean isAdd) {
        Category category = new Category();
        category.setId(Integer.valueOf(requestMap.get("categoryId")));

        Product product = new Product();
        if(isAdd) {
            product.setId(Integer.valueOf(requestMap.get("id")));
        }else{
            product.setStatus(("true"));
        }
        product.setCategory(category);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Integer.valueOf(requestMap.get("price")));
        product.setImagePath(requestMap.get("image_path"));
        return product;
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProduct() {
        try{
            return new ResponseEntity<>(productRepository.getAllProduct(), HttpStatus.OK);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        try{
            if(jwtRequestFilter.isAdmin()){
                if(validateProductMap(requestMap, true)){
                    Product productName = productRepository.findByNameProduct(requestMap.get(CafeConstants.NAMEPRODUCT));
                    Optional<Product> optional = productRepository.findById(Integer.valueOf(requestMap.get("id")));
                    if(optional.isPresent()){
                        Product product = getProductFromMap(requestMap, true);
                        product.setStatus(optional.get().getStatus());
                        if (Objects.isNull(productName)) {
                            productRepository.save(product);
                            return CafeUtils.getResponseEntity("Product updated successfully", HttpStatus.OK);
                        }
                        else {
                            return CafeUtils.getResponseEntity(CafeConstants.NAMEPRODUCT_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
                        }
                    }else {
                        return CafeUtils.getResponseEntity("product id does not exist", HttpStatus.OK);
                    }
                }else{
                    return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {
        try{
            if(jwtRequestFilter.isAdmin()){
                Optional<Product> optional = productRepository.findById(id);
                if(optional.isPresent()){
                    productRepository.deleteById(id);
                    return CafeUtils.getResponseEntity("Product deleted successfully", HttpStatus.OK);
                }else{
                    return CafeUtils.getResponseEntity("Product id does not exist", HttpStatus.OK);
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        try{
            if(jwtRequestFilter.isAdmin()){
                Optional<Product> optional = productRepository.findById(Integer.valueOf(requestMap.get("id")));
                if(optional.isPresent()){
                    productRepository.updateProductStatus(requestMap.get("status"), Integer.valueOf(requestMap.get("id")));
                    return CafeUtils.getResponseEntity("Product status updated successfully", HttpStatus.OK);
                }else {
                    return CafeUtils.getResponseEntity("Product id does not exist", HttpStatus.OK);
                }
            }else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        try{
            return new ResponseEntity<>(productRepository.getProductByCategory(id), HttpStatus.OK);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProductWrapper> getById(Integer id) {
        try{
            return new ResponseEntity<>(productRepository.getProductById(id), HttpStatus.OK);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteImage(Integer id) {
        try {
            if(jwtRequestFilter.isAdmin()) {
                Optional<Product> optional = productRepository.findById(id);
                if(optional.isPresent()) {
                    Product product = optional.get();

                    if(product.getImagePath() != null) {
                        fileStorageService.deleteFile(product.getImagePath());
                        product.setImagePath(null);
                        productRepository.save(product);
                    }

                    return CafeUtils.getResponseEntity(
                            "Image deleted successfully",
                            HttpStatus.OK
                    );
                }
                return CafeUtils.getResponseEntity(
                        "Product id does not exist",
                        HttpStatus.NOT_FOUND
                );
            }
            return CafeUtils.getResponseEntity(
                    CafeConstants.UNAUTHORIZED_ACCESS,
                    HttpStatus.UNAUTHORIZED
            );
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(
                CafeConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @Override
    public ResponseEntity<String> uploadImage(Integer id, MultipartFile file) {
        try {
            if(jwtRequestFilter.isAdmin()) {
                Optional<Product> optional = productRepository.findById(id);
                if(optional.isPresent()) {
                    Product product = optional.get();

                    // Delete old image if exists
                    if(product.getImagePath() != null) {
                        fileStorageService.deleteFile(product.getImagePath());
                    }

                    // Store new image
                    String fileName = fileStorageService.storeFile(file, id.toString());
                    product.setImagePath(fileName);
                    productRepository.save(product);

                    return CafeUtils.getResponseEntity(
                            "Image uploaded successfully",
                            HttpStatus.OK
                    );
                }
                return CafeUtils.getResponseEntity(
                        "Product id does not exist",
                        HttpStatus.NOT_FOUND
                );
            }
            return CafeUtils.getResponseEntity(
                    CafeConstants.UNAUTHORIZED_ACCESS,
                    HttpStatus.UNAUTHORIZED
            );
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(
                CafeConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

}
