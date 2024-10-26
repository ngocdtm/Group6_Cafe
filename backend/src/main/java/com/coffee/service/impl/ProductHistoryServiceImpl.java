package com.coffee.service.impl;

import com.coffee.entity.Product;
import com.coffee.entity.ProductHistory;
import com.coffee.entity.User;
import com.coffee.repository.ProductHistoryRepository;
import com.coffee.repository.ProductRepository;
import com.coffee.wrapper.ProductHistoryWrapper;
import com.coffee.wrapper.ProductImageWrapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductHistoryServiceImpl {

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public void addToHistory(Integer userId, Integer productId) {
        Optional<ProductHistory> existingHistory = productHistoryRepository.findByUserIdAndProductId(userId, productId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (!productOpt.isPresent()) {
            System.out.println("Product not found for ID: " + productId);
            return;
        }

        Product product = productOpt.get();
        System.out.println("Found product: " + product.getName() + " with ID: " + product.getId());

        if (existingHistory.isPresent()) {
            // Update viewed time
            ProductHistory history = existingHistory.get();
            history.setViewedAt(LocalDateTime.now());
            productHistoryRepository.save(history);
        } else {
            // Create new history entry
            ProductHistory history = new ProductHistory();
            history.setUser(new User(userId));
            history.setProduct(product);
            history.setViewedAt(LocalDateTime.now());
            productHistoryRepository.save(history);
        }
    }

    public List<ProductHistoryWrapper> getUserHistory(Integer userId) {
        List<ProductHistory> histories = productHistoryRepository.findProductHistoryByUserId(userId);

        return histories.stream()
                .map(history -> {
                    Product product = history.getProduct();

                    // Create new ProductHistoryWrapper
                    ProductHistoryWrapper wrapper = new ProductHistoryWrapper();
                    wrapper.setProductId(product.getId());
                    wrapper.setProductName(product.getName());
                    wrapper.setDescription(product.getDescription());
                    wrapper.setPrice(product.getPrice());
                    wrapper.setStatus(product.getStatus());
                    wrapper.setOriginalPrice(product.getOriginalPrice());
                    wrapper.setViewedAt(history.getViewedAt());

                    // Map product images
                    if (product.getImages() != null) {
                        List<ProductImageWrapper> imageWrappers = product.getImages().stream()
                                .map(image -> new ProductImageWrapper(image.getId(), image.getImagePath()))
                                .collect(Collectors.toList());
                        wrapper.setImages(imageWrappers);
                    }

                    return wrapper;
                })
                .collect(Collectors.toList());
    }

}