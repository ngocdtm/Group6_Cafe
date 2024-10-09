package com.coffee.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;

@NamedQuery(name = "Category.getAllCategory", query = "SELECT c FROM Category c WHERE c.id IN (SELECT p.category FROM Product p WHERE p.status='true')")
@NamedQuery(name = "Category.findByNameCategory", query = "SELECT c FROM Category c WHERE c.name=:name")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "category")
public class Category implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)//nullable = false tức ko đc phép null thuộc tính này
    private String name;

}