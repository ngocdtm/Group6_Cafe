package com.coffee.repository;

import com.coffee.entity.ProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Integer> {
    @Query("SELECT p FROM ProductHistory p WHERE p.user.id = :userId ORDER BY p.viewedAt DESC")
    List<ProductHistory> findProductHistoryByUserId(@Param("userId") Integer userId);


    Optional<ProductHistory> findByUserIdAndProductId(Integer userId, Integer productId);
}