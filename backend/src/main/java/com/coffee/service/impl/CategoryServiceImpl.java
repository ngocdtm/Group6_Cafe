package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.Category;
import com.coffee.repository.CategoryRepository;
import com.coffee.security.JwtRequestFilter;
import com.coffee.service.CategoryService;
import com.coffee.utils.CafeUtils;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Override
    public ResponseEntity<String> addCategory(Map<String, String> requestMap) {
        try{
            if(jwtRequestFilter.isAdmin()){
                if(validateCategoryMap(requestMap, false)){
                    Category category = categoryRepository.findByNameCategory(requestMap.get(CafeConstants.NAMECATEGORY));
                    if (Objects.isNull(category)) {
                    categoryRepository.save(getCategoryFromMap(requestMap, false));
                    return CafeUtils.getResponseEntity("Category added successfully", HttpStatus.OK);
                    }
                    else {
                        return CafeUtils.getResponseEntity(CafeConstants.NAMECATEGORY_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
                    }
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    private boolean validateCategoryMap(Map<String, String> requestMap, boolean validateId) {
        return requestMap.containsKey("name") && (requestMap.containsKey("id") || !validateId);
    }

    private Category getCategoryFromMap(Map<String,String> requestMap, boolean isAdd) {
        Category category = new Category();
        if(isAdd && requestMap.containsKey("id")) {
            category.setId(Integer.valueOf(requestMap.get("id")));
        }
        category.setName(requestMap.get("name"));
        return category;
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        try{
            if(!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")){
                log.info("Inside if filterVale = true");
                return new ResponseEntity<>(categoryRepository.getAllCategory(), HttpStatus.OK);
            }
            log.info("Inside if filterVale is not set");
            return new ResponseEntity<>(categoryRepository.findAll(), HttpStatus.OK);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        try{
            if(jwtRequestFilter.isAdmin()){
                if(validateCategoryMap(requestMap, true)){
                    Category categoryName = categoryRepository.findByNameCategory(requestMap.get(CafeConstants.NAMECATEGORY));
                    Optional<Category> optional = categoryRepository.findById(Integer.parseInt(requestMap.get("id")));
                    if(optional.isPresent()){
                        if (Objects.isNull(categoryName)) {
                        categoryRepository.save(getCategoryFromMap(requestMap, true));
                        return CafeUtils.getResponseEntity("Category updated successfully", HttpStatus.OK);
                        }
                        else {
                            return CafeUtils.getResponseEntity(CafeConstants.NAMECATEGORY_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
                        }
                    }else{
                        return CafeUtils.getResponseEntity("Category id does not exist", HttpStatus.OK);
                    }
                }
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}