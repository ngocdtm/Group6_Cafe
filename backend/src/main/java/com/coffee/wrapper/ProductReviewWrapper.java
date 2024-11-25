package com.coffee.wrapper;

import com.coffee.entity.ProductReview;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.coffee.entity.ReviewImage;

@Data
public class ProductReviewWrapper {
    private Integer id;
    private Integer productId;
    private String productName;
    private String userName;
    private Integer rating;
    private String comment;
    private String reviewDate;
    private List<String> images;

    public static ProductReviewWrapper fromProductReview(ProductReview review) {
        ProductReviewWrapper wrapper = new ProductReviewWrapper();
        wrapper.setId(review.getId());
        wrapper.setProductId(review.getProduct().getId());
        wrapper.setProductName(review.getProduct().getName());
        wrapper.setUserName(review.getUser().getName());
        wrapper.setRating(review.getRating());
        wrapper.setComment(review.getComment());
        wrapper.setReviewDate(review.getReviewDate().toString());
        wrapper.setImages(review.getImages().stream()
                .map(ReviewImage::getImagePath)
                .collect(Collectors.toList()));
        return wrapper;
    }
}
