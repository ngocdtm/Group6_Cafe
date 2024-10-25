package com.coffee.enums;

import lombok.Getter;

@Getter
public enum OrderType {
    ONLINE("Online order"),
    IN_STORE("In-store order");

    private final String description;

    OrderType(String description) {
        this.description = description;
    }
}
