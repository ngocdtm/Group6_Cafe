package com.coffee.service;

import com.coffee.wrapper.ProductReviewWrapper;
import com.coffee.wrapper.ReviewWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductReviewService {

    ResponseEntity<String> createReview(ReviewWrapper reviewDTO, List<MultipartFile> images);

    ResponseEntity<List<ProductReviewWrapper>> getProductReviews(Integer productId, int page, int size);

    ResponseEntity<List<ProductReviewWrapper>> getUserReviews(int page, int size);

    ResponseEntity<List<ProductReviewWrapper>> getReviewsByBillId(Integer billId);

    ResponseEntity<Boolean> isProductReviewed(Integer billId, Integer productId);
}
