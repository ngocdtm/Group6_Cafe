package com.coffee.wrapper;


import com.coffee.enums.UserRole;
import com.coffee.enums.UserStatus;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class UserWrapper {


    private Integer id;
    private String name;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private String address;
    private Integer loyaltyPoints;
    private String avatar;
    private UserRole role;


    public UserWrapper(Integer id, String name, String email, String phoneNumber, UserStatus status, String address, Integer loyaltyPoints, String avatar, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
        this.avatar = avatar;
        this.role = role;
    }
}


