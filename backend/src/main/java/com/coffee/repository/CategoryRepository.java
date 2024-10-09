package com.coffee.repository;

import com.coffee.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> getAllCategory();

    Optional<Category> findByNameCategory(@Param("name") String name);
}