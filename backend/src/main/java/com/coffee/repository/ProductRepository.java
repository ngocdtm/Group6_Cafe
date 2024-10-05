
package com.coffee.repository;

import com.coffee.entity.Product;
import com.coffee.wrapper.ProductWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT NEW com.coffee.wrapper.ProductWrapper(p.id, p.name, p.description, " +
            "p.price, p.status, p.category.id, p.category.name) FROM Product p")
    List<ProductWrapper> getAllProduct();

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.status=:status WHERE p.id=:id")
    Integer updateProductStatus(@Param("status") String status, @Param("id") Integer id);

    @Query("SELECT NEW com.coffee.wrapper.ProductWrapper(p.id, p.name) " +
            "FROM Product p WHERE p.category.id=:id AND p.status='true'")
    List<ProductWrapper> getProductByCategory(@Param("id") Integer id);

    @Query("SELECT NEW com.coffee.wrapper.ProductWrapper(p.id, p.name, p.description, p.price) " +
            "FROM Product p WHERE p.id=:id")
    ProductWrapper getProductById(@Param("id") Integer id);
}