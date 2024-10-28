package com.coffee.repository;

import com.coffee.entity.Product;
import com.coffee.entity.RecentlyViewedProduct;
import com.coffee.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewedProduct, Integer> {

    List<RecentlyViewedProduct> findByUserOrderByViewedAtDesc(User user);

    Optional<RecentlyViewedProduct> findByUserAndProduct(User user, Product product);

    long countByUser(User user);
}
