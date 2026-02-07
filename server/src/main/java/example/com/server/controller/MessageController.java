package example.com.server.controller;

import example.com.server.model.Message;
import example.com.server.service.JwtService;
import example.com.server.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final JwtService jwtService;

    @Autowired
    public MessageController(MessageService messageService, JwtService jwtService) {
        this.messageService = messageService;
        this.jwtService = jwtService;
    }

    @GetMapping("/threads")
    public ResponseEntity<?> getUserConversations(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }
        Map<Long, List<Map<String, Object>>> threads = messageService.getThreadsForUser(userId);
        return ResponseEntity.ok(Map.of("threads", threads));
    }

    @Transactional(readOnly = true)
    @GetMapping("/thread/{threadId}")
    public ResponseEntity<?> getConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long threadId) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        List<Message> messages = messageService.getConversation(threadId);

        return ResponseEntity.ok(Map.of(
                "threadId", threadId,
                "messages", messages.stream().map(this::messageToMap).toList()
        ));
    }

    @PostMapping("/thread/{threadId}")
    public ResponseEntity<?> sendMessage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long threadId,
            @RequestBody Map<String, Object> body) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        String content = (String) body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message content is required"));
        }

        Long receiverId = null;
        if (body.get("receiverId") != null) {
            receiverId = Long.valueOf(body.get("receiverId").toString());
        }

        Long productId = null;
        if (body.get("productId") != null) {
            productId = Long.valueOf(body.get("productId").toString());
        }

        Message message = messageService.sendMessage(userId, receiverId, threadId, content, productId);

        return ResponseEntity.status(HttpStatus.CREATED).body(messageToMap(message));
    }

    @PostMapping("/start")
    public ResponseEntity<?> startConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> body) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String content = (String) body.get("content");
        Long productId = null;
        if (body.get("productId") != null) {
            productId = Long.valueOf(body.get("productId").toString());
        }

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message content is required"));
        }

        Message message = messageService.startConversation(userId, receiverId, productId, content);

        Map<String, Object> response = new HashMap<>(messageToMap(message));
        response.put("message", "Conversation started");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{messageId}/read")
    public ResponseEntity<?> markAsRead(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long messageId) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        messageService.markAsRead(messageId);

        return ResponseEntity.ok(Map.of("message", "Message marked as read"));
    }

    private Map<String, Object> messageToMap(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("senderId", message.getSender().getId());
        map.put("senderName", message.getSender().getFullName() != null ? message.getSender().getFullName() : message.getSender().getLogin());
        map.put("receiverId", message.getReceiver().getId());
        map.put("receiverName", message.getReceiver().getFullName() != null ? message.getReceiver().getFullName() : message.getReceiver().getLogin());
        map.put("content", message.getContent());
        map.put("threadId", message.getThreadId());
        map.put("productId", message.getProduct() != null ? message.getProduct().getId() : null);
        map.put("createdAt", message.getCreatedAt());
        map.put("isRead", message.getIsRead());
        return map;
    }
}
