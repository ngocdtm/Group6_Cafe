package com.coffee.repository;

import com.coffee.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {

    List<InventoryTransaction> findByProductIdOrderByTransactionDateDesc(Integer productId);

    List<InventoryTransaction> findAllByTransactionDateLessThanEqual(LocalDateTime localDateTime);
}
