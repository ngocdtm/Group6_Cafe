package com.coffee.wrapper;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductHistoryWrapper {
    private Integer id;
    private String modifiedDate;
    private String modifiedBy;
    private String action;
    private String previousData;
    private String newData;
    private String details;
}