package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.Message;
import com.coffee.entity.User;
import com.coffee.enums.UserRole;
import com.coffee.repository.MessageRepository;
import com.coffee.repository.UserRepository;
import com.coffee.security.JwtRequestFilter;
import com.coffee.service.MessageService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.MessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtRequestFilter jwtFilter;

    @Override
    public ResponseEntity<List<MessageWrapper>> getMessages(Integer withUserId) {
        try {
            String currentUsername = jwtFilter.getCurrentUser();
            if (currentUsername == null || currentUsername.trim().isEmpty()) {
                log.error("No username found in token");
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            User currentUser = userRepository.findByEmail(currentUsername);
            if (currentUser == null) {
                log.error("User not found for email: {}", currentUsername);
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            // Lấy thông tin user được yêu cầu xem tin nhắn
            User requestedUser = userRepository.findById(withUserId).orElse(null);
            if (requestedUser == null) {
                log.error("Requested user not found with ID: {}", withUserId);
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            List<MessageWrapper> messages;
            // Nếu người dùng hiện tại là CUSTOMER
            if (currentUser.getRole() == UserRole.CUSTOMER) {
                // Kiểm tra xem CUSTOMER có phải là người tham gia cuộc trò chuyện không
                if (!currentUser.getId().equals(withUserId) &&
                        !isParticipantInConversation(currentUser.getId(), withUserId)) {
                    log.error("CUSTOMER {} is not authorized to view messages with user {}",
                            currentUser.getId(), withUserId);
                    return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
                }
                // Chỉ lấy tin nhắn giữa CUSTOMER và người được chỉ định
                messages = messageRepository.getAllMessages(currentUser.getId(), withUserId)
                        .stream()
                        .filter(msg ->
                                (msg.getFromUserId().equals(currentUser.getId()) && msg.getToUserId().equals(withUserId)) ||
                                        (msg.getFromUserId().equals(withUserId) && msg.getToUserId().equals(currentUser.getId())))
                        .toList();
            }
            // Nếu người dùng hiện tại là ADMIN
            else if (currentUser.getRole() == UserRole.ADMIN) {
                // ADMIN có thể xem tất cả tin nhắn liên quan đến user được chỉ định
                messages = messageRepository.getAllMessagesByUserId(withUserId);
            }
            else {
                log.error("Unsupported role: {}", currentUser.getRole());
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
            }

            // Đánh dấu tin nhắn đã đọc nếu người dùng hiện tại là người nhận
            messages.stream()
                    .filter(msg -> !msg.getSeen() && msg.getToUserId().equals(currentUser.getId()))
                    .forEach(msg -> markAsRead(msg.getId()));

            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error in getMessages: ", ex);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isParticipantInConversation(Integer userId, Integer withUserId) {
        // Kiểm tra xem có tin nhắn nào giữa hai người dùng không
        List<MessageWrapper> messages = messageRepository.getAllMessages(userId, withUserId);
        return messages.stream()
                .anyMatch(msg ->
                        (msg.getFromUserId().equals(userId) && msg.getToUserId().equals(withUserId)) ||
                                (msg.getFromUserId().equals(withUserId) && msg.getToUserId().equals(userId)));
    }

    @Override
    public ResponseEntity<String> sendMessage(Map<String, String> requestMap) {
        try {
            if (!validateSendMessage(requestMap)) {
                log.error("Invalid request data: {}", requestMap);
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            String currentUsername = jwtFilter.getCurrentUser();
            log.info("Current username from token: {}", currentUsername);

            if (currentUsername == null || currentUsername.trim().isEmpty()) {
                log.error("No username found in token");
                return CafeUtils.getResponseEntity("Unauthorized: No user found in token", HttpStatus.UNAUTHORIZED);
            }

            User fromUser = userRepository.findByEmail(currentUsername);
            if (fromUser == null) {
                log.error("Sender user not found for email: {}", currentUsername);
                return CafeUtils.getResponseEntity("Sender user not found", HttpStatus.NOT_FOUND);
            }

            Integer toUserId = Integer.parseInt(requestMap.get("toUserId"));
            User toUser = userRepository.findById(toUserId).orElse(null);
            if (toUser == null) {
                log.error("Recipient user not found for ID: {}", toUserId);
                return CafeUtils.getResponseEntity("Recipient user not found", HttpStatus.NOT_FOUND);
            }

            Message message = new Message();
            message.setContent(requestMap.get("content"));
            message.setFromUser(fromUser);
            message.setToUser(toUser);
            message.setCreatedDate(new Date());
            message.setSeen(false);

            messageRepository.save(message);
            log.info("Message sent successfully from user {} to user {}", fromUser.getId(), toUserId);

            return CafeUtils.getResponseEntity("Message Sent Successfully:" + message.getId(), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error in sendMessage: ", ex);
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<MessageWrapper>> getUnreadMessages() {
        try {
            String currentUsername = jwtFilter.getCurrentUser();
            if (currentUsername == null || currentUsername.trim().isEmpty()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            User currentUser = userRepository.findByEmail(currentUsername);
            if (currentUser == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            List<MessageWrapper> unreadMessages = messageRepository.getUnreadMessages(currentUser.getId());
            return new ResponseEntity<>(unreadMessages, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error in getUnreadMessages: ", ex);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> markAsRead(Integer messageId) {
        try {
            Message message = messageRepository.findById(messageId).orElse(null);
            if (message != null) {
                message.setSeen(true);
                messageRepository.save(message);
                return CafeUtils.getResponseEntity("Message marked as read", HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Message not found", HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            log.error("Error in markAsRead: ", ex);
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateSendMessage(Map<String, String> requestMap) {
        return requestMap.containsKey("toUserId") &&
                requestMap.containsKey("content") &&
                requestMap.get("content") != null &&
                !requestMap.get("content").trim().isEmpty();
    }
}