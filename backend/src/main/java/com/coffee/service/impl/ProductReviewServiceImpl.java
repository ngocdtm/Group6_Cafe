package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.Bill;
import com.coffee.entity.ProductReview;
import com.coffee.entity.ReviewImage;
import com.coffee.entity.User;
import com.coffee.enums.OrderStatus;
import com.coffee.repository.BillRepository;
import com.coffee.repository.ProductRepository;
import com.coffee.repository.ProductReviewRepository;
import com.coffee.repository.UserRepository;
import com.coffee.security.JwtRequestFilter;
import com.coffee.service.FileStorageService;
import com.coffee.service.ProductReviewService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.ProductReviewWrapper;
import com.coffee.wrapper.ReviewWrapper;
import com.itextpdf.text.exceptions.InvalidImageException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductReviewServiceImpl implements ProductReviewService {

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtRequestFilter jwtFilter;

    @Autowired
    FileStorageService fileStorageService;

    @Override
    @Transactional
    public ResponseEntity<String> createReview(ReviewWrapper reviewDTO, List<MultipartFile> images) {
        try {
            String userEmail = jwtFilter.getCurrentUser();
            User user = userRepository.findByEmail(userEmail);
            Bill bill = billRepository.findById(reviewDTO.getBillId())
                    .orElseThrow(() -> new RuntimeException("Bill not found"));

            // Validate that the bill belongs to the user and is completed
            if (!bill.getUser().getId().equals(user.getId())) {
                return CafeUtils.getResponseEntity("Unauthorized access", HttpStatus.FORBIDDEN);
            }
            if (bill.getOrderStatus() != OrderStatus.COMPLETED) {
                return CafeUtils.getResponseEntity("Order must be completed to leave a review", HttpStatus.BAD_REQUEST);
            }

            // Check if product exists in bill
            boolean productInBill = bill.getBillItems().stream()
                    .anyMatch(item -> item.getOriginalProductId().equals(reviewDTO.getProductId()));
            if (!productInBill) {
                return CafeUtils.getResponseEntity("Product not found in order", HttpStatus.BAD_REQUEST);
            }

            // Check if review already exists
            if (reviewRepository.existsByUserAndBillAndProduct_Id(user, bill, reviewDTO.getProductId())) {
                return CafeUtils.getResponseEntity("Review already exists", HttpStatus.BAD_REQUEST);
            }

            // Create review
            ProductReview review = new ProductReview();
            review.setUser(user);
            review.setBill(bill);
            review.setProduct(productRepository.findById(reviewDTO.getProductId()).orElseThrow());
            review.setRating(reviewDTO.getRating());
            review.setComment(reviewDTO.getComment());
            review.setReviewDate(LocalDateTime.now());

            // Handle image uploads
            if (images != null && !images.isEmpty()) {
                validateImages(images);
                List<String> imagePaths = fileStorageService.saveReviewImages(images);
                List<ReviewImage> reviewImages = imagePaths.stream()
                        .map(path -> {
                            ReviewImage image = new ReviewImage();
                            image.setImagePath(path);
                            image.setReview(review);
                            return image;
                        })
                        .collect(Collectors.toList());
                review.setImages(reviewImages);
            }

            reviewRepository.save(review);
            return CafeUtils.getResponseEntity("Review created successfully", HttpStatus.OK);
        } catch (InvalidImageException e) {
            return CafeUtils.getResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateImages(List<MultipartFile> images) throws InvalidImageException {
        long maxFileSize = 5 * 1024 * 1024; // 5MB
        Set<String> allowedTypes = Set.of("image/jpeg", "image/png", "image/jpg");

        for (MultipartFile file : images) {
            // Check file size
            if (file.getSize() > maxFileSize) {
                throw new InvalidImageException("File size must be less than 5MB");
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
                throw new InvalidImageException("Only JPEG, JPG and PNG files are allowed");
            }
        }
    }

    private boolean validateRating(Integer rating) {
        return rating != null && rating >= 1 && rating <= 5;
    }

    @Override
    public ResponseEntity<List<ProductReviewWrapper>> getProductReviews(Integer productId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("reviewDate").descending());
            Page<ProductReview> reviews = reviewRepository.findByProduct_Id(productId, pageable);
            List<ProductReviewWrapper> reviewWrappers = reviews.getContent().stream()
                    .map(ProductReviewWrapper::fromProductReview)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(reviewWrappers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<ProductReviewWrapper>> getUserReviews(int page, int size) {
        try {
            String userEmail = jwtFilter.getCurrentUser();
            Pageable pageable = PageRequest.of(page, size, Sort.by("reviewDate").descending());
            Page<ProductReview> reviews = reviewRepository.findByUser_Email(userEmail, pageable);
            List<ProductReviewWrapper> reviewWrappers = reviews.getContent().stream()
                    .map(ProductReviewWrapper::fromProductReview)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(reviewWrappers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<ProductReviewWrapper>> getReviewsByBillId(Integer billId) {
        try {
            String userEmail = jwtFilter.getCurrentUser();
            User user = userRepository.findByEmail(userEmail);
            Bill bill = billRepository.findById(billId)
                    .orElseThrow(() -> new RuntimeException("Bill not found"));

            if (!bill.getUser().getId().equals(user.getId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            List<ProductReview> reviews = reviewRepository.findByBillId(billId);
            List<ProductReviewWrapper> reviewWrappers = reviews.stream()
                    .map(ProductReviewWrapper::fromProductReview)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(reviewWrappers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Boolean> isProductReviewed(Integer billId, Integer productId) {
        try {
            String userEmail = jwtFilter.getCurrentUser();
            User user = userRepository.findByEmail(userEmail);
            Bill bill = billRepository.findById(billId)
                    .orElseThrow(() -> new RuntimeException("Bill not found"));

            if (!bill.getUser().getId().equals(user.getId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            boolean isReviewed = reviewRepository.existsByUserAndBillAndProduct_Id(user, bill, productId);
            return new ResponseEntity<>(isReviewed, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
