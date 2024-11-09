package com.coffee.controller;

import com.coffee.service.StatisticsService;
import com.coffee.wrapper.CategoryStatisticsWrapper;
import com.coffee.wrapper.DashboardSummaryWrapper;
import com.coffee.wrapper.InventoryTurnoverWrapper;
import com.coffee.wrapper.ProductStatisticsWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Operation(
            summary = "Get revenue statistics",
            description = "Get revenue statistics by different time periods"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueStatistics(
            @RequestParam(required = false, defaultValue = "monthly") String timeFrame,  // daily, weekly, monthly, yearly
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getRevenueStatistics(timeFrame, specificDate, startDate, endDate);
    }

    @Operation(
            summary = "Get profit statistics",
            description = "Get profit statistics by different time periods"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profit")
    public ResponseEntity<Map<String, Object>> getProfitStatistics(
            @RequestParam(required = false, defaultValue = "monthly") String timeFrame,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getProfitStatistics(timeFrame, specificDate, startDate, endDate);
    }

    @Operation(
            summary = "Get best selling products",
            description = "Get statistics about best selling products"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/best-selling")
    public ResponseEntity<List<ProductStatisticsWrapper>> getBestSellingProducts(
            @RequestParam(required = false, defaultValue = "monthly") String timeFrame,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "5") int limit) {
        return statisticsService.getBestSellingProducts(timeFrame, specificDate, startDate, endDate, limit);
    }

    @Operation(
            summary = "Get category performance",
            description = "Get statistics about category performance"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/category-performance")
    public ResponseEntity<List<CategoryStatisticsWrapper>> getCategoryPerformance(
            @RequestParam(required = false, defaultValue = "monthly") String timeFrame,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getCategoryPerformance(timeFrame, specificDate, startDate, endDate);
    }

    @Operation(
            summary = "Get order statistics",
            description = "Get statistics about orders (total orders, average order value, etc.)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrderStatistics(
            @RequestParam(required = false, defaultValue = "monthly") String timeFrame,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getOrderStatistics(timeFrame, specificDate, startDate, endDate);
    }

    @Operation(
            summary = "Get inventory turnover statistics",
            description = "Get statistics about inventory turnover rate"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/inventory-turnover")
    public ResponseEntity<List<InventoryTurnoverWrapper>> getInventoryTurnover(
            @RequestParam(required = false, defaultValue = "monthly") String timeFrame,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getInventoryTurnover(timeFrame, specificDate, startDate, endDate);
    }

    @Operation(
            summary = "Get dashboard summary",
            description = "Get summary statistics for dashboard"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/dashboard-summary")
    public ResponseEntity<DashboardSummaryWrapper> getDashboardSummary() {
        return statisticsService.getDashboardSummary();
    }
}