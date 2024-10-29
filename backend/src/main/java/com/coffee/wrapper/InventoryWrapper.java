package com.coffee.wrapper;

import lombok.Data;
import java.util.Date;

@Data
public class InventoryWrapper {
    private Integer id;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private Integer minQuantity;
    private Integer maxQuantity;
    private Date lastUpdated;

    public InventoryWrapper(Integer id, Integer productId, String productName, Integer quantity, Integer minQuantity, Integer maxQuantity, Date lastUpdated) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.lastUpdated = lastUpdated;
    }

    public InventoryWrapper() {

    }
}
