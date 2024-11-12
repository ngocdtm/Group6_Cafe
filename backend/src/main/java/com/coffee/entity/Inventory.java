package com.coffee.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Entity
@Table(name = "inventory", indexes = {
        @Index(name = "idx_product_id", columnList = "product_id")
})
@Data
@DynamicUpdate
@DynamicInsert
public class Inventory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "min_quantity")
    private Integer minQuantity;

    @Column(name = "max_quantity")
    private Integer maxQuantity;

    @Column(name = "last_updated", columnDefinition = "DATETIME")
    private LocalDateTime lastUpdated;

    public String getFormattedLastUpdated() {
        if (lastUpdated != null) {
            return lastUpdated.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        }
        return null;
    }
}
