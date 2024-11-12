package com.coffee.repository;

import com.coffee.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ProductImageRepository extends JpaRepository <ProductImage, Integer> {

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId AND pi.deleted = 'false'")
    List<ProductImage> findActiveImagesByProductId(@Param("productId") Integer productId);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId AND pi.deleted = 'true'")
    List<ProductImage> findDeletedImagesByProductId(@Param("productId") Integer productId);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId")
    List<ProductImage> findAllImagesByProductId(@Param("productId") Integer productId);
}
