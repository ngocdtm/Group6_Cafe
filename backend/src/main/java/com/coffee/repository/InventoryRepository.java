package com.coffee.repository;


import com.coffee.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;


public interface InventoryRepository extends JpaRepository<Inventory, Integer> {


    Optional<Inventory> findByProductId(Integer productId);


    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.minQuantity")
    List<Inventory> findLowStockInventories();


    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantity <= i.minQuantity")
    long countByQuantityLessThanMinQuantity();
}

