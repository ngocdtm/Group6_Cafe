package com.coffee.wrapper;

import com.coffee.entity.Product;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductWrapper {

    Product product;

    Integer id;

    String name;

    String description;

    Integer price;

    Integer originalPrice;

    String status;

    Integer categoryId;

    String categoryName;

    List<String> imagePaths;

    List<ProductImageWrapper> images;

    public ProductWrapper(){
        this.imagePaths = new ArrayList<>();
    }

    // Constructor chung cho tất cả các trường hợp
    public ProductWrapper(Integer id, String name, String description, Integer price, Integer originalPrice,
                          String status, Integer categoryId, String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.originalPrice = originalPrice;
        this.status = status;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.imagePaths = new ArrayList<>();
    }

}