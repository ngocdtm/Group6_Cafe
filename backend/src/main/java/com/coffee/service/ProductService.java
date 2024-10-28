package com.coffee.service;

import com.coffee.wrapper.ProductWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface ProductService {
    ResponseEntity<String> addProduct(List<MultipartFile> files, String name, Integer categoryId, String description, Integer price, Integer originalPrice);

    ResponseEntity<List<ProductWrapper>> getAllProduct();

    ResponseEntity<String> updateProduct(List<MultipartFile> files, Integer id, String name,
                                         Integer categoryId, String description, Integer price, Integer originalPrice, List<Integer> deletedImageIds);
    ResponseEntity<String> deleteProduct(Integer id);

    ResponseEntity<String> updateStatus(Map<String, String> requestMap);

    ResponseEntity<List<ProductWrapper>> getByCategory(Integer id);

    ResponseEntity<ProductWrapper> getById(Integer id);

    ResponseEntity<String> deleteProductImage(Integer productId, Integer imageId);

    ResponseEntity<List<ProductWrapper>> searchProducts(String keyword);

    ResponseEntity<List<ProductWrapper>> getRelatedProducts(Integer productId, Integer limit);

    ResponseEntity<String> addToRecentlyViewed(Integer productId);

    ResponseEntity<List<ProductWrapper>> getRecentlyViewedProducts();
}

