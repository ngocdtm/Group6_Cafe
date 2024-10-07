package com.coffee.repository;

import com.coffee.entity.Category;
import com.coffee.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> getAllCategory();

    Category findByNameCategory(@Param("name") String name);
}