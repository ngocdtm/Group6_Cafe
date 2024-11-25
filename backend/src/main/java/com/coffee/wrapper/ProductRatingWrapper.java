package com.coffee.wrapper;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ProductRatingWrapper {

    private Double averageRating;
    private Integer totalReviews;
    private Map<Integer, Integer> ratingDistribution; // Key: rating value (1-5), Value: count

    public ProductRatingWrapper() {
        this.ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0);
        }
    }
}
