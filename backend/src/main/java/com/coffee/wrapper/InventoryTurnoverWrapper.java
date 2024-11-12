package com.coffee.wrapper;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryTurnoverWrapper {
    private Integer productId;
    private String productName;
    private Integer beginningInventory;
    private Integer endingInventory;
    private Integer soldQuantity;
    private Double turnoverRate;
    private Double averageInventory;
}
