package com.coffee.service;
import com.coffee.wrapper.CategoryStatisticsWrapper;
import com.coffee.wrapper.DashboardSummaryWrapper;
import com.coffee.wrapper.InventoryTurnoverWrapper;
import com.coffee.wrapper.ProductStatisticsWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@Service
public interface StatisticsService {

    ResponseEntity<Map<String, Object>> getRevenueStatistics(String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate);

    ResponseEntity<Map<String, Object>> getProfitStatistics(String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate);

    ResponseEntity<List<ProductStatisticsWrapper>> getBestSellingProducts(String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate, int limit);

    ResponseEntity<List<CategoryStatisticsWrapper>> getCategoryPerformance(String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate);

    ResponseEntity<Map<String, Object>> getOrderStatistics(String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate);

    ResponseEntity<List<InventoryTurnoverWrapper>> getInventoryTurnover(String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate);

    ResponseEntity<DashboardSummaryWrapper> getDashboardSummary();
}
