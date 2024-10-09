package com.coffee.wrapper;

import lombok.Data;

import java.time.LocalDate;
@Data
public class CouponWrapper {
    Long id;

    String name;

    String code;

    Long discount;

    LocalDate expirationDate;


    public CouponWrapper(){

    }

    public CouponWrapper(Long id, String name, String code, Long discount, LocalDate expirationDate){
        this.id = id;
        this.name = name;
        this.code = code;
        this.discount = discount;
        this.expirationDate = expirationDate;
    }
}
