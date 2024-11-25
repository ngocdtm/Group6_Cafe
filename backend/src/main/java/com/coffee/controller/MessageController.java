package com.coffee.controller;


import com.coffee.service.MessageService;
import com.coffee.wrapper.MessageWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/message")
public class MessageController {


    @Autowired
    private MessageService messageService;


    @Operation(
            summary = "send",
            description = "send"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody Map<String, String> requestMap) {
        return messageService.sendMessage(requestMap);
    }

    @Operation(
            summary = "get",
            description = "get"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/get/{withUserId}")
    public ResponseEntity<List<MessageWrapper>> getMessages(@PathVariable Integer withUserId) {
        return messageService.getMessages(withUserId);
    }

    @Operation(
            summary = "unread",
            description = "unread"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/unread")
    public ResponseEntity<List<MessageWrapper>> getUnreadMessages() {
        return messageService.getUnreadMessages();
    }


    @Operation(
            summary = "read",
            description = "read"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/read/{messageId}")
    public ResponseEntity<String> markAsRead(@PathVariable Integer messageId) {
        return messageService.markAsRead(messageId);
    }

}



