package com.coffee.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ProductWrapper {
    private Integer id;
    private String name;
    private String description;
    private Integer price;
    private Integer originalPrice;
    private String status;
    private Integer categoryId;
    private String categoryName;
    private List<ProductImageWrapper> images;
    private List<String> imagePaths;
    private LocalDateTime viewedAt; // Thêm trường này để lưu thời gian xem

    public ProductWrapper(Integer id, String name, String description,
                          Integer price, String status, Integer categoryId,
                          String categoryName, Integer originalPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.originalPrice = originalPrice;
    }
}