package com.coffee.controller;


import com.coffee.entity.InventorySnapshot;
import com.coffee.service.InventoryService;
import com.coffee.wrapper.InventoryTransactionWrapper;
import com.coffee.wrapper.InventoryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {


    @Autowired
    private InventoryService inventoryService;


    @Operation(
            summary = "Add stock to inventory",
            description = "Add stock quantity for a specific product"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/addStock")
    public ResponseEntity<String> addStock(
            @RequestParam Integer productId,
            @RequestParam Integer quantity,
            @RequestParam String note) {
        return inventoryService.addStock(productId, quantity, note);
    }


    @Operation(
            summary = "Remove stock from inventory",
            description = "Remove stock quantity for a specific product"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/removeStock")
    public ResponseEntity<String> removeStock(
            @RequestParam Integer productId,
            @RequestParam Integer quantity,
            @RequestParam String note) {
        return inventoryService.removeStock(productId, quantity, note);
    }


    @Operation(
            summary = "Get inventory status",
            description = "Get current inventory status for a specific product"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/status/{productId}")
    public ResponseEntity<InventoryWrapper> getInventoryStatus(@PathVariable Integer productId) {
        return inventoryService.getInventoryStatus(productId);
    }


    @Operation(
            summary = "Get low stock products",
            description = "Get list of products with low stock"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/lowStock")
    public ResponseEntity<List<InventoryWrapper>> getLowStockProducts() {
        return inventoryService.getLowStockProducts();
    }


    @Operation(
            summary = "Get transaction history",
            description = "Get transaction history for a specific product"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/history/{productId}")
    public ResponseEntity<List<InventoryTransactionWrapper>> getTransactionHistory(@PathVariable Integer productId) {
        return inventoryService.getTransactionHistory(productId);
    }


    @Operation(
            summary = "Get all inventory information",
            description = "Get inventory status for all products"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/all")
    public ResponseEntity<List<InventoryWrapper>> getAllInventory() {
        return inventoryService.getAllInventory();
    }


    @Operation(
            summary = "Update min and max stock",
            description = "Update the minimum and maximum stock quantity for a product"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/updateMinMaxStock/{productId}")
    public ResponseEntity<String> updateMinMaxStock(
            @PathVariable Integer productId,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) Integer maxQuantity) {
        return inventoryService.updateMinMaxStock(productId, minQuantity, maxQuantity);
    }


    @GetMapping("/snapshot/{date}")
    public ResponseEntity<Map<Integer, Integer>> getInventorySnapshotForDate(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        Map<Integer, Integer> snapshot = inventoryService.getInventorySnapshotForDate(date);
        return ResponseEntity.ok(snapshot);
    }


    @GetMapping("/snapshot/latest")
    public ResponseEntity<InventorySnapshot> getLatestInventorySnapshot(@RequestParam Integer productId) {
        InventorySnapshot latestSnapshot = inventoryService.getLatestInventorySnapshot(productId);
        return ResponseEntity.ok(latestSnapshot);
    }
}

