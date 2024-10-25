package com.coffee.service;

import com.coffee.entity.ShoppingCart;
import com.coffee.wrapper.ShoppingCartWrapper;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ShoppingCartService {
    ResponseEntity<String> addToCart(Map<String, String> requestMap);
    ResponseEntity<String> updateCartItem(Map<String, String> requestMap);
    ResponseEntity<String> removeFromCart(Integer cartItemId);
    ResponseEntity<ShoppingCartWrapper> getCart();
    ResponseEntity<String> clearCart();
    ResponseEntity<Integer> getCartItemCount();
}
