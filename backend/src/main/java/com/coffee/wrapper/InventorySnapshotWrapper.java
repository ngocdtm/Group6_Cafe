package com.coffee.wrapper;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class InventorySnapshotWrapper {

    private Long id;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private LocalDate snapshotDate;
    private LocalDateTime createdAt;
}
