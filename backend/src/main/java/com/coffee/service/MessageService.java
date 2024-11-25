package com.coffee.service;


import com.coffee.wrapper.MessageWrapper;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;


public interface MessageService {
    ResponseEntity<String> sendMessage(Map<String, String> requestMap);
    ResponseEntity<List<MessageWrapper>> getMessages(Integer withUserId);
    ResponseEntity<List<MessageWrapper>> getUnreadMessages();
    ResponseEntity<String> markAsRead(Integer messageId);
}



