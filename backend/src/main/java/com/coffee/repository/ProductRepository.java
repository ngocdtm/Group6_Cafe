package com.coffee.repository;

import com.coffee.entity.Product;
import com.coffee.entity.ProductImage;
import com.coffee.wrapper.ProductWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<ProductWrapper> getAllProduct();

    Optional<Product> findByNameProduct(@Param("name") String name);

    @Transactional
    @Modifying
    Integer updateProductStatus(@Param("status") String status, @Param("id") Integer id);

    List<ProductWrapper> getProductByCategory(@Param("id") Integer id);

    ProductWrapper getProductById(@Param("id") Integer id);

    @Query("SELECT new com.coffee.wrapper.ProductWrapper(p.id, p.name, p.description, p.price, p.originalPrice, p.status,p.category.id, p.category.name) " +
            "FROM Product p WHERE p.deleted = 'false' AND p.status != 'false' AND LOWER(p.name) LIKE LOWER(concat('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(concat('%', :keyword, '%'))")
    List<ProductWrapper> searchProducts(String keyword);

    @Query(name = "Product.getRelatedProducts")
    List<ProductWrapper> getRelatedProducts(
            @Param("categoryId") Integer categoryId,
            @Param("productId") Integer productId,
            @Param("price") Integer price,
            @Param("priceRange") Integer priceRange,
            @Param("limit") Integer limit
    );

    @Query("SELECT new com.coffee.wrapper.ProductWrapper(p.id, p.name, p.description, " +
            "p.price, p.originalPrice, p.status, p.category.id, p.category.name) " +
            "FROM Product p WHERE p.deleted = 'false' ORDER BY p.id DESC")
    List<ProductWrapper> findActiveProducts();

    @Query("SELECT new com.coffee.wrapper.ProductWrapper(p.id, p.name, p.description, " +
            "p.price, p.originalPrice, p.status, p.category.id, p.category.name) " +
            "FROM Product p WHERE p.deleted = 'true' ORDER BY p.id DESC")
    List<ProductWrapper> findDeletedProducts();
}