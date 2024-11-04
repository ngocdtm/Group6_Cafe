package com.coffee.wrapper;

import lombok.Data;

@Data
public class ProductImageWrapper {
    Integer id;
    String imagePath;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public ProductImageWrapper(Integer id, String imagePath) {
        this.id = id;
        this.imagePath = imagePath;
    }
}
