package com.coffee.controller;

import com.coffee.entity.ShoppingCart;
import com.coffee.service.ShoppingCartService;
import com.coffee.wrapper.ShoppingCartWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
public class ShoppingCartController {

    @Autowired
    ShoppingCartService shoppingCartService;

    @Operation(summary = "Add item to cart")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody Map<String, String> requestMap) {
        return shoppingCartService.addToCart(requestMap);
    }

    @Operation(summary = "Update cart item quantity")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/update")
    public ResponseEntity<String> updateCartItem(@RequestBody Map<String, String> requestMap) {
        return shoppingCartService.updateCartItem(requestMap);
    }

    @Operation(summary = "Remove item from cart")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<String> removeFromCart(@PathVariable Integer cartItemId) {
        return shoppingCartService.removeFromCart(cartItemId);
    }

    @Operation(summary = "Get user's cart")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/get")
    public ResponseEntity<ShoppingCartWrapper> getCart() {
        return shoppingCartService.getCart();
    }

    @Operation(summary = "Clear cart")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart() {
        return shoppingCartService.clearCart();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount() {
        try {
            return shoppingCartService.getCartItemCount();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}