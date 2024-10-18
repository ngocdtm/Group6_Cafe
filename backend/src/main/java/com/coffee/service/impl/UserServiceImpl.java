package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.User;
import com.coffee.repository.UserRepository;
import com.coffee.security.CustomUserDetailsService;
import com.coffee.security.JwtRequestFilter;
import com.coffee.security.JwtUtil;
import com.coffee.service.UserService;
import com.coffee.utils.CafeUtils;
import com.coffee.utils.EmailUtils;
import com.coffee.wrapper.UserWrapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Autowired
    EmailUtils emailUtils;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside signUp {}", requestMap);
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userRepository.findByEmail(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    String passwordStrengthMessage = isPasswordStrong(requestMap.get("password"));
                    if (passwordStrengthMessage.equals("OK")) {
                        userRepository.save(getUserFromMap(requestMap));
                        return CafeUtils.getResponseEntity("Successfully Registered", HttpStatus.OK);
                    } else {
                        return CafeUtils.getResponseEntity(passwordStrengthMessage, HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return CafeUtils.getResponseEntity(CafeConstants.EMAIL_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String isPasswordStrong(String password) {
        if (password.length() < 8) {
            return "Mật khẩu không đủ 8 ký tự!";
        }

        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = !password.matches("[A-Za-z0-9 ]*");

        if (!hasUppercase || !hasLowercase || !hasDigit || !hasSpecialChar) {
            return "Mật khẩu không đủ mạnh (cần ít nhất 1 chữ hoa, 1 kí tự đặc biệt và 1 con số)";
        }

        return "OK";
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        return requestMap.containsKey("name") && requestMap.containsKey("email")
                && requestMap.containsKey("phoneNumber") && requestMap.containsKey("password")
                && requestMap.containsKey("address");
    }

    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setEmail(requestMap.get("email"));
        user.setPhoneNumber(requestMap.get("phoneNumber"));
        user.setPassword(passwordEncoder.encode(requestMap.get("password")));
        user.setStatus("true");
        user.setRole(requestMap.getOrDefault("role", "customer"));
        user.setAddress(requestMap.getOrDefault("address", ""));
        user.setLoyaltyPoints(0);
        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );
            if (auth.isAuthenticated()) {
                User userDetails = customUserDetailsService.getUserDetail();
                if (userDetails.getStatus().equalsIgnoreCase("true")) {
                    String token = jwtUtil.generateToken(userDetails.getEmail(), userDetails.getRole());
                    return ResponseEntity.ok()
                            .body("{\"token\":\"" + token + "\", \"role\":\"" + userDetails.getRole() + "\"}");
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"message\":\"Account is not active. Wait for admin approval.\"}");
                }
            }
        }  catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\":\"Invalid username or password\"}");
        } catch (Exception ex) {
            log.error("Error in authentication process", ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\":\"Something went wrong\"}");
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            if (jwtRequestFilter.isAdmin()) {
                return new ResponseEntity<>(userRepository.getAllUser(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllCustomers() {
        try {
            if (jwtRequestFilter.isAdmin()) {
                return new ResponseEntity<>(userRepository.getAllCustomers(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (jwtRequestFilter.isAdmin()) {
                Optional<User> optional = userRepository.findById(Integer.parseInt(requestMap.get("id")));
                if (optional.isPresent()) {
                    userRepository.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userRepository.getAllAdmin());
                    return CafeUtils.getResponseEntity("User Status updated successfully!", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("user id does not exist!", HttpStatus.OK);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCustomer(Map<String, String> requestMap) {
        try {
            if (jwtRequestFilter.isAdmin() || isOwnAccount(requestMap.get("id"))) {
                Optional<User> optional = userRepository.findById(Integer.parseInt(requestMap.get("id")));
                if (!optional.isPresent()) {
                    return CafeUtils.getResponseEntity("User id does not exist", HttpStatus.NOT_FOUND);
                }
                User user = optional.get();
                if (requestMap.containsKey("name")) user.setName(requestMap.get("name"));
                if (requestMap.containsKey("email")) user.setEmail(requestMap.get("email"));
                if (requestMap.containsKey("phoneNumber")) user.setPhoneNumber(requestMap.get("phoneNumber"));
                if (requestMap.containsKey("address")) user.setAddress(requestMap.get("address"));
                if (requestMap.containsKey("password")) {
                    user.setPassword(passwordEncoder.encode(requestMap.get("password")));
                }
                userRepository.save(user);
                return CafeUtils.getResponseEntity("Customer Updated Successfully", HttpStatus.OK);
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean isOwnAccount(String userId) {
        String currentUserEmail = jwtRequestFilter.getCurrentUser();
        User currentUser = userRepository.findByEmail(currentUserEmail);
        return currentUser != null && currentUser.getId().toString().equals(userId);
    }

    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtRequestFilter.getCurrentUser());
        if (status != null && status.equalsIgnoreCase("true")) {
            emailUtils.sendMessage(jwtRequestFilter.getCurrentUser(), "Account Approved", "User: " + user +
                    "\n is approved by \nAdmin: " + jwtRequestFilter.getCurrentUser(), allAdmin);
        } else {
            emailUtils.sendMessage(jwtRequestFilter.getCurrentUser(), "Account Disabled", "User: " + user +
                    "\n is Disabled by \nAdmin: " + jwtRequestFilter.getCurrentUser(), allAdmin);


        }
    }

    @Override
    public ResponseEntity<String> checkToken() {
        try {
            String currentUser = jwtRequestFilter.getCurrentUser();
            if (currentUser != null) {
                return ResponseEntity.ok("{\"valid\": true, \"user\": \"" + currentUser + "\"}");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"valid\": false}");
            }
        } catch (Exception ex) {
            log.error("Error checking token", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"Error checking token\"}");
        }
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            User user = userRepository.findByEmail(jwtRequestFilter.getCurrentUser());
            if (user != null) {
                if (passwordEncoder.matches(requestMap.get("oldPassword"), user.getPassword())) {
                    String passwordStrengthMessage = isPasswordStrong(requestMap.get("newPassword"));
                    if (passwordStrengthMessage.equals("OK")) {
                        user.setPassword(passwordEncoder.encode(requestMap.get("newPassword")));
                        userRepository.save(user);
                        return CafeUtils.getResponseEntity("Password updated successfully!", HttpStatus.OK);
                    } else {
                        return CafeUtils.getResponseEntity(passwordStrengthMessage, HttpStatus.BAD_REQUEST);
                    }
                }
                return CafeUtils.getResponseEntity("Incorrect Old Password", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            User user = userRepository.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())) {
                emailUtils.forgotMail(user.getEmail(), "Credentials by Cafe Management System", user.getPassword());
                return CafeUtils.getResponseEntity("Check your mail for credentials", HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Email not found!", HttpStatus.NOT_FOUND);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

