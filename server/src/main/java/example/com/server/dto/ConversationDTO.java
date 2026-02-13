package example.com.server.dto;

import java.time.Instant;

/**
 * DTO для отображения краткой информации о диалоге в списке разговоров
 */
public class ConversationDTO {

    private Long otherUserId;
    private String otherUserName;
    private String lastMessage;
    private Instant lastMessageAt;
    private Long unreadCount;
    private Boolean isLastMessageFromMe;

    public ConversationDTO() {
    }

    public ConversationDTO(Long otherUserId, String otherUserName, String lastMessage,
                           Instant lastMessageAt, Long unreadCount, Boolean isLastMessageFromMe) {
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
        this.isLastMessageFromMe = isLastMessageFromMe;
    }

    // Getters and Setters

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Boolean getIsLastMessageFromMe() {
        return isLastMessageFromMe;
    }

    public void setIsLastMessageFromMe(Boolean isLastMessageFromMe) {
        this.isLastMessageFromMe = isLastMessageFromMe;
    }
}