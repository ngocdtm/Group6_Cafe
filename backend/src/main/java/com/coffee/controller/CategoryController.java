package com.coffee.controller;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.Category;
import com.coffee.service.CategoryService;
import com.coffee.utils.CafeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @Operation(
            summary = "Add a new category",
            description = "Endpoint to add a new category."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/add")
    public ResponseEntity<String> addCategory(@RequestBody(required = true) Map<String, String> requestMap){
        try{
            return categoryService.addCategory(requestMap);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Get all categories",
            description = "Endpoint to get all categories."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/get")
    public ResponseEntity<List<Category>> getAllCategory(@RequestParam(required = false) String filterValue){
        try{
            return categoryService.getAllCategory(filterValue);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Update category",
            description = "Endpoint to update a category."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/update")
    public ResponseEntity<String> updateCategory(@RequestBody(required = true) Map<String, String> requestMap){
        try{
            return categoryService.updateCategory(requestMap);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Delete a category",
            description = "Endpoint to delete a category."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Integer id){
        try{
            return categoryService.deleteCategory(id);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}