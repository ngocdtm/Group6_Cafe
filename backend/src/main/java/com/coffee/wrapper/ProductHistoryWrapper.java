package com.coffee.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ProductHistoryWrapper {
    private Integer productId;
    private String productName;
    private Integer price;
    private Integer originalPrice;
    private String description;
    private String status;
    private LocalDateTime viewedAt;
    private List<ProductImageWrapper> images;
}