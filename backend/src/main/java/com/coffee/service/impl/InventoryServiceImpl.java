package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.Inventory;
import com.coffee.entity.InventoryTransaction;
import com.coffee.entity.Product;
import com.coffee.enums.TransactionType;
import com.coffee.repository.InventoryRepository;
import com.coffee.repository.InventoryTransactionRepository;
import com.coffee.repository.ProductRepository;
import com.coffee.security.JwtRequestFilter;
import com.coffee.security.ResourceNotFoundException;
import com.coffee.service.InventoryService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.InventoryTransactionWrapper;
import com.coffee.wrapper.InventoryWrapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Override
    @Transactional
    public ResponseEntity<String> addStock(Integer productId, Integer quantity, String note) {
        try {
            if (!jwtRequestFilter.isAdmin()) {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

            if (quantity <= 0) {
                return CafeUtils.getResponseEntity("Quantity must be greater than 0", HttpStatus.BAD_REQUEST);
            }

            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isEmpty()) {
                return CafeUtils.getResponseEntity("Product not found", HttpStatus.NOT_FOUND);
            }

            Product product = productOptional.get();
            // Nếu thêm stock cho sản phẩm đang OUT_OF_STOCK, cập nhật lại status
            if ("OUT_OF_STOCK".equals(product.getStatus())) {
                product.setStatus("true");
                productRepository.save(product);
            }

            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseGet(() -> {
                        Inventory newInventory = new Inventory();
                        newInventory.setProduct(productOptional.get());
                        newInventory.setQuantity(0);
                        // Set default values for min and max quantity
                        newInventory.setMinQuantity(10); // Giá trị mặc định cho min quantity
                        newInventory.setMaxQuantity(100); // Giá trị mặc định cho max quantity
                        return newInventory;
                    });

            // Kiểm tra số lượng tối đa nếu maxQuantity được thiết lập
            Integer maxQty = inventory.getMaxQuantity();
            if (maxQty != null && (inventory.getQuantity() + quantity) > maxQty) {
                return CafeUtils.getResponseEntity(
                        String.format("Cannot add more than the maximum quantity. Current: %d, Adding: %d, Max: %d",
                                inventory.getQuantity(), quantity, maxQty),
                        HttpStatus.BAD_REQUEST
                );
            }

            // Cập nhật số lượng tồn kho
            inventory.setQuantity(inventory.getQuantity() + quantity);
            inventory.setLastUpdated(new Date());
            inventoryRepository.save(inventory);

            // Ghi lại lịch sử giao dịch
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(productOptional.get());
            transaction.setTransactionType(TransactionType.IMPORT);
            transaction.setQuantity(quantity);
            transaction.setNote(note);
            transaction.setTransactionDate(new Date());
            transaction.setCreatedBy(jwtRequestFilter.getCurrentUser());
            transactionRepository.save(transaction);

            return CafeUtils.getResponseEntity("Stock added successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(CafeConstants.SOMETHING_WENT_WRONG, ex);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> removeStock(Integer productId, Integer quantity, String note) {
        try {
//            if (!jwtRequestFilter.isAdmin()) {
//                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
//            }

            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found in inventory"));

            // Kiểm tra số lượng hàng cần trừ
            if (inventory.getQuantity() < quantity) {
                return CafeUtils.getResponseEntity("Insufficient stock", HttpStatus.BAD_REQUEST);
            }

            // Cập nhật số lượng tồn kho
            int newQuantity = inventory.getQuantity() - quantity;
            inventory.setQuantity(newQuantity);
            inventory.setLastUpdated(new Date());

            // Nếu số lượng = 0, cập nhật trạng thái sản phẩm thành OUT_OF_STOCK
            if (newQuantity == 0) {
                Product product = inventory.getProduct();
                product.setStatus("OUT_OF_STOCK");
                productRepository.save(product);

                // Ghi lại transaction cho việc hết hàng
                InventoryTransaction outOfStockTrans = new InventoryTransaction();
                outOfStockTrans.setProduct(product);
                outOfStockTrans.setTransactionType(TransactionType.OUT_OF_STOCK);
                outOfStockTrans.setQuantity(0);
                outOfStockTrans.setNote("Product is out of stock");
                outOfStockTrans.setTransactionDate(new Date());
                outOfStockTrans.setCreatedBy(jwtRequestFilter.getCurrentUser());
                transactionRepository.save(outOfStockTrans);
            }

            inventoryRepository.save(inventory);

            // Ghi lại lịch sử giao dịch xuất hàng
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(inventory.getProduct());
            transaction.setTransactionType(TransactionType.EXPORT);
            transaction.setQuantity(quantity);
            transaction.setNote(note);
            transaction.setTransactionDate(new Date());
            transaction.setCreatedBy(jwtRequestFilter.getCurrentUser());
            transactionRepository.save(transaction);

            return CafeUtils.getResponseEntity("Stock removed successfully", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return CafeUtils.getResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(CafeConstants.SOMETHING_WENT_WRONG, ex);
        }
    }

    @Override
    public ResponseEntity<InventoryWrapper> getInventoryStatus(Integer productId) {
        try {
            Optional<Inventory> inventoryOptional = inventoryRepository.findByProductId(productId);
            if (inventoryOptional.isEmpty()) {
                return new ResponseEntity<>(new InventoryWrapper(), HttpStatus.NOT_FOUND);
            }

            Inventory inventory = inventoryOptional.get();
            InventoryWrapper wrapper = new InventoryWrapper(
                    inventory.getId(),
                    inventory.getProduct().getId(),
                    inventory.getProduct().getName(),
                    inventory.getQuantity(),
                    inventory.getMinQuantity(),
                    inventory.getMaxQuantity(),
                    inventory.getLastUpdated()
            );

            return new ResponseEntity<>(wrapper, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new InventoryWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<InventoryWrapper>> getLowStockProducts() {
        try {
            List<Inventory> lowStockInventories = inventoryRepository.findLowStockInventories();

            List<InventoryWrapper> wrappers = lowStockInventories.stream()
                    .map(inventory -> new InventoryWrapper(
                            inventory.getId(),
                            inventory.getProduct().getId(),
                            inventory.getProduct().getName(),
                            inventory.getQuantity(),
                            inventory.getMinQuantity(),
                            inventory.getMaxQuantity(),
                            inventory.getLastUpdated()
                    ))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(wrappers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<InventoryTransactionWrapper>> getTransactionHistory(Integer productId) {
        try {
            List<InventoryTransaction> transactions = transactionRepository.findByProductIdOrderByTransactionDateDesc(productId);

            List<InventoryTransactionWrapper> wrappers = transactions.stream()
                    .map(transaction -> new InventoryTransactionWrapper(
                            transaction.getId(),
                            transaction.getProduct().getId(),
                            transaction.getProduct().getName(),
                            transaction.getTransactionType(),
                            transaction.getQuantity(),
                            transaction.getNote(),
                            transaction.getTransactionDate(),
                            transaction.getCreatedBy()
                    ))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(wrappers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<InventoryWrapper>> getAllInventory() {
        try {
            if (!jwtRequestFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<Inventory> inventories = inventoryRepository.findAll();

            List<InventoryWrapper> wrappers = inventories.stream()
                    .map(inventory -> new InventoryWrapper(
                            inventory.getId(),
                            inventory.getProduct().getId(),
                            inventory.getProduct().getName(),
                            inventory.getQuantity(),
                            inventory.getMinQuantity(),
                            inventory.getMaxQuantity(),
                            inventory.getLastUpdated()
                    ))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(wrappers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> updateMinMaxStock(Integer productId, Integer minQuantity, Integer maxQuantity) {
        try {
            if (!jwtRequestFilter.isAdmin()) {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found in inventory"));

            if (minQuantity != null) {
                inventory.setMinQuantity(minQuantity);
            }

            if (maxQuantity != null) {
                inventory.setMaxQuantity(maxQuantity);
            }

            inventory.setLastUpdated(new Date());
            inventoryRepository.save(inventory);

            return CafeUtils.getResponseEntity("Min and max stock updated successfully", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return CafeUtils.getResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(CafeConstants.SOMETHING_WENT_WRONG, ex);
        }
    }

}
