
package com.coffee.repository;

import com.coffee.entity.Bill;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Integer> {

    List<Bill> getAllBills();

    List<Bill> getBillByUserName(@Param("name") String name);

    Bill findByUuid(String uuid);

    List<Bill> findByOrderDateBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);

    List<Bill> findByOrderDateAfter(LocalDateTime start);

    long countByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(b.totalAfterDiscount), 0) " +
            "FROM Bill b WHERE b.orderDate BETWEEN :startDate AND :endDate")
    int sumTotalAfterDiscountByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT bi.originalProductId, bi.productName, SUM(bi.quantity), SUM(bi.price * bi.quantity) " +
            "FROM Bill b JOIN b.billItems bi " +
            "WHERE b.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY bi.originalProductId, bi.productName " +
            "ORDER BY SUM(bi.quantity) DESC")
    List<Object[]> findBestSellingProducts(LocalDateTime startDate, LocalDateTime endDate, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT c.id, c.name, COALESCE(COUNT(DISTINCT p.id), 0), " +
            "COALESCE(SUM(bi.quantity), 0), COALESCE(SUM(bi.price * bi.quantity), 0) " +
            "FROM Category c " +
            "LEFT JOIN Product p ON p.category = c " +
            "LEFT JOIN BillItem bi ON bi.originalProductId = p.id " +
            "LEFT JOIN bi.bill b " +
            "WHERE b.orderDate BETWEEN :startDate AND :endDate OR b.orderDate IS NULL " +
            "GROUP BY c.id, c.name")
    List<Object[]> findCategoryPerformance(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT p.id, p.name, SUM(bi.quantity) " +
            "FROM Product p " +
            "LEFT JOIN BillItem bi ON bi.originalProductId = p.id " +
            "LEFT JOIN bi.bill b " +
            "WHERE b.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.id, p.name")
    List<Object[]> findInventoryTurnover(LocalDateTime startDate, LocalDateTime endDate);
}
