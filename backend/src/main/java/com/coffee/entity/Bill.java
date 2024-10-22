package com.coffee.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NamedQuery(name = "Bill.getAllBills", query = "SELECT b FROM Bill b")
@NamedQuery(name = "Bill.getBillByUserName", query = "SELECT b FROM Bill b WHERE b.createdBy=:name ORDER BY b.id DESC")
@NamedQuery(name = "Bill.existsByCouponCode", query = "SELECT CASE WHEN (COUNT(b) > 0) THEN true ELSE false END FROM Bill b WHERE b.code = :code")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "bill")
public class Bill implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "paymentMethod")
    private String paymentMethod;

    @Column(name = "total")
    private Integer total;

    @Column(name = "totalAfterDiscount")
    private Integer totalAfterDiscount;

    @Column(name = "discount")
    private Integer discount;

//    @Column(name = "productDetails", columnDefinition = "TEXT")
//    private String productDetails;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "code")
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "status")
    private String status;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillItem> billItems = new ArrayList<>();
}
