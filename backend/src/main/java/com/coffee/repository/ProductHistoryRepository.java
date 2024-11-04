package com.coffee.repository;

import com.coffee.entity.ProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Integer> {

    List<ProductHistory> findByProductIdOrderByModifiedDateDesc(Integer productId);
}
