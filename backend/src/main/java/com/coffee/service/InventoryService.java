package com.coffee.service;

import com.coffee.wrapper.InventoryTransactionWrapper;
import com.coffee.wrapper.InventoryWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface InventoryService {
    ResponseEntity<String> addStock(Integer productId, Integer quantity, String note);

    ResponseEntity<String> removeStock(Integer productId, Integer quantity, String note);

    ResponseEntity<InventoryWrapper> getInventoryStatus(Integer productId);

    ResponseEntity<List<InventoryWrapper>> getLowStockProducts();

    ResponseEntity<List<InventoryTransactionWrapper>> getTransactionHistory(Integer productId);

    ResponseEntity<List<InventoryWrapper>> getAllInventory();

    ResponseEntity<String> updateMinMaxStock(Integer productId, Integer minQuantity, Integer maxQuantity);
}
