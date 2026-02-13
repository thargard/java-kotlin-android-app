package example.com.server.dto;

import example.com.server.model.Message;
import java.time.Instant;

public class MessageDTO {

    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private Instant createdAt;
    private Boolean isRead;

    public MessageDTO() {
    }

    public MessageDTO(Long id, Long senderId, String senderName, Long receiverId,
                      String receiverName, String content, Instant createdAt, Boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    /**
     * Создать DTO из Entity Message
     */
    public static MessageDTO fromEntity(Message message) {
        return new MessageDTO(
                message.getId(),
                message.getSender().getId(),
                getUserDisplayName(message.getSender().getFullName(), message.getSender().getLogin()),
                message.getReceiver().getId(),
                getUserDisplayName(message.getReceiver().getFullName(), message.getReceiver().getLogin()),
                message.getContent(),
                message.getCreatedAt(),
                message.getIsRead()
        );
    }

    private static String getUserDisplayName(String fullName, String login) {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        if (login != null && !login.isBlank()) {
            return login;
        }
        return "Unknown User";
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}