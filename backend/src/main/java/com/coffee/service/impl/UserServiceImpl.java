package com.coffee.service.impl;


import com.coffee.constants.CafeConstants;
import com.coffee.entity.User;
import com.coffee.enums.UserRole;
import com.coffee.enums.UserStatus;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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


    @Value("${app.file.avatar-dir}")
    private String avatarDir;


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
        // Set initial status as INACTIVE for new users pending admin approval
        user.setStatus(UserStatus.INACTIVE);


        // Convert string role to enum, default to CUSTOMER if not specified
        String roleStr = requestMap.getOrDefault("role", "CUSTOMER");
        user.setRole(UserRole.valueOf(roleStr.toUpperCase()));


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


                // Check if user status is ACTIVE
                if (UserStatus.ACTIVE.equals(userDetails.getStatus())) {
                    String token = jwtUtil.generateToken(userDetails.getEmail(), userDetails.getRole());
                    return ResponseEntity.ok()
                            .body(String.format(
                                    "{\"token\":\"%s\", \"role\":\"%s\", \"name\":\"%s\", \"id\":%d, \"status\":\"%s\"}",
                                    token,
                                    userDetails.getRole(),
                                    userDetails.getName(),
                                    userDetails.getId(),
                                    userDetails.getStatus()
                            ));
                }


                // Handle different status cases
                String message = switch (userDetails.getStatus()) {
                    case INACTIVE -> "Account is not active. Wait for admin approval.";
                    case SUSPENDED -> "Account has been suspended. Please contact administrator.";
                    default -> "Account is not accessible. Please contact administrator.";
                };


                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(String.format("{\"message\":\"%s\", \"status\":\"%s\"}",
                                message, userDetails.getStatus()));
            }
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\":\"Invalid username or password\"}");
        } catch (Exception e) {
            log.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"Something went wrong\"}");
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
    public ResponseEntity<UserWrapper> getProfile() {
        try {
            String email = jwtRequestFilter.getCurrentUser();
            User user = userRepository.findByEmail(email);


            if (user != null) {
                UserWrapper profile = new UserWrapper(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getStatus(),
                        user.getAddress(),
                        user.getLoyaltyPoints(),
                        user.getAvatar(),
                        user.getRole()
                );
                return new ResponseEntity<>(profile, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<String> updateAvatar(MultipartFile file) {
        try {
            // Validate authentication
            String currentUser = jwtRequestFilter.getCurrentUser();
            if (currentUser == null) {
                return CafeUtils.getResponseEntity("Unauthorized access", HttpStatus.UNAUTHORIZED);
            }


            if (file.isEmpty()) {
                return CafeUtils.getResponseEntity("Please select a file", HttpStatus.BAD_REQUEST);
            }


            // Validate file type
            String contentType = file.getContentType();
            if (!isValidImageType(contentType)) {
                return CafeUtils.getResponseEntity("Only image files are allowed", HttpStatus.BAD_REQUEST);
            }


            // Get current user
            User user = userRepository.findByEmail(currentUser);
            if (user == null) {
                return CafeUtils.getResponseEntity("User not found", HttpStatus.NOT_FOUND);
            }


            // Create avatar directory if it doesn't exist
            Path uploadPath = Paths.get(avatarDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);


            // Generate unique filename
            String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
            Path targetLocation = uploadPath.resolve(fileName);


            // Delete old avatar if exists
            if (user.getAvatar() != null) {
                Path oldAvatarPath = uploadPath.resolve(user.getAvatar());
                Files.deleteIfExists(oldAvatarPath);
            }


            // Save new avatar
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);


            // Update user avatar URL in database
            String avatar = fileName;
            user.setAvatar(avatar);
            userRepository.save(user);


            // Return consistent response format
            return ResponseEntity.ok()
                    .body(String.format("{\"message\":\"Avatar updated successfully\",\"avatar\":\"%s\"}", avatar));
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<List<UserWrapper>> getAllEmployees() {
        try {
            if (jwtRequestFilter.isAdmin() || jwtRequestFilter.isEmployee()) {
                return new ResponseEntity<>(userRepository.getAllEmployees(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif")
        );
    }


    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (jwtRequestFilter.isAdmin()) {
                Optional<User> optional = userRepository.findById(Integer.parseInt(requestMap.get("id")));
                if (optional.isPresent()) {
                    User user = optional.get();
                    // Convert string to UserStatus enum
                    UserStatus newStatus = UserStatus.valueOf(requestMap.get("status").toUpperCase());
                    user.setStatus(newStatus);
                    userRepository.save(user);


                    // Update email notification logic
                    sendMailToAllAdmin(newStatus, user.getEmail(), userRepository.getAllAdmin());
                    return CafeUtils.getResponseEntity("User Status updated successfully!", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("User id does not exist!", HttpStatus.OK);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (IllegalArgumentException e) {
            return CafeUtils.getResponseEntity("Invalid status value", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<String> updateCustomer(Map<String, String> requestMap) {
        try {
            if (jwtRequestFilter.isAdmin() || jwtRequestFilter.isCustomer()|| isOwnAccount(requestMap.get("id"))) {
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


    private void sendMailToAllAdmin(UserStatus status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtRequestFilter.getCurrentUser());
        switch (status) {
            case ACTIVE:
                emailUtils.sendMessage(jwtRequestFilter.getCurrentUser(),
                        "Account Activated",
                        "User: " + user + "\n is activated by \nAdmin: " + jwtRequestFilter.getCurrentUser(),
                        allAdmin);
                break;
            case INACTIVE:
                emailUtils.sendMessage(jwtRequestFilter.getCurrentUser(),
                        "Account Deactivated",
                        "User: " + user + "\n is deactivated by \nAdmin: " + jwtRequestFilter.getCurrentUser(),
                        allAdmin);
                break;
            case SUSPENDED:
                emailUtils.sendMessage(jwtRequestFilter.getCurrentUser(),
                        "Account Suspended",
                        "User: " + user + "\n is suspended by \nAdmin: " + jwtRequestFilter.getCurrentUser(),
                        allAdmin);
                break;
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
            if (user != null && !Strings.isNullOrEmpty(user.getEmail())) {
                // Generate a random temporary password
                String temporaryPassword = generateTemporaryPassword();


                // Update user's password in database
                user.setPassword(passwordEncoder.encode(temporaryPassword));
                userRepository.save(user);


                // Send email with temporary password
                emailUtils.sendPasswordResetEmail(user.getEmail(), temporaryPassword);


                return CafeUtils.getResponseEntity("Password reset instructions sent to your email", HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Email not found!", HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            log.error("Error in forgotPassword", ex);
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private String generateTemporaryPassword() {
        // Generate a random 12-character password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}



