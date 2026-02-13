package example.com.server.controller;

import example.com.server.dto.ConversationDTO;
import example.com.server.dto.MessageDTO;
import example.com.server.dto.SendMessageRequest;
import example.com.server.service.JwtService;
import example.com.server.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*") // Настройте правильный CORS для продакшена
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private JwtService jwtService;

    /**
     * Получить список всех диалогов пользователя
     * GET /api/messages/conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> getUserConversations(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        List<ConversationDTO> conversations = messageService.getUserConversations(userId);
        return ResponseEntity.ok(Map.of("conversations", conversations));
    }

    /**
     * Получить переписку с конкретным пользователем
     * GET /api/messages/conversation/{otherUserId}
     */
    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<?> getConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long otherUserId) {

        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            List<MessageDTO> messages = messageService.getConversation(userId, otherUserId);
            return ResponseEntity.ok(Map.of(
                    "otherUserId", otherUserId,
                    "messages", messages
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Отправить сообщение пользователю
     * POST /api/messages/send
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody SendMessageRequest request) {

        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            MessageDTO message = messageService.sendMessage(
                    userId,
                    request.getReceiverId(),
                    request.getContent()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Отметить сообщение как прочитанное
     * PATCH /api/messages/{messageId}/read
     */
    @PatchMapping("/{messageId}/read")
    public ResponseEntity<?> markAsRead(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long messageId) {

        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            messageService.markAsRead(messageId, userId);
            return ResponseEntity.ok(Map.of("message", "Message marked as read"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Отметить весь диалог как прочитанный
     * POST /api/messages/conversation/{otherUserId}/read
     */
    @PostMapping("/conversation/{otherUserId}/read")
    public ResponseEntity<?> markConversationAsRead(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long otherUserId) {

        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            messageService.markConversationAsRead(userId, otherUserId);
            return ResponseEntity.ok(Map.of("message", "Conversation marked as read"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Получить количество непрочитанных сообщений
     * GET /api/messages/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        Long unreadCount = messageService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    /**
     * Получить количество непрочитанных сообщений от конкретного пользователя
     * GET /api/messages/unread/count/{otherUserId}
     */
    @GetMapping("/unread/count/{otherUserId}")
    public ResponseEntity<?> getUnreadCountFromUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long otherUserId) {

        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        Long unreadCount = messageService.getUnreadCountFromUser(userId, otherUserId);
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }
}