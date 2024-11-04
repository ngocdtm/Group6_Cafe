package com.coffee.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_history")
@Data
public class ProductHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "modified_date", columnDefinition = "DATETIME")
    private LocalDateTime modifiedDate;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "action")
    private String action; // CREATE, UPDATE, DELETE, RESTORE, ADD_IMAGES, DELETE_IMAGES, STATUS_CHANGE

    @Column(name = "previous_data", columnDefinition = "TEXT")
    private String previousData;

    @Column(name = "new_data", columnDefinition = "TEXT")
    private String newData;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // Additional details about the change
}