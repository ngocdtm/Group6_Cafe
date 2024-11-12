package com.coffee.entity;
import lombok.Data;
@Data
public class Payment {
    private String orderId;
    private int amount;
    private String bankCode;
    private String orderInfo;
}
