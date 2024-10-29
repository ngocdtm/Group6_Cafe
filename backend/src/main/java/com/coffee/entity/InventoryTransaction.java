package com.coffee.entity;

import com.coffee.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "inventory_transaction", indexes = {
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_transaction_date", columnList = "transaction_date")
})
@Data
@DynamicUpdate
@DynamicInsert
public class InventoryTransaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "transaction_type")
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // IMPORT, EXPORT

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "note")
    private String note;

    @Column(name = "transaction_date")
    private Date transactionDate;

    @Column(name = "created_by")
    private String createdBy;
}
