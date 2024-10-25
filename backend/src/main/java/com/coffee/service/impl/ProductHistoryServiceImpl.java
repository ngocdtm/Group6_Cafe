package com.coffee.service.impl;

import com.coffee.entity.Product;
import com.coffee.entity.ProductHistory;
import com.coffee.entity.User;
import com.coffee.repository.ProductHistoryRepository;
import com.coffee.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductHistoryServiceImpl {

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    public void addToHistory(Integer userId, Integer productId) {
        // Check if product already exists in history
        Optional<ProductHistory> existingHistory =
                productHistoryRepository.findByUserIdAndProductId(userId, productId);

        if (existingHistory.isPresent()) {
            // Update viewed time
            ProductHistory history = existingHistory.get();
            history.setViewedAt(LocalDateTime.now());
            productHistoryRepository.save(history);
        } else {
            // Create new history entry
            ProductHistory history = new ProductHistory();
            history.setUser(new User(userId));
            history.setProduct(new Product(productId));
            history.setViewedAt(LocalDateTime.now());
            productHistoryRepository.save(history);
        }
    }

    public List<ProductWrapper> getUserHistory(Integer userId) {
        return productHistoryRepository.findProductHistoryByUserId(userId);
    }
}
