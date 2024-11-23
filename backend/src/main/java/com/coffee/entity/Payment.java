package com.coffee.entity;
import lombok.Data;
@Data
public class Payment {
    private String orderId;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private String couponCode;
    private Integer discount = 0;  // Khởi tạo giá trị mặc định
    private Integer total = 0;     // Khởi tạo giá trị mặc định
    private Integer totalAfterDiscount = 0;  // Khởi tạo giá trị mặc định
    private Long userId;
    private int amount;
    private String bankCode;
    private String orderInfo;
}