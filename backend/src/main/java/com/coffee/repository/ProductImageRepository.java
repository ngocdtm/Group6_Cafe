package com.coffee.repository;

import com.coffee.entity.Product;
import com.coffee.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository <ProductImage, Integer> {

    List<ProductImage> findByProduct(Product product);
}
