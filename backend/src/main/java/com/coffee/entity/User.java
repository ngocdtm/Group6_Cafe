
package com.coffee.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import java.io.Serial;
import java.io.Serializable;

@NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email=:email")
@NamedQuery(name = "User.getAllUser",
        query = "SELECT new com.coffee.wrapper.UserWrapper(u.id,u.name,u.email,u.phoneNumber,u.status) from User u WHERE u.role='user'")
@NamedQuery(name = "User.getAllAdmin",
        query = "SELECT u.email from User u WHERE u.role='admin'")

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

    @Column(name = "status")
    private String status;

    @Column(name = "role")
    private String role;
}