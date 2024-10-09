package com.coffee.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NamedQuery(name = "Product.getAllProduct",
        query = "SELECT new com.coffee.wrapper.ProductWrapper(p.id, p.name, p.description, p.price, p.status, p.category.id, p.category.name) FROM Product p")

@NamedQuery(name = "Product.updateProductStatus",
        query = "UPDATE Product p SET p.status=:status WHERE p.id=:id")

@NamedQuery(name = "Product.getProductByCategory",
        query = "SELECT new com.coffee.wrapper.ProductWrapper(p.id, p.name, p.description, p.price, p.status, p.category.id, p.category.name) " +
                "FROM Product p WHERE p.category.id=:id AND p.status='true'")

@NamedQuery(name = "Product.getProductById",
        query = "SELECT new com.coffee.wrapper.ProductWrapper(p.id, p.name, p.description, p.price, p.status, p.category.id, p.category.name) " +
                "FROM Product p WHERE p.id=:id")

@NamedQuery(name = "Product.findByNameProduct", query = "SELECT p FROM Product p WHERE p.name=:name")

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "product")
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name",nullable = false, length = 350)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_fk", nullable = false)
    private Category category;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private Integer price;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();
}