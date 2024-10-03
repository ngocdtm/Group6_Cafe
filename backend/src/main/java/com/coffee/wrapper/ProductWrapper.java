package com.coffee.wrapper;

import lombok.Data;

@Data
public class ProductWrapper {

    Integer id;
    String name;
    String description;
    Integer price;
    String status;
    Integer categoryId;
    String categoryName;

    public ProductWrapper(Integer id,String name, String description,Integer price,String status,  Integer categoryId,String categoryName) {
        this.status = status;
        this.price = price;
        this.name = name;
        this.id = id;
        this.description = description;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
    }

    public ProductWrapper(Integer id,String name) {
        this.id = id;
        this.name = name;
    }

    public ProductWrapper(Integer id,String name, String description, Integer price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public ProductWrapper() {

    }
}