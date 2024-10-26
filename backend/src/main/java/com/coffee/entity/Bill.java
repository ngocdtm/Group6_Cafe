package com.coffee.entity;


import com.coffee.enums.OrderStatus;
import com.coffee.enums.OrderType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@NamedQuery(name = "Bill.getAllBills", query = "SELECT b FROM Bill b")
@NamedQuery(name = "Bill.getBillByUserName", query = "SELECT b FROM Bill b WHERE b.createdByUser=:name ORDER BY b.id DESC")
@NamedQuery(name = "Bill.existsByCouponCode", query = "SELECT CASE WHEN (COUNT(b) > 0) THEN true ELSE false END FROM Bill b WHERE b.couponCode = :couponCode")

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

    // Thông tin khách hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Có thể null nếu là khách vãng lai

    private String customerName;
    private String customerPhone;
    private String shippingAddress;

    // Thông tin thanh toán
    private String paymentMethod;
    private Integer total;
    private Integer discount;
    private Integer totalAfterDiscount;
    private String couponCode;

    // Thông tin thời gian
    private LocalDateTime orderDate;
    private LocalDateTime lastUpdatedDate;

    // Add these methods to format dates
    public String getFormattedOrderDate() {
        if (orderDate != null) {
            return orderDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        }
        return null;
    }

    public String getFormattedLastUpdatedDate() {
        if (lastUpdatedDate != null) {
            return lastUpdatedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        }
        return null;
    }

    // Thông tin đặt mua
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)

    private OrderStatus orderStatus;
    private String createdByUser;


    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillItem> billItems = new ArrayList<>();
}
