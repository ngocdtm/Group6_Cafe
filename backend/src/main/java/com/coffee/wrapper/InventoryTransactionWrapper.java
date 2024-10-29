package com.coffee.wrapper;

import com.coffee.enums.TransactionType;
import lombok.Data;
import java.util.Date;

@Data
public class InventoryTransactionWrapper {
    private Integer id;
    private Integer productId;
    private String productName;
    private TransactionType transactionType;
    private Integer quantity;
    private String note;
    private Date transactionDate;
    private String createdBy;

    public InventoryTransactionWrapper(Integer id, Integer productId, String productName, TransactionType transactionType, Integer quantity, String note, Date transactionDate, String createdBy) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.note = note;
        this.transactionDate = transactionDate;
        this.createdBy = createdBy;
    }
}