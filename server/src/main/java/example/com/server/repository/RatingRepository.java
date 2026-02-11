package example.com.server.repository;

import example.com.server.model.Rating;
import example.com.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Найти рейтинг, который конкретный заказчик поставил конкретному исполнителю
    Optional<Rating> findByCustomerAndProducer(User customer, User producer);

    // Найти все рейтинги для конкретного исполнителя
    List<Rating> findByProducer(User producer);

    // Найти все рейтинги для конкретного исполнителя по ID
    List<Rating> findByProducerId(Long producerId);

    // Посчитать средний рейтинг исполнителя
    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.producer.id = :producerId")
    Double calculateAverageRating(@Param("producerId") Long producerId);

    // Посчитать количество оценок для исполнителя
    Long countByProducerId(Long producerId);

    // Посчитать количество оценок по каждой звезде
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.producer.id = :producerId AND r.ratingValue = :value")
    Long countByProducerIdAndRatingValue(@Param("producerId") Long producerId, @Param("value") Integer value);

    // Проверить, поставил ли заказчик оценку исполнителю
    boolean existsByCustomerIdAndProducerId(Long customerId, Long producerId);
}