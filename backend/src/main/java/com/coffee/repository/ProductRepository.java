

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


    @Query("SELECT p FROM ProductImage p WHERE p.product = :product")
    List<ProductImage> findByProduct(Product product);


    @Query("SELECT new com.coffee.wrapper.ProductWrapper(p.id, p.name, p.description, p.price, p.originalPrice, p.status,p.category.id, p.category.name) " +
            "FROM Product p WHERE LOWER(p.name) LIKE LOWER(concat('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(concat('%', :keyword, '%'))")
    List<ProductWrapper> searchProducts(String keyword);
}

