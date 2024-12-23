package com.coffee.entity;


import com.coffee.enums.UserRole;
import com.coffee.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email=:email")


@NamedQuery(name = "User.getAllUser",
        query = "SELECT new com.coffee.wrapper.UserWrapper(u.id,u.name,u.email,u.phoneNumber,u.status, u.address, u.loyaltyPoints, u.avatar, u.role) from User u WHERE u.role='CUSTOMER' OR u.role='EMPLOYEE'")


@NamedQuery(name = "User.getAllAdmin",
        query = "SELECT u.email from User u WHERE u.role='ADMIN'")


@NamedQuery(name = "User.getAllCustomers",
        query = "SELECT u.email from User u WHERE u.role='CUSTOMER'")


@NamedQuery(name = "User.updateStatus", query = "UPDATE User u SET u.status=:status WHERE u.id=:id")


@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "\"user\"")
public class User implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    @Column(name = "name")
    private String name;


    @Column(name = "phoneNumber")
    private String phoneNumber;


    @Column(name = "email")
    private String email;


    @Column(name = "password")
    private String password;


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status = UserStatus.ACTIVE; // Default value


    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;


    @Column(name = "address")
    private String address;


    @Column(name = "loyaltyPoints")
    private Integer loyaltyPoints;
    public User() {
    }


    public User(Integer id) {
        this.id = id;
    }


    @Column(name = "avatar")
    private String avatar;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("user")
    private List<ShoppingCart> shoppingCart;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bill> bills = new ArrayList<>();


}

