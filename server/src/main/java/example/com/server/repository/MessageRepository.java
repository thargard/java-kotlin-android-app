package example.com.server.repository;

import example.com.server.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByThreadIdOrderByCreatedAtAsc(Long threadId);

    @Query("SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.receiver WHERE m.sender.id = :userId OR m.receiver.id = :userId ORDER BY m.createdAt DESC")
    List<Message> findMessagesByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    List<Message> findByThreadId(Long threadId);

    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);
}
