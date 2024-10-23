package com.coffee.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("Waiting for confirmation"),
    CONFIRMED("Order confirmed"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}