package com.coffee.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.coffee.entity.ProductReview;
import com.coffee.entity.User;
import com.coffee.entity.Bill;

import java.util.List;


public interface ProductReviewRepository extends JpaRepository<ProductReview, Integer> {
    Page<ProductReview> findByProduct_Id(Integer productId, Pageable pageable);
    Page<ProductReview> findByUser_Email(String email, Pageable pageable);
    boolean existsByUserAndBillAndProduct_Id(User user, Bill bill, Integer productId);

    List<ProductReview> findByBillId(Integer billId);
}