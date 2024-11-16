package com.coffee.controller;

import com.coffee.entity.Message;
import com.coffee.entity.User;
import com.coffee.repository.UserRepository;
import com.coffee.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    // API lấy danh sách users
    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    // API gửi tin nhắn
    @PostMapping("/chat/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        Message savedMessage = chatService.saveMessage(
                message.getSender(),
                message.getReceiver(),
                message.getContent()
        );

        // Gửi tin nhắn realtime qua WebSocket
        messagingTemplate.convertAndSendToUser(
                message.getReceiver().getId().toString(),
                "/queue/messages",
                savedMessage
        );

        return ResponseEntity.ok(savedMessage);
    }

    // API lấy cuộc hội thoại giữa 2 users
    @GetMapping("/chat/conversation/{userId1}/{userId2}")
    public List<Message> getConversation(
            @PathVariable Integer userId1,
            @PathVariable Integer userId2) {
        User user1 = userRepository.findById(userId1).orElseThrow();
        User user2 = userRepository.findById(userId2).orElseThrow();
        return chatService.getConversation(user1, user2);
    }

    // WebSocket endpoint để xử lý tin nhắn realtime
    @MessageMapping("/chat")
    public void processMessage(@Payload Message chatMessage) {
        Message saved = chatService.saveMessage(
                chatMessage.getSender(),
                chatMessage.getReceiver(),
                chatMessage.getContent()
        );
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiver().getId().toString(),
                "/queue/messages",
                saved
        );
    }
}