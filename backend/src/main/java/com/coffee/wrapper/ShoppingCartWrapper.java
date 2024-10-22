package com.coffee.wrapper;

import java.util.List;

public class ShoppingCartWrapper {

    private Integer id;
    private Integer totalAmount;
    private Integer userId;
    private List<CartItemWrapper> cartItems;

    // Getters and setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<CartItemWrapper> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemWrapper> cartItems) {
        this.cartItems = cartItems;
    }
}