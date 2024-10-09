package com.coffee.wrapper;

import lombok.Data;

@Data
public class ProductImageWrapper {
    Integer id;
    String imagePath;

    public ProductImageWrapper(Integer id, String imagePath) {
        this.id = id;
        this.imagePath = imagePath;
    }
}
