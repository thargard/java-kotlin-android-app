package example.com.server.repository;

import example.com.server.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Получить все сообщения между двумя пользователями (в обе стороны)
     * Сортировка по времени создания (от старых к новым)
     */
    @Query("SELECT m FROM Message m " +
            "WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2) " +
            "   OR (m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findConversationBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    /**
     * Получить все сообщения пользователя (входящие и исходящие)
     * С JOIN FETCH для избежания N+1 проблемы
     */
    @Query("SELECT DISTINCT m FROM Message m " +
            "JOIN FETCH m.sender " +
            "JOIN FETCH m.receiver " +
            "WHERE m.sender.id = :userId OR m.receiver.id = :userId " +
            "ORDER BY m.createdAt DESC")
    List<Message> findAllUserMessages(@Param("userId") Long userId);

    /**
     * Получить непрочитанные сообщения для пользователя
     */
    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);

    /**
     * Получить количество непрочитанных сообщений от конкретного пользователя
     */
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.receiver.id = :receiverId " +
            "AND m.sender.id = :senderId " +
            "AND m.isRead = false")
    Long countUnreadMessagesBetweenUsers(
            @Param("receiverId") Long receiverId,
            @Param("senderId") Long senderId
    );

    /**
     * Получить последнее сообщение между двумя пользователями
     */
    @Query("SELECT m FROM Message m " +
            "WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2) " +
            "   OR (m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
            "ORDER BY m.createdAt DESC " +
            "LIMIT 1")
    Message findLastMessageBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    /**
     * Получить всех пользователей, с которыми есть переписка
     */
    @Query("SELECT DISTINCT CASE " +
            "  WHEN m.sender.id = :userId THEN m.receiver.id " +
            "  ELSE m.sender.id " +
            "END " +
            "FROM Message m " +
            "WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Long> findAllConversationPartners(@Param("userId") Long userId);
}