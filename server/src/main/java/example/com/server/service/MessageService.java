package example.com.server.service;

import example.com.server.model.Message;
import example.com.server.model.Product;
import example.com.server.model.User;
import example.com.server.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    // Simple thread ID generator (in production, you'd use a proper database sequence or UUID)
    private static final AtomicLong threadIdGenerator = new AtomicLong(System.currentTimeMillis());

    public Message startConversation(Long senderId, Long receiverId, Long productId, String initialMessage) {
        User sender = userService.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + senderId));

        User receiver = userService.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found: " + receiverId));

        Product product = null;
        if (productId != null) {
            product = productService.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        }

        // Generate a new thread ID
        Long threadId = threadIdGenerator.incrementAndGet();

        Message message = new Message(sender, receiver, initialMessage, product);
        message.setThreadId(threadId);

        return messageRepository.save(message);
    }

    public Message sendMessage(Long senderId, Long receiverId, Long threadId, String content, Long productId) {
        User sender = userService.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + senderId));

        User receiver = userService.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found: " + receiverId));

        Product product = null;
        if (productId != null) {
            product = productService.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        }

        Message message = new Message(sender, receiver, content, product);
        message.setThreadId(threadId);

        return messageRepository.save(message);
    }

    public List<Message> getConversation(Long threadId) {
        return messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
    }

    public List<Message> getUserConversations(Long userId) {
        return messageRepository.findMessagesByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<Map<String, Object>>> getThreadsForUser(Long userId) {
        List<Message> messages = messageRepository.findMessagesByUserIdOrderByCreatedAtDesc(userId);
        Map<Long, List<Map<String, Object>>> threads = new HashMap<>();
        for (Message msg : messages) {
            threads.computeIfAbsent(msg.getThreadId(), k -> new ArrayList<>())
                    .add(messageToMap(msg));
        }
        return threads;
    }

    private Map<String, Object> messageToMap(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("senderId", message.getSender().getId());
        map.put("senderName", userNameOrFallback(message.getSender()));
        map.put("receiverId", message.getReceiver().getId());
        map.put("receiverName", userNameOrFallback(message.getReceiver()));
        map.put("content", message.getContent());
        map.put("threadId", message.getThreadId());
        map.put("productId", message.getProduct() != null ? message.getProduct().getId() : null);
        map.put("createdAt", message.getCreatedAt());
        map.put("isRead", message.getIsRead());
        return map;
    }

    private String userNameOrFallback(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        if (user.getLogin() != null && !user.getLogin().isBlank()) return user.getLogin();
        return "User " + user.getId();
    }

    public List<Message> findByThreadId(Long threadId) {
        return messageRepository.findByThreadId(threadId);
    }

    public void markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        message.setIsRead(true);
        messageRepository.save(message);
    }
}
