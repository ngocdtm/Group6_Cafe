package com.coffee.repository;

import com.coffee.entity.ShoppingCart;
import com.coffee.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {
    Optional<ShoppingCart> findByUser(User user);
}