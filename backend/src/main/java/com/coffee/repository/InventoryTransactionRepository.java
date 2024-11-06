package com.coffee.repository;


import com.coffee.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {


    List<InventoryTransaction> findByProductIdOrderByTransactionDateDesc(Integer productId);


    @Query("SELECT it FROM InventoryTransaction it WHERE it.transactionDate BETWEEN :startDate AND :endDate")
    List<InventoryTransaction> findTransactionsByDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );


    List<InventoryTransaction> findAllByTransactionDateLessThanEqual(LocalDateTime localDateTime);
}

