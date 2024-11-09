package com.coffee.service.impl;

import com.coffee.entity.Bill;
import com.coffee.entity.Inventory;
import com.coffee.entity.InventoryTransaction;
import com.coffee.enums.OrderStatus;
import com.coffee.enums.OrderType;
import com.coffee.enums.TransactionType;
import com.coffee.repository.*;
import com.coffee.service.StatisticsService;
import com.coffee.utils.DateUtils;
import com.coffee.wrapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private BillRepository billRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

//    @Autowired
//    private ProductRatingRepository productRatingRepository; // Add this if you have ratings

    @Override
    public ResponseEntity<Map<String, Object>> getRevenueStatistics(
            String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate) {
        try {
            DateRange dateRange = DateUtils.calculateDateRange(timeFrame, specificDate, startDate, endDate);
            List<Bill> bills = billRepository.findByOrderDateBetween(
                    dateRange.getStartDateTime(),
                    dateRange.getEndDateTime()
            );

            Map<String, Object> statistics = new HashMap<>();

            int totalRevenue = bills.stream()
                    .mapToInt(Bill::getTotalAfterDiscount)
                    .sum();

            Map<String, Integer> revenueByPeriod = bills.stream()
                    .collect(Collectors.groupingBy(
                            bill -> DateUtils.formatPeriod(bill.getOrderDate(), timeFrame),
                            Collectors.summingInt(Bill::getTotalAfterDiscount)
                    ));

            statistics.put("totalRevenue", totalRevenue);
            statistics.put("revenueByPeriod", revenueByPeriod);
            statistics.put("growth", calculateGrowth(revenueByPeriod));
            statistics.put("dateRange", Map.of(
                    "start", dateRange.getStartDateTime(),
                    "end", dateRange.getEndDateTime()
            ));

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error calculating revenue statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error calculating statistics"));
        }
    }

    @Override
    public ResponseEntity<List<ProductStatisticsWrapper>> getBestSellingProducts(
            String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate, int limit) {
        try {
            DateRange dateRange = DateUtils.calculateDateRange(timeFrame, specificDate, startDate, endDate);
            List<Object[]> results = billRepository.findBestSellingProducts(
                    dateRange.getStartDateTime(),
                    dateRange.getEndDateTime(),
                    PageRequest.of(0, limit)
            );

            List<ProductStatisticsWrapper> statistics = results.stream()
                    .map(result -> {
                        Integer productId = Integer.valueOf(String.valueOf(result[0]));
                        Inventory inventory = inventoryRepository.findByProductId(productId)
                                .orElse(null);
                        String stockLevel = calculateStockLevel(inventory);

                        return ProductStatisticsWrapper.builder()
                                .productId(productId)
                                .productName((String) result[1])
                                .totalQuantitySold(Long.valueOf(String.valueOf(result[2])))
                                .totalRevenue(Long.valueOf(String.valueOf(result[3])))
                                .stockLevel(stockLevel)
                                .averageRating(calculateAverageRating(productId))
                                .build();
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error calculating best selling products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    private Double calculateAverageRating(Integer productId) {
        // Giả sử bạn có bảng ratings để lưu đánh giá sản phẩm
        // Nếu chưa có, bạn có thể thêm vào sau
        return 0.0; // Temporary return default value
    }

    private String calculateStockLevel(Inventory inventory) {
        if (inventory == null) return "OUT_OF_STOCK";

        int quantity = inventory.getQuantity();
        int minQuantity = inventory.getMinQuantity() != null ? inventory.getMinQuantity() : 0;
        int maxQuantity = inventory.getMaxQuantity() != null ? inventory.getMaxQuantity() : Integer.MAX_VALUE;

        if (quantity <= 0) return "OUT_OF_STOCK";
        if (quantity <= minQuantity) return "LOW_STOCK";
        if (quantity >= maxQuantity) return "OVERSTOCKED";
        return "IN_STOCK";
    }

    @Override
    public ResponseEntity<DashboardSummaryWrapper> getDashboardSummary() {
        try {
            // Get today's date range
            DateRange todayRange = DateUtils.calculateDateRange("daily", LocalDate.now(), null, null);

            // Get yesterday's date range
            DateRange yesterdayRange = DateUtils.calculateDateRange("daily", LocalDate.now().minusDays(1), null, null);

            // Get last 30 days range for best sellers and category performance
            DateRange thirtyDaysRange = DateUtils.calculateDateRange(
                    "daily",
                    null,
                    LocalDate.now().minusDays(30),
                    LocalDate.now()
            );

            // Get today's statistics
            long todayOrders = billRepository.countByOrderDateBetween(
                    todayRange.getStartDateTime(),
                    todayRange.getEndDateTime()
            );

            int todayRevenue = billRepository.sumTotalAfterDiscountByOrderDateBetween(
                    todayRange.getStartDateTime(),
                    todayRange.getEndDateTime()
            );

            // Get previous day's revenue for growth calculation
            int yesterdayRevenue = billRepository.sumTotalAfterDiscountByOrderDateBetween(
                    yesterdayRange.getStartDateTime(),
                    yesterdayRange.getEndDateTime()
            );

            // Calculate revenue growth
            double revenueGrowth = yesterdayRevenue > 0
                    ? ((todayRevenue - yesterdayRevenue) / (double) yesterdayRevenue) * 100
                    : 0.0;

            // Get inventory statistics
            long lowStockItems = inventoryRepository.countByQuantityLessThanMinQuantity();

            // Get best selling products for last 30 days
            ResponseEntity<List<ProductStatisticsWrapper>> bestSellersResponse = getBestSellingProducts(
                    "daily",
                    null,
                    thirtyDaysRange.getStartDateTime().toLocalDate(),
                    thirtyDaysRange.getEndDateTime().toLocalDate(),
                    5
            );
            List<ProductStatisticsWrapper> bestSellers = bestSellersResponse.getBody();

            // Get category performance for last 30 days
            List<Object[]> categoryResults = billRepository.findCategoryPerformance(
                    thirtyDaysRange.getStartDateTime(),
                    thirtyDaysRange.getEndDateTime()
            );

            List<CategoryPerformanceWrapper> categoryPerformance = categoryResults.stream()
                    .map(result -> CategoryPerformanceWrapper.builder()
                            .categoryId(((Number) result[0]).intValue())
                            .categoryName((String) result[1])
                            .productCount(((Number) result[2]).longValue())
                            .totalQuantitySold(((Number) result[3]).longValue())
                            .totalRevenue(((Number) result[4]).longValue())
                            .build())
                    .collect(Collectors.toList());

            // Calculate monthly trends
            DateRange monthlyRange = DateUtils.calculateDateRange("monthly", LocalDate.now(), null, null);

            // Monthly orders trend
            Map<String, Long> monthlyOrdersTrend = billRepository.findByOrderDateBetween(
                            monthlyRange.getStartDateTime(),
                            monthlyRange.getEndDateTime()
                    )
                    .stream()
                    .collect(Collectors.groupingBy(
                            bill -> DateUtils.formatPeriod(bill.getOrderDate(), "daily"),
                            Collectors.counting()
                    ));

            // Monthly revenue trend
            Map<String, Integer> monthlyRevenueTrend = billRepository.findByOrderDateBetween(
                            monthlyRange.getStartDateTime(),
                            monthlyRange.getEndDateTime()
                    )
                    .stream()
                    .collect(Collectors.groupingBy(
                            bill -> DateUtils.formatPeriod(bill.getOrderDate(), "daily"),
                            Collectors.summingInt(Bill::getTotalAfterDiscount)
                    ));

            // Build the dashboard summary
            DashboardSummaryWrapper summary = DashboardSummaryWrapper.builder()
                    .todayOrders(todayOrders)
                    .todayRevenue(todayRevenue)
                    .revenueGrowth(revenueGrowth)
                    .lowStockItems(lowStockItems)
                    .bestSellers(bestSellers)
                    .categoryPerformance(categoryPerformance)
                    .monthlyOrdersTrend(monthlyOrdersTrend)
                    .monthlyRevenueTrend(monthlyRevenueTrend)
                    .dateRanges(Map.of(
                            "today", todayRange,
                            "lastThirtyDays", thirtyDaysRange,
                            "currentMonth", monthlyRange
                    ))
                    .build();

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error generating dashboard summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getProfitStatistics(
            String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate) {
        try {
            DateRange dateRange = DateUtils.calculateDateRange(timeFrame, specificDate, startDate, endDate);
            List<Bill> bills = billRepository.findByOrderDateBetween(
                    dateRange.getStartDateTime(),
                    dateRange.getEndDateTime()
            );

            Map<String, Object> statistics = new HashMap<>();

            // Calculate total revenue and cost
            int totalRevenue = bills.stream()
                    .mapToInt(Bill::getTotalAfterDiscount)
                    .sum();

            int totalCost = bills.stream()
                    .flatMap(bill -> bill.getBillItems().stream())
                    .mapToInt(item -> item.getOriginalPrice() * item.getQuantity())
                    .sum();

            int totalProfit = totalRevenue - totalCost;

            // Calculate profit by period
            Map<String, Integer> profitByPeriod = bills.stream()
                    .collect(Collectors.groupingBy(
                            bill -> DateUtils.formatPeriod(bill.getOrderDate(), timeFrame),
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    periodBills -> {
                                        int periodRevenue = periodBills.stream()
                                                .mapToInt(Bill::getTotalAfterDiscount)
                                                .sum();
                                        int periodCost = periodBills.stream()
                                                .flatMap(bill -> bill.getBillItems().stream())
                                                .mapToInt(item -> item.getOriginalPrice() * item.getQuantity())
                                                .sum();
                                        return periodRevenue - periodCost;
                                    }
                            )
                    ));

            statistics.put("totalProfit", totalProfit);
            statistics.put("profitMargin", totalRevenue > 0 ? (double) totalProfit / totalRevenue * 100 : 0);
            statistics.put("profitByPeriod", profitByPeriod);
            statistics.put("growth", calculateGrowth(profitByPeriod));
            statistics.put("dateRange", Map.of(
                    "start", dateRange.getStartDateTime(),
                    "end", dateRange.getEndDateTime()
            ));

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error calculating profit statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error calculating statistics"));
        }
    }

    @Override
    public ResponseEntity<List<CategoryStatisticsWrapper>> getCategoryPerformance(
            String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate) {
        try {
            DateRange dateRange = DateUtils.calculateDateRange(timeFrame, specificDate, startDate, endDate);
            List<Object[]> results = billRepository.findCategoryPerformance(
                    dateRange.getStartDateTime(),
                    dateRange.getEndDateTime()
            );

            int totalSales = results.stream()
                    .mapToInt(result -> ((Number) result[3]).intValue())
                    .sum();

            List<CategoryStatisticsWrapper> statistics = results.stream()
                    .map(result -> CategoryStatisticsWrapper.builder()
                            .categoryId(((Number) result[0]).intValue())
                            .categoryName((String) result[1])
                            .totalProducts(((Number) result[2]).longValue())
                            .totalSales(((Number) result[3]).longValue())
                            .totalRevenue(((Number) result[4]).intValue())
                            .percentageOfTotalSales(totalSales > 0
                                    ? (double) ((Number) result[4]).intValue() / totalSales * 100
                                    : 0.0)
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error calculating category performance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getOrderStatistics(
            String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate) {
        try {
            DateRange dateRange = DateUtils.calculateDateRange(timeFrame, specificDate, startDate, endDate);
            List<Bill> bills = billRepository.findByOrderDateBetween(
                    dateRange.getStartDateTime(),
                    dateRange.getEndDateTime()
            );

            Map<String, Object> statistics = new HashMap<>();

            // Total orders and average order value
            long totalOrders = bills.size();
            double averageOrderValue = bills.stream()
                    .mapToInt(Bill::getTotalAfterDiscount)
                    .average()
                    .orElse(0.0);

            // Orders by status and type
            Map<OrderStatus, Long> ordersByStatus = bills.stream()
                    .collect(Collectors.groupingBy(
                            Bill::getOrderStatus,
                            Collectors.counting()
                    ));

            Map<OrderType, Long> ordersByType = bills.stream()
                    .collect(Collectors.groupingBy(
                            Bill::getOrderType,
                            Collectors.counting()
                    ));

            // Orders by period
            Map<String, Long> ordersByPeriod = bills.stream()
                    .collect(Collectors.groupingBy(
                            bill -> DateUtils.formatPeriod(bill.getOrderDate(), timeFrame),
                            Collectors.counting()
                    ));

            statistics.put("totalOrders", totalOrders);
            statistics.put("averageOrderValue", averageOrderValue);
            statistics.put("ordersByStatus", ordersByStatus);
            statistics.put("ordersByType", ordersByType);
            statistics.put("ordersByPeriod", ordersByPeriod);
            statistics.put("growth", calculateGrowth(ordersByPeriod.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().intValue()
                    ))));
            statistics.put("dateRange", Map.of(
                    "start", dateRange.getStartDateTime(),
                    "end", dateRange.getEndDateTime()
            ));

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error calculating order statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error calculating statistics"));
        }
    }

    @Override
    public ResponseEntity<List<InventoryTurnoverWrapper>> getInventoryTurnover(
            String timeFrame, LocalDate specificDate, LocalDate startDate, LocalDate endDate) {
        try {
            DateRange dateRange = DateUtils.calculateDateRange(timeFrame, specificDate, startDate, endDate);
            List<Object[]> results = billRepository.findInventoryTurnover(
                    dateRange.getStartDateTime(),
                    dateRange.getEndDateTime()
            );

            // Get beginning inventory from snapshot
            Map<Integer, Integer> beginningInventory = getInventorySnapshotAtDate(
                    dateRange.getStartDateTime().toLocalDate());

            List<InventoryTurnoverWrapper> statistics = results.stream()
                    .map(result -> {
                        Integer productId = ((Number) result[0]).intValue();
                        String productName = (String) result[1];
                        Integer soldQuantity = ((Number) result[2]).intValue();

                        Inventory currentInventory = inventoryRepository.findByProductId(productId)
                                .orElse(null);
                        Integer endingInventory = currentInventory != null ? currentInventory.getQuantity() : 0;
                        Integer beginningInv = beginningInventory.getOrDefault(productId, 0);
                        Double averageInventory = (beginningInv + endingInventory) / 2.0;
                        Double turnoverRate = averageInventory > 0 ? soldQuantity / averageInventory : 0.0;

                        return InventoryTurnoverWrapper.builder()
                                .productId(productId)
                                .productName(productName)
                                .beginningInventory(beginningInv)
                                .endingInventory(endingInventory)
                                .soldQuantity(soldQuantity)
                                .turnoverRate(turnoverRate)
                                .averageInventory(averageInventory)
                                .build();
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error calculating inventory turnover", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    private InventoryTurnoverWrapper mapToInventoryTurnoverWrapper(Object[] result, LocalDate startDate) {
        Integer productId = ((Number) result[0]).intValue();
        String productName = (String) result[1];
        Integer soldQuantity = ((Number) result[2]).intValue();

        // Get current inventory
        Inventory currentInventory = inventoryRepository.findByProductId(productId)
                .orElse(null);
        Integer endingInventory = currentInventory != null ? currentInventory.getQuantity() : 0;

        // Get beginning inventory from snapshot
        Integer beginningInv = getInventorySnapshotAtDate(startDate).getOrDefault(productId, 0);

        // Calculate average inventory
        Double averageInventory = (beginningInv + endingInventory) / 2.0;

        // Calculate turnover rate
        Double turnoverRate = averageInventory > 0
                ? soldQuantity / averageInventory
                : 0.0;

        return InventoryTurnoverWrapper.builder()
                .productId(productId)
                .productName(productName)
                .beginningInventory(beginningInv)
                .endingInventory(endingInventory)
                .soldQuantity(soldQuantity)
                .turnoverRate(turnoverRate)
                .averageInventory(averageInventory)
                .build();
    }

    // Helper methods
    private Map<String, Double> calculateGrowth(Map<String, Integer> dataByPeriod) {
        if (dataByPeriod == null || dataByPeriod.size() < 2) {
            return Map.of("growth", 0.0);
        }

        List<String> periods = new ArrayList<>(dataByPeriod.keySet());
        Collections.sort(periods);

        String currentPeriod = periods.get(periods.size() - 1);
        String previousPeriod = periods.get(periods.size() - 2);

        double currentValue = dataByPeriod.get(currentPeriod);
        double previousValue = dataByPeriod.get(previousPeriod);

        double growth = previousValue != 0
                ? ((currentValue - previousValue) / previousValue) * 100
                : 0.0;

        return Map.of(
                "currentPeriod", currentValue,
                "previousPeriod", previousValue,
                "growthRate", growth
        );
    }

    private LocalDateTime calculateStartDate(String timeFrame) {
        LocalDateTime now = LocalDateTime.now();
        return switch (timeFrame.toLowerCase()) {
            case "daily" -> now.minusDays(1);
            case "weekly" -> now.minusWeeks(1);
            case "monthly" -> now.minusMonths(1);
            case "yearly" -> now.minusYears(1);
            default -> now.minusMonths(1); // Default to monthly
        };
    }

    private String formatPeriod(LocalDateTime date, String timeFrame) {
        return switch (timeFrame.toLowerCase()) {
            case "daily" -> date.format(DateTimeFormatter.ISO_DATE);
            case "weekly" -> date.format(DateTimeFormatter.ISO_WEEK_DATE);
            case "monthly" -> date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case "yearly" -> date.format(DateTimeFormatter.ofPattern("yyyy"));
            default -> date.format(DateTimeFormatter.ISO_DATE);
        };
    }

    // Helper method to get inventory snapshot at a specific date
    private Map<Integer, Integer> getInventorySnapshotAtDate(LocalDate date) {
        // This should be implemented based on your InventoryTransaction history
        // Here's a simple implementation that you should modify based on your needs
        Map<Integer, Integer> snapshot = new HashMap<>();
        List<InventoryTransaction> transactions = inventoryTransactionRepository
                .findAllByTransactionDateLessThanEqual(date.atTime(LocalTime.MAX));

        for (InventoryTransaction transaction : transactions) {
            Integer productId = transaction.getProduct().getId();
            snapshot.merge(productId,
                    transaction.getTransactionType() == TransactionType.IMPORT ?
                            transaction.getQuantity() : -transaction.getQuantity(),
                    Integer::sum);
        }

        return snapshot;
    }
}