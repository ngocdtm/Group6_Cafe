package com.coffee.service;

import com.coffee.entity.ProductHistory;
import com.coffee.wrapper.ProductHistoryWrapper;
import com.coffee.wrapper.ProductImageWrapper;
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

    ResponseEntity<List<ProductWrapper>> searchProducts(String keyword);

    ResponseEntity<List<ProductWrapper>> getRelatedProducts(Integer productId, Integer limit);

    ResponseEntity<String> addToRecentlyViewed(Integer productId);

    ResponseEntity<List<ProductWrapper>> getRecentlyViewedProducts();

    ResponseEntity<String> restoreProduct(Integer id);

    ResponseEntity<List<ProductHistoryWrapper>> getProductHistory(Integer id);

    ResponseEntity<List<ProductWrapper>> getActiveProducts();

    ResponseEntity<List<ProductWrapper>> getDeletedProducts();

    ResponseEntity<String> restoreImage(Integer id);

    ResponseEntity<List<ProductImageWrapper>> getActiveImages(Integer productId);

    ResponseEntity<List<ProductImageWrapper>> getDeletedImages(Integer productId);
}

