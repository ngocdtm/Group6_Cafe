package com.coffee.service.impl;


import com.coffee.entity.Inventory;
import com.coffee.entity.InventorySnapshot;
import com.coffee.entity.InventoryTransaction;
import com.coffee.enums.TransactionType;
import com.coffee.repository.InventoryRepository;
import com.coffee.repository.InventorySnapshotRepository;
import com.coffee.repository.InventoryTransactionRepository;
import com.coffee.service.InventorySnapshotService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class InventorySnapshotServiceImpl implements InventorySnapshotService {


    @Autowired
    private InventoryRepository inventoryRepository;


    @Autowired
    private InventorySnapshotRepository snapshotRepository;


    @Autowired
    private InventoryTransactionRepository transactionRepository;


    @Transactional
    public void createDailySnapshot() {
        LocalDate today = LocalDate.now();
        List<Inventory> currentInventory = inventoryRepository.findAll();


        for (Inventory inventory : currentInventory) {
            try {
                InventorySnapshot snapshot = new InventorySnapshot();
                snapshot.setProduct(inventory.getProduct());
                snapshot.setQuantity(inventory.getQuantity());
                snapshot.setSnapshotDate(today);
                snapshot.setCreatedAt(LocalDateTime.now());


                snapshotRepository.save(snapshot);
            } catch (Exception e) {
                log.error("Error creating snapshot for product ID: " + inventory.getProduct().getId(), e);
            }
        }


        log.info("Daily inventory snapshot created for date: {}", today);
    }


    public Map<Integer, Integer> getInventorySnapshotForDate(LocalDate date) {
        List<InventorySnapshot> snapshots = snapshotRepository.findLatestSnapshotBeforeDate(date);


        if (snapshots.isEmpty()) {
            log.warn("No snapshot found for date: {}. Calculating from transaction history...", date);
            return calculateInventoryFromTransactions(date);
        }


        return snapshots.stream()
                .collect(Collectors.toMap(
                        snapshot -> snapshot.getProduct().getId(),
                        InventorySnapshot::getQuantity
                ));
    }


    private Map<Integer, Integer> calculateInventoryFromTransactions(LocalDate date) {
        List<InventoryTransaction> transactions = transactionRepository
                .findAllByTransactionDateLessThanEqual(date.atTime(LocalTime.MAX));


        Map<Integer, Integer> inventoryMap = new HashMap<>();


        for (InventoryTransaction transaction : transactions) {
            Integer productId = transaction.getProduct().getId();
            Integer quantity = transaction.getQuantity();


            if (transaction.getTransactionType() == TransactionType.IMPORT) {
                inventoryMap.merge(productId, quantity, Integer::sum);
            } else {
                inventoryMap.merge(productId, -quantity, Integer::sum);
            }
        }


        return inventoryMap;
    }
}

