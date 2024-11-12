package com.coffee.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "product_image")
//@Where(clause = "deleted = 'false'")
public class ProductImage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "image_path")
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "deleted")
    private String deleted = "false";

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @Column(name = "restored_date")
    private LocalDateTime restoredDate;

    @Version
    private Long version = 0L;

    public ProductImage() {
        this.version = 0L; // Initialize in constructor as well
    }

    public ProductImage(Integer id) {
        this.id = id;
        this.version = 0L;
    }
}