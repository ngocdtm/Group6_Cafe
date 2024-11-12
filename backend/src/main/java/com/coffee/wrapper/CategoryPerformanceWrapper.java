package com.coffee.wrapper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CategoryPerformanceWrapper {
    private Integer categoryId;
    private String categoryName;
    private Long productCount;
    private Long totalQuantitySold;
    private Long totalRevenue;
}
