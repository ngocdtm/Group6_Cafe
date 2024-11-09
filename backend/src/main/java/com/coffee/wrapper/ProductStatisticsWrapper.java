package com.coffee.wrapper;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductStatisticsWrapper {
    private Integer productId;
    private String productName;
    private Long totalQuantitySold;
    private Long  totalRevenue;
    private Double averageRating;
    private String stockLevel;
}
