package com.coffee.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "inventory_snapshot", indexes = {
        @Index(name = "idx_snapshot_date", columnList = "snapshot_date"),
        @Index(name = "idx_product_id_snapshot_date", columnList = "product_id,snapshot_date")
})
@Data
@DynamicUpdate
@DynamicInsert
public class InventorySnapshot implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;


    @Column(name = "quantity", nullable = false)
    private Integer quantity;


    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

