package com.coffee.service;

import com.coffee.wrapper.UserWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    ResponseEntity<String> signUp(Map<String, String> requestMap);

    ResponseEntity<String> login(Map<String, String> requestMap);

    ResponseEntity<List<UserWrapper>> getAllUser();

    ResponseEntity<String> update(Map<String, String> requestMap);

    ResponseEntity<String> checkToken();

    ResponseEntity<String> changePassword(Map<String, String> requestMap);

    ResponseEntity<String> forgotPassword(Map<String, String> requestMap);

    ResponseEntity<String> updateCustomer(Map<String, String> requestMap);

    ResponseEntity<List<UserWrapper>> getAllCustomers();

    ResponseEntity<UserWrapper> getProfile();

    ResponseEntity<String> updateAvatar(MultipartFile file);

    ResponseEntity<List<UserWrapper>> getAllEmployees();

}