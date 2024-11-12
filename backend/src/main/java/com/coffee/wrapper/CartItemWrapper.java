package com.coffee.wrapper;


import com.coffee.entity.CartItems;
import com.coffee.entity.Product;
import com.coffee.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class CartItemWrapper {


    private Integer id;
    private Integer quantity;
    private Integer price;
    private Integer productId;
    private String productName;
    private String productStatus;
    private Integer AvailableQuantity;
    private boolean IsOutOfStock;


   

    public boolean isOutOfStock() {
        return IsOutOfStock;
    }


    public void setIsOutOfStock(boolean outOfStock) {
        IsOutOfStock = outOfStock;
    }


    public Integer getAvailableQuantity() {
        return AvailableQuantity;
    }


    public void setAvailableQuantity(Integer availableQuantity) {
        AvailableQuantity = availableQuantity;
    }


    public String getProductStatus() {
        return productStatus;
    }


    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }


    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public Integer getQuantity() {
        return quantity;
    }


    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }


    public Integer getPrice() {
        return price;
    }


    public void setPrice(Integer price) {
        this.price = price;
    }


    public Integer getProductId() {
        return productId;
    }


    public void setProductId(Integer productId) {
        this.productId = productId;
    }


    public String getProductName() {
        return productName;
    }


    public void setProductName(String productName) {
        this.productName = productName;
    }
}



