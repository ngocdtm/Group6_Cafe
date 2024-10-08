package com.coffee.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@NamedQuery(name = "Coupon.getAllCoupon", query = "SELECT new com.coffee.wrapper.CouponWrapper(c.id,c.name,c.code,c.discount,c.expirationDate) FROM Coupon c")
@NamedQuery(name = "Coupon.getCouponById", query = "SELECT new com.coffee.wrapper.CouponWrapper(c.id,c.name,c.code,c.discount,c.expirationDate) FROM Coupon c WHERE c.id=:id")
@NamedQuery(name = "Coupon.findByNameCoupon", query = "SELECT c FROM Coupon c WHERE c.name=:name")
@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "coupon")
public class Coupon implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name",nullable = false, length = 350)
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "discount")
    private Long discount;

    @Column(name = "expirationDate")
    private LocalDate expirationDate;

}
