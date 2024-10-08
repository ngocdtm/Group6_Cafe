package com.coffee.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserWrapper {

    private Integer id;

    private String name;

    private String email;

    private String phoneNumber;

    private String status;

    public UserWrapper(Integer id, String name, String email, String phoneNumber, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }
}