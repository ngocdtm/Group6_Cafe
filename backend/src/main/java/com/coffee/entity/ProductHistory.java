package com.coffee.entity;

import com.coffee.wrapper.ProductImageWrapper;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JpaAttributeTypeInspection")
@Entity
@Table(name = "product_history")
@Data
@DynamicUpdate
@DynamicInsert
public class ProductHistory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "viewed_at", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime viewedAt;

    public ProductHistory() {
    }
    public ProductHistory(User user, Product product) {
        this.user = user;
        this.product = product;
        this.viewedAt = LocalDateTime.now();
    }
}