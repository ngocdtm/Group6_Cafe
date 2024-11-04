package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.*;
import com.coffee.repository.*;
import com.coffee.security.JwtRequestFilter;
import com.coffee.service.ProductService;
import com.coffee.service.UserService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.ProductHistoryWrapper;
import com.coffee.wrapper.ProductImageWrapper;
import com.coffee.wrapper.ProductWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Autowired
    RecentlyViewedRepository recentlyViewedRepository;

    @Autowired
    private UserService userService;

    @Autowired
    UserRepository userRepository;

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

                    // Save initial product creation history
                    saveProductHistory(product, "CREATE", null, convertToJson(product));

                    List<String> savedImagePaths = new ArrayList<>();
                    for(MultipartFile file : files) {
                        String fileName = saveImage(file);
                        ProductImage productImage = new ProductImage();
                        productImage.setImagePath(fileName);
                        productImage.setProduct(product);
                        productImageRepository.save(productImage);
                        savedImagePaths.add(fileName);
                    }

                    // Save image addition history
                    if (!savedImagePaths.isEmpty()) {
                        Map<String, Object> imageData = new HashMap<>();
                        imageData.put("action", "ADD_IMAGES");
                        imageData.put("images", savedImagePaths);
                        saveProductHistory(product, "ADD_IMAGES", null, new ObjectMapper().writeValueAsString(imageData));
                    }

                    return CafeUtils.getResponseEntity("Product Added successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch(Exception ex) {
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
                                                Integer categoryId, String description, Integer price, Integer originalPrice,
                                                List<Integer> deletedImageIds) {
        try {
            if(jwtRequestFilter.isAdmin()) {
                Optional<Product> optional = productRepository.findById(id);
                if(optional.isPresent()) {
                    Product product = optional.get();
                    String previousState = convertToJson(product);

                    boolean productChanged = false;
                    Map<String, Object> changes = new HashMap<>();

                    if(name != null && !name.equals(product.getName())) {
                        Optional<Product> existingProduct = productRepository.findByNameProduct(name);
                        if(existingProduct.isPresent() && !existingProduct.get().getId().equals(id)) {
                            return CafeUtils.getResponseEntity("Product name already exists", HttpStatus.BAD_REQUEST);
                        }
                        changes.put("name", new String[]{product.getName(), name});
                        product.setName(name);
                        productChanged = true;
                    }

                    if(categoryId != null && !categoryId.equals(product.getCategory().getId())) {
                        changes.put("categoryId", new Integer[]{product.getCategory().getId(), categoryId});
                        Category category = new Category();
                        category.setId(categoryId);
                        product.setCategory(category);
                        productChanged = true;
                    }

                    if(description != null && !description.equals(product.getDescription())) {
                        changes.put("description", new String[]{product.getDescription(), description});
                        product.setDescription(description);
                        productChanged = true;
                    }

                    if(price != null && !price.equals(product.getPrice())) {
                        changes.put("price", new Integer[]{product.getPrice(), price});
                        product.setPrice(price);
                        productChanged = true;
                    }

                    if(originalPrice != null && !originalPrice.equals(product.getOriginalPrice())) {
                        changes.put("originalPrice", new Integer[]{product.getOriginalPrice(), originalPrice});
                        product.setOriginalPrice(originalPrice);
                        productChanged = true;
                    }

                    // Track basic product changes
                    if (productChanged) {
                        saveProductHistory(product, "UPDATE",
                                new ObjectMapper().writeValueAsString(changes),
                                convertToJson(product));
                    }

                    // Track image deletions
                    if(deletedImageIds != null && !deletedImageIds.isEmpty()) {
                        List<String> deletedImages = new ArrayList<>();
                        for(Integer imageId : deletedImageIds) {
                            Optional<ProductImage> optionalImage = productImageRepository.findById(imageId);
                            if(optionalImage.isPresent() && optionalImage.get().getProduct().getId().equals(id)) {
                                ProductImage image = optionalImage.get();
                                deletedImages.add(image.getImagePath());
                                image.setDeleted("true");
                                image.setDeletedDate(LocalDateTime.now());
                                productImageRepository.save(image);
                            }
                        }

                        if (!deletedImages.isEmpty()) {
                            Map<String, Object> imageData = new HashMap<>();
                            imageData.put("action", "DELETE_IMAGES");
                            imageData.put("images", deletedImages);
                            saveProductHistory(product, "DELETE_IMAGES",
                                    new ObjectMapper().writeValueAsString(imageData), null);
                        }
                    }

                    // Track new image additions
                    if(files != null && !files.isEmpty()) {
                        List<String> newImages = new ArrayList<>();
                        for(MultipartFile file : files) {
                            String fileName = saveImage(file);
                            ProductImage productImage = new ProductImage();
                            productImage.setImagePath(fileName);
                            productImage.setProduct(product);
                            productImageRepository.save(productImage);
                            newImages.add(fileName);
                        }

                        if (!newImages.isEmpty()) {
                            Map<String, Object> imageData = new HashMap<>();
                            imageData.put("action", "ADD_IMAGES");
                            imageData.put("images", newImages);
                            saveProductHistory(product, "ADD_IMAGES", null,
                                    new ObjectMapper().writeValueAsString(imageData));
                        }
                    }

                    productRepository.save(product);
                    return CafeUtils.getResponseEntity("Product updated successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Product id does not exist", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch(Exception ex) {
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

    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {
        try {
            if(jwtRequestFilter.isAdmin()) {
                Optional<Product> optional = productRepository.findById(id);
                if(optional.isPresent()) {
                    Product product = optional.get();

                    // Capture the previous state before making any changes
//                    String previousState = convertToJson(product);

                    // Soft delete the product
                    product.setDeleted("true");
                    product.setDeletedDate(LocalDateTime.now());
                    product.setRestoredDate(null);

                    // Save the product first to ensure it's updated in the database
                    product = productRepository.save(product);

                    // Soft delete all associated images
                    List<ProductImage> images = productImageRepository.findActiveImagesByProductId(id);
                    for(ProductImage image : images) {
                        image.setDeleted("true");
                        image.setDeletedDate(LocalDateTime.now());
                        image.setRestoredDate(null);
                        productImageRepository.save(image);
                    }

                    // Create a map for deletion details
//                    Map<String, Object> deletionDetails = new HashMap<>();
//                    deletionDetails.put("deletionDate", product.getDeletedDate());
//                    deletionDetails.put("affectedImages", images.size());

                    // Save deletion history with structured data
                    String newState = convertToJson(product);
                    saveProductHistory(product, "DELETE",
                            null,
                            null);

                    return CafeUtils.getResponseEntity("Product deleted successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Product id does not exist", HttpStatus.NOT_FOUND);
            }
            return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        try {
            if(jwtRequestFilter.isAdmin()) {
                Optional<Product> optional = productRepository.findById(Integer.valueOf(requestMap.get("id")));
                if(optional.isPresent()) {
                    Product product = optional.get();
                    String previousStatus = product.getStatus();
                    String newStatus = requestMap.get("status");

                    // Track status change
                    Map<String, Object> statusChange = new HashMap<>();
                    statusChange.put("previousStatus", previousStatus);
                    statusChange.put("newStatus", newStatus);

                    saveProductHistory(product, "STATUS_CHANGE",
                            new ObjectMapper().writeValueAsString(statusChange), null);

                    productRepository.updateProductStatus(newStatus, product.getId());
                    return CafeUtils.getResponseEntity("Product status updated successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Product id does not exist", HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch(Exception ex) {
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

    @Override
    public ResponseEntity<List<ProductWrapper>> searchProducts(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
            }

            List<ProductWrapper> productWrappers = productRepository.searchProducts(keyword.trim());

            // Fetch và set images cho mỗi product
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getRelatedProducts(Integer productId, Integer limit) {
        try{
            Product product = productRepository.findById(productId).orElse(null);

            if (product == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
            }

            // Lấy sản phẩm liên quan từ cùng category, có mức giá tương đương
            Integer priceRange = (int)(product.getPrice() * 0.3); // Range 30% của giá gốc

            List<ProductWrapper> relatedProducts = productRepository.getRelatedProducts(

                    product.getCategory().getId(),
                    productId,
                    product.getPrice(),
                    priceRange,
                    limit
            );
            // Thêm phần fetch và set images cho mỗi sản phẩm liên quan
            for (ProductWrapper wrapper : relatedProducts) {
                Optional<Product> relatedProduct = productRepository.findById(wrapper.getId());
                if (relatedProduct.isPresent()) {
                    List<ProductImageWrapper> imageWrappers = relatedProduct.get().getImages().stream()
                            .map(image -> new ProductImageWrapper(image.getId(), image.getImagePath()))
                            .collect(Collectors.toList());
                    wrapper.setImages(imageWrappers);
                }
            }
            return new ResponseEntity<>(relatedProducts, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addToRecentlyViewed(Integer productId) {
        try {
            String username = jwtRequestFilter.getCurrentUser();
            if (username == null) {
                return CafeUtils.getResponseEntity("User not authenticated", HttpStatus.UNAUTHORIZED);
            }

            // Lấy User object từ username
            User user = userRepository.findByEmail(username);

            Optional<Product> optionalProduct = productRepository.findById(productId);
            if (!optionalProduct.isPresent()) {
                return CafeUtils.getResponseEntity("Product not found", HttpStatus.NOT_FOUND);
            }

            Product product = optionalProduct.get();

            // Check if product is already in recently viewed
            Optional<RecentlyViewedProduct> existing =
                    recentlyViewedRepository.findByUserAndProduct(user, product);

            if (existing.isPresent()) {
                // Update viewed time
                RecentlyViewedProduct recentlyViewed = existing.get();
                recentlyViewed.setViewedAt(LocalDateTime.now());
                recentlyViewedRepository.save(recentlyViewed);
            } else {
                // Check if user has reached the limit of 10 products
                long count = recentlyViewedRepository.countByUser(user);
                if (count >= 10) {
                    // Get all products ordered by viewed time and remove the oldest one
                    List<RecentlyViewedProduct> products =
                            recentlyViewedRepository.findByUserOrderByViewedAtDesc(user);
                    recentlyViewedRepository.delete(products.get(products.size() - 1));
                }

                // Add new recently viewed product
                RecentlyViewedProduct recentlyViewed = new RecentlyViewedProduct();
                recentlyViewed.setUser(user);
                recentlyViewed.setProduct(product);
                recentlyViewed.setViewedAt(LocalDateTime.now());
                recentlyViewedRepository.save(recentlyViewed);
            }

            return CafeUtils.getResponseEntity("Product added to recently viewed", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getRecentlyViewedProducts() {
        try {
            String username = jwtRequestFilter.getCurrentUser();
            if (username == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            // Lấy User object từ username
            User user = userRepository.findByEmail(username);
            if (user == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            List<RecentlyViewedProduct> recentlyViewed =
                    recentlyViewedRepository.findByUserOrderByViewedAtDesc(user);

            List<ProductWrapper> productWrappers = new ArrayList<>();

            for (RecentlyViewedProduct rv : recentlyViewed) {
                Product product = rv.getProduct();

                // Create new ProductWrapper with only necessary fields
                ProductWrapper wrapper = new ProductWrapper();
                wrapper.setId(product.getId());
                wrapper.setName(product.getName());
                wrapper.setDescription(product.getDescription());
                wrapper.setPrice(product.getPrice());
                wrapper.setStatus(product.getStatus());

                // Set category info
                if (product.getCategory() != null) {
                    wrapper.setCategoryId(product.getCategory().getId());
                    wrapper.setCategoryName(product.getCategory().getName());
                }

                // Set images
                List<ProductImageWrapper> imageWrappers = new ArrayList<>();
                if (product.getImages() != null) {
                    for (ProductImage image : product.getImages()) {
                        imageWrappers.add(new ProductImageWrapper(image.getId(), image.getImagePath()));
                    }
                }
                wrapper.setImages(imageWrappers);

                productWrappers.add(wrapper);
            }

            return new ResponseEntity<>(productWrappers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> restoreProduct(Integer id) {
        try {
            if(jwtRequestFilter.isAdmin()) {
                Optional<Product> optional = productRepository.findById(id);
                if(optional.isPresent()) {
                    Product product = optional.get();
                    if("true".equals(product.getDeleted())) {
                        // Restore product
                        product.setDeleted("false");
                        product.setRestoredDate(LocalDateTime.now());

                        // Restore all associated deleted images
                        List<ProductImage> deletedImages = productImageRepository.findDeletedImagesByProductId(id);
                        for(ProductImage image : deletedImages) {
                            image.setDeleted("false");
                            image.setRestoredDate(LocalDateTime.now());
                            productImageRepository.save(image);
                        }

                        // Save product restoration history
                        saveProductHistory(product, "RESTORE", null, convertToJson(product));

                        productRepository.save(product);
                        return CafeUtils.getResponseEntity("Product restored successfully", HttpStatus.OK);
                    }
                    return CafeUtils.getResponseEntity("Product is not deleted", HttpStatus.BAD_REQUEST);
                }
                return CafeUtils.getResponseEntity("Product id does not exist", HttpStatus.NOT_FOUND);
            }
            return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> restoreImage(Integer imageId) {
        try {
            Optional<ProductImage> optional = productImageRepository.findById(imageId);
            if(optional.isPresent()) {
                ProductImage image = optional.get();
                if ("true".equals(image.getDeleted())) {
                    image.setDeleted("false");
                    image.setDeletedDate(null);
                    image.setRestoredDate(LocalDateTime.now());
                    productImageRepository.save(image);
                    return CafeUtils.getResponseEntity("Image restored successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Image is not deleted", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity("Image id does not exist", HttpStatus.NOT_FOUND);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductHistoryWrapper>> getProductHistory(Integer id) {
        try {
            if(jwtRequestFilter.isAdmin()) {
                Optional<Product> optional = productRepository.findById(id);
                if(optional.isPresent()) {
                    List<ProductHistory> histories = productHistoryRepository.findByProductIdOrderByModifiedDateDesc(id);
                    List<ProductHistoryWrapper> wrappers = histories.stream()
                            .map(this::convertToWrapper)
                            .collect(Collectors.toList());
                    return new ResponseEntity<>(wrappers, HttpStatus.OK);
                }
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getActiveProducts() {
        try {
            List<ProductWrapper> products = productRepository.findActiveProducts();
            if(products != null && !products.isEmpty()) {
                // Set images for each product
                for(ProductWrapper product : products) {
                    List<ProductImageWrapper> images = productImageRepository.findActiveImagesByProductId(product.getId())
                            .stream()
                            .map(image -> new ProductImageWrapper(image.getId(), image.getImagePath()))
                            .collect(Collectors.toList());
                    product.setImages(images);
                }
                return new ResponseEntity<>(products, HttpStatus.OK);
            }
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        } catch(Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<ProductImageWrapper>> getActiveImages(Integer productId) {
        try {
            // Verify if product exists
            Optional<Product> productOptional = productRepository.findById(productId);
            if (!productOptional.isPresent()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            // Get active images for the product
            List<ProductImage> activeImages = productImageRepository.findActiveImagesByProductId(productId);

            // Convert to wrapper objects
            List<ProductImageWrapper> wrappers = activeImages.stream()
                    .map(image -> new ProductImageWrapper(
                            image.getId(),
                            image.getImagePath()
                    ))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(wrappers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<ProductImageWrapper>> getDeletedImages(Integer productId) {
        try {
            // Check admin access
            if (!jwtRequestFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            // Verify if product exists
            Optional<Product> productOptional = productRepository.findById(productId);
            if (!productOptional.isPresent()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            // Get deleted images for the product
            List<ProductImage> deletedImages = productImageRepository.findDeletedImagesByProductId(productId);

            // Convert to wrapper objects
            List<ProductImageWrapper> wrappers = deletedImages.stream()
                    .map(image -> new ProductImageWrapper(
                            image.getId(),
                            image.getImagePath()
                    ))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(wrappers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public ResponseEntity<List<ProductWrapper>> getDeletedProducts() {
        try {
            if(jwtRequestFilter.isAdmin()) {
                List<ProductWrapper> products = productRepository.findDeletedProducts();
                if(products != null && !products.isEmpty()) {
                    // Set images for each product
                    for(ProductWrapper product : products) {
                        List<ProductImageWrapper> images = productImageRepository.findAllImagesByProductId(product.getId())
                                .stream()
                                .map(image -> new ProductImageWrapper(image.getId(), image.getImagePath()))
                                .collect(Collectors.toList());
                        product.setImages(images);
                    }
                }
                return new ResponseEntity<>(products, HttpStatus.OK);
            }
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
        } catch(Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper methods for tracking history
    private void saveProductHistory(Product product, String action, String previousData, String newData) {
        ProductHistory history = new ProductHistory();
        history.setProduct(product);
        history.setModifiedDate(LocalDateTime.now());
        history.setModifiedBy(jwtRequestFilter.getCurrentUser());
        history.setAction(action);
        history.setPreviousData(previousData);
        history.setNewData(newData);
        productHistoryRepository.save(history);
    }

    private String convertToJson(Product product) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(product);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ProductHistoryWrapper convertToWrapper(ProductHistory history) {
        return ProductHistoryWrapper.builder()
                .id(history.getId())
                .modifiedDate(history.getModifiedDate())
                .modifiedBy(history.getModifiedBy())
                .action(history.getAction())
                .previousData(history.getPreviousData())
                .newData(history.getNewData())
                .build();
    }

}