package com.coffee.wrapper;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryWrapper {
    private long todayOrders;
    private int todayRevenue;
    private double revenueGrowth;
    private long lowStockItems;
    private List<ProductStatisticsWrapper> bestSellers;
    private List<CategoryPerformanceWrapper> categoryPerformance;
    private Map<String, Long> monthlyOrdersTrend;
    private Map<String, Integer> monthlyRevenueTrend;
    private Map<String, DateRange> dateRanges;
}
