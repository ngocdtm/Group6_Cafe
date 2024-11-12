package com.coffee.wrapper;

import com.coffee.enums.TransactionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryTransactionWrapper {
    private Integer id;
    private Integer productId;
    private String productName;
    private TransactionType transactionType;
    private Integer quantity;
    private String note;
    private String transactionDate;
    private String createdBy;

    public InventoryTransactionWrapper(Integer id, Integer productId, String productName, TransactionType transactionType, Integer quantity, String note, LocalDateTime transactionDate, String createdBy) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.note = note;
        this.transactionDate = (String.valueOf(transactionDate));
        this.createdBy = createdBy;
    }
}