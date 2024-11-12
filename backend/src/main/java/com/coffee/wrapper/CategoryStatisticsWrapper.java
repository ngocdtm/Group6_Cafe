package com.coffee.wrapper;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryStatisticsWrapper {
    private Integer categoryId;
    private String categoryName;
    private Long totalProducts;
    private Long totalSales;
    private Integer totalRevenue;
    private Double percentageOfTotalSales;
}
