package example.com.server.service;

import example.com.server.dto.MessageDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyUsers(MessageDTO message) {
        if (message == null || message.getReceiverId() == null) {
            return;
        }
        String receiverId = message.getReceiverId().toString();
        messagingTemplate.convertAndSendToUser(receiverId, "/queue/messages", message);
        messagingTemplate.convertAndSend("/topic/messages/" + receiverId, message);

        if (message.getSenderId() != null
                && !message.getSenderId().equals(message.getReceiverId())) {
            String senderId = message.getSenderId().toString();
            messagingTemplate.convertAndSendToUser(senderId, "/queue/messages", message);
            messagingTemplate.convertAndSend("/topic/messages/" + senderId, message);
        }
    }
}
