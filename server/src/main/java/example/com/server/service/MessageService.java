package example.com.server.service;

import example.com.server.dto.ConversationDTO;
import example.com.server.dto.MessageDTO;
import example.com.server.model.Message;
import example.com.server.model.User;
import example.com.server.repository.MessageRepository;
import example.com.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    /**
     * Отправить сообщение от одного пользователя другому
     */
    @Transactional
    public MessageDTO sendMessage(Long senderId, Long receiverId, String content) {
        // Проверяем, что пользователи существуют
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + senderId));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found: " + receiverId));

        // Проверяем, что пользователь не отправляет сообщение сам себе
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send message to yourself");
        }

        // Создаем и сохраняем сообщение
        Message message = new Message(sender, receiver, content);
        message = messageRepository.save(message);

        MessageDTO dto = MessageDTO.fromEntity(message);
        webSocketNotificationService.notifyUsers(dto);
        return dto;
    }

    /**
     * Получить переписку между двумя пользователями
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getConversation(Long userId1, Long userId2) {
        List<Message> messages = messageRepository.findConversationBetweenUsers(userId1, userId2);
        return messages.stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Получить список всех диалогов для пользователя
     * Группирует сообщения по собеседникам
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(Long userId) {
        // Получаем все сообщения пользователя
        List<Message> allMessages = messageRepository.findAllUserMessages(userId);

        // Группируем сообщения по собеседникам
        Map<Long, List<Message>> conversationsByUser = new HashMap<>();

        for (Message message : allMessages) {
            Long otherUserId = message.getSender().getId().equals(userId)
                    ? message.getReceiver().getId()
                    : message.getSender().getId();

            conversationsByUser.computeIfAbsent(otherUserId, k -> new ArrayList<>())
                    .add(message);
        }

        // Создаем DTO для каждого диалога
        List<ConversationDTO> conversations = new ArrayList<>();

        for (Map.Entry<Long, List<Message>> entry : conversationsByUser.entrySet()) {
            Long otherUserId = entry.getKey();
            List<Message> messages = entry.getValue();

            // Сортируем сообщения по времени (последнее - первое)
            messages.sort((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()));

            Message lastMessage = messages.get(0);

            // Подсчитываем непрочитанные сообщения от собеседника
            long unreadCount = messages.stream()
                    .filter(m -> m.getReceiver().getId().equals(userId) && !m.getIsRead())
                    .count();

            // Определяем имя собеседника
            User otherUser = lastMessage.getSender().getId().equals(userId)
                    ? lastMessage.getReceiver()
                    : lastMessage.getSender();

            String otherUserName = getUserDisplayName(otherUser);

            boolean isLastMessageFromMe = lastMessage.getSender().getId().equals(userId);

            ConversationDTO conversation = new ConversationDTO(
                    otherUserId,
                    otherUserName,
                    lastMessage.getContent(),
                    lastMessage.getCreatedAt(),
                    unreadCount,
                    isLastMessageFromMe
            );

            conversations.add(conversation);
        }

        // Сортируем диалоги по времени последнего сообщения
        conversations.sort((c1, c2) -> c2.getLastMessageAt().compareTo(c1.getLastMessageAt()));

        return conversations;
    }

    /**
     * Отметить сообщение как прочитанное
     */
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        // Проверяем, что пользователь является получателем
        if (!message.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only mark your own messages as read");
        }

        message.setIsRead(true);
        messageRepository.save(message);
    }

    /**
     * Отметить все сообщения от конкретного пользователя как прочитанные
     */
    @Transactional
    public void markConversationAsRead(Long currentUserId, Long otherUserId) {
        List<Message> messages = messageRepository.findConversationBetweenUsers(currentUserId, otherUserId);

        for (Message message : messages) {
            // Отмечаем только те сообщения, где текущий пользователь - получатель
            if (message.getReceiver().getId().equals(currentUserId) && !message.getIsRead()) {
                message.setIsRead(true);
            }
        }

        messageRepository.saveAll(messages);
    }

    /**
     * Получить количество непрочитанных сообщений для пользователя
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndIsReadFalse(userId);
        return (long) unreadMessages.size();
    }

    /**
     * Получить количество непрочитанных сообщений от конкретного пользователя
     */
    @Transactional(readOnly = true)
    public Long getUnreadCountFromUser(Long receiverId, Long senderId) {
        return messageRepository.countUnreadMessagesBetweenUsers(receiverId, senderId);
    }

    /**
     * Получить отображаемое имя пользователя
     */
    private String getUserDisplayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        if (user.getLogin() != null && !user.getLogin().isBlank()) {
            return user.getLogin();
        }
        return "User " + user.getId();
    }
}
