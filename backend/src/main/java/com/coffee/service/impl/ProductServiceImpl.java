package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.Category;
import com.coffee.entity.Product;
import com.coffee.entity.ProductImage;
import com.coffee.repository.ProductImageRepository;
import com.coffee.repository.ProductRepository;
import com.coffee.security.JwtRequestFilter;
import com.coffee.service.ProductService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.ProductImageWrapper;
import com.coffee.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductImageRepository productImageRepository;

    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Value("${app.file.upload-dir}")
    private String uploadPath;

    @Override
    public ResponseEntity<String> addProduct(List<MultipartFile> files, String name, Integer categoryId, String description, Integer price, Integer originalPrice) {
        try {
            if(jwtRequestFilter.isAdmin()){
                if(files != null && !files.isEmpty() && name != null && categoryId != null && description != null && price != null && originalPrice != null) {
                    // Check if product name already exists
                    if(productRepository.findByNameProduct(name).isPresent()) {
                        return CafeUtils.getResponseEntity("Product name already exists", HttpStatus.BAD_REQUEST);
                    }

                    Category category = new Category();
                    category.setId(categoryId);

                    Product product = new Product();
                    product.setName(name);
                    product.setCategory(category);
                    product.setDescription(description);
                    product.setPrice(price);
                    product.setOriginalPrice(originalPrice);
                    product.setStatus("true");

                    product = productRepository.save(product);

                    for(MultipartFile file : files) {
                        String fileName = saveImage(file);
                        ProductImage productImage = new ProductImage();
                        productImage.setImagePath(fileName);
                        productImage.setProduct(product);
                        productImageRepository.save(productImage);
                    }

                    return CafeUtils.getResponseEntity("Product Added successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProduct() {
        try {
            List<ProductWrapper> productWrappers = productRepository.getAllProduct();

            // Fetch and set images for each product
            for (ProductWrapper wrapper : productWrappers) {
                Optional<Product> product = productRepository.findById(wrapper.getId());
                if (product.isPresent()) {
                    List<ProductImageWrapper> imageWrappers = product.get().getImages().stream()
                            .map(image -> new ProductImageWrapper(image.getId(), image.getImagePath()))
                            .collect(Collectors.toList());
                    wrapper.setImages(imageWrappers);
                }
            }

            return new ResponseEntity<>(productWrappers, HttpStatus.OK);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProduct(List<MultipartFile> files, Integer id, String name,
                                                Integer categoryId, String description, Integer price, Integer originalPrice, List<Integer> deletedImageIds) {
        try {
            if(jwtRequestFilter.isAdmin()) {
                Optional<Product> optional = productRepository.findById(id);
                if(optional.isPresent()) {
                    Product product = optional.get();

                    // Check if the new name already exists for a different product
                    if(name != null && !name.equals(product.getName())) {
                        Optional<Product> existingProduct = productRepository.findByNameProduct(name);
                        if(existingProduct.isPresent() && !existingProduct.get().getId().equals(id)) {
                            return CafeUtils.getResponseEntity("Product name already exists", HttpStatus.BAD_REQUEST);
                        }
                        product.setName(name);
                    }

                    if(categoryId != null) {
                        Category category = new Category();
                        category.setId(categoryId);
                        product.setCategory(category);
                    }
                    if(description != null) {
                        product.setDescription(description);
                    }
                    if(price != null) {
                        product.setPrice(price);
                    }
                    if (originalPrice != null) {
                        product.setOriginalPrice(originalPrice);
                    }

                    // Handle image deletions first
                    if(deletedImageIds != null && !deletedImageIds.isEmpty()) {
                        for(Integer imageId : deletedImageIds) {
                            Optional<ProductImage> optionalImage = productImageRepository.findById(imageId);
                            if(optionalImage.isPresent() && optionalImage.get().getProduct().getId().equals(id)) {
                                ProductImage image = optionalImage.get();
                                // Delete physical file
                                deleteImage(image.getImagePath());
                                // Remove from database
                                productImageRepository.delete(image);
                            }
                        }
                    }

                    // Handle new image uploads
                    if(files != null && !files.isEmpty()) {
                        for(MultipartFile file : files) {
                            String fileName = saveImage(file);
                            ProductImage productImage = new ProductImage();
                            productImage.setImagePath(fileName);
                            productImage.setProduct(product);
                            productImageRepository.save(productImage);
                        }
                    }

                    productRepository.save(product);
                    return CafeUtils.getResponseEntity("Product updated successfully", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("Product id does not exist", HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<String> deleteProductImage(Integer productId, Integer imageId) {
        try {
            if(jwtRequestFilter.isAdmin()){
                Optional<Product> optionalProduct = productRepository.findById(productId);
                if(optionalProduct.isPresent()){
                    Product product = optionalProduct.get();
                    Optional<ProductImage> optionalImage = productImageRepository.findById(imageId);
                    if(optionalImage.isPresent() && optionalImage.get().getProduct().getId().equals(productId)){
                        ProductImage image = optionalImage.get();
                        deleteImage(image.getImagePath());
                        productImageRepository.delete(image);
                        return CafeUtils.getResponseEntity("Product image deleted successfully", HttpStatus.OK);
                    } else {
                        return CafeUtils.getResponseEntity("Image id does not exist or does not belong to the product", HttpStatus.OK);
                    }
                } else {
                    return CafeUtils.getResponseEntity("Product id does not exist", HttpStatus.OK);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String saveImage(MultipartFile file) throws Exception {
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        Path uploadPath = Paths.get(this.uploadPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    private void deleteImage(String fileName) {
        if(fileName != null) {
            try {
                Path filePath = Paths.get(uploadPath).resolve(fileName);
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    public ResponseEntity<ProductWrapper> getById(Integer id) {
        try {
            Optional<Product> optionalProduct = productRepository.findById(id);
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                ProductWrapper wrapper = productRepository.getProductById(id);

                // Set image paths
                List<ProductImageWrapper> imageWrappers = product.getImages().stream()
                        .map(image -> new ProductImageWrapper(image.getId(), image.getImagePath()))
                        .collect(Collectors.toList());
                wrapper.setImages(imageWrappers);

                return new ResponseEntity<>(wrapper, HttpStatus.OK);
            }
            return new ResponseEntity<>(new ProductWrapper(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        try {
            List<ProductWrapper> wrappers = productRepository.getProductByCategory(id);

            // Set image paths for each product
            for (ProductWrapper wrapper : wrappers) {
                Optional<Product> optionalProduct = productRepository.findById(wrapper.getId());
                if (optionalProduct.isPresent()) {
                    Product product = optionalProduct.get();
                    List<String> imagePaths = product.getImages().stream()
                            .map(ProductImage::getImagePath)
                            .collect(Collectors.toList());
                    wrapper.setImagePaths(imagePaths);
                }
            }

            return new ResponseEntity<>(wrappers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}