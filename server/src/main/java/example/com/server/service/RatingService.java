package example.com.server.service;

import example.com.server.dto.RatingRequestDTO;
import example.com.server.dto.RatingResponseDTO;
import example.com.server.dto.RatingStatsDTO;
import example.com.server.model.Rating;
import example.com.server.model.User;
import example.com.server.repository.RatingRepository;
import example.com.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Создать или обновить рейтинг
     */
    @Transactional
    public RatingResponseDTO createOrUpdateRating(Long customerId, RatingRequestDTO requestDTO) {
        // Найти заказчика
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        // Найти исполнителя
        User producer = userRepository.findById(requestDTO.getProducerId())
                .orElseThrow(() -> new RuntimeException("Producer not found with id: " + requestDTO.getProducerId()));

        // Проверить, что пользователь не ставит рейтинг сам себе
        if (customerId.equals(requestDTO.getProducerId())) {
            throw new RuntimeException("You cannot rate yourself");
        }

        // Проверить, существует ли уже рейтинг
        Optional<Rating> existingRating = ratingRepository.findByCustomerAndProducer(customer, producer);

        Rating rating;
        if (existingRating.isPresent()) {
            // Обновить существующий рейтинг
            rating = existingRating.get();
            rating.setRatingValue(requestDTO.getRatingValue());
        } else {
            // Создать новый рейтинг
            rating = new Rating(customer, producer, requestDTO.getRatingValue());
        }

        rating = ratingRepository.save(rating);
        return convertToResponseDTO(rating);
    }

    /**
     * Получить рейтинг, который заказчик поставил исполнителю
     */
    public Optional<RatingResponseDTO> getRatingByCustomerAndProducer(Long customerId, Long producerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        User producer = userRepository.findById(producerId)
                .orElseThrow(() -> new RuntimeException("Producer not found"));

        return ratingRepository.findByCustomerAndProducer(customer, producer)
                .map(this::convertToResponseDTO);
    }

    /**
     * Получить все рейтинги для исполнителя
     */
    public List<RatingResponseDTO> getRatingsByProducer(Long producerId) {
        return ratingRepository.findByProducerId(producerId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получить статистику рейтинга исполнителя
     */
    public RatingStatsDTO getProducerRatingStats(Long producerId) {
        // Проверить, что исполнитель существует
        userRepository.findById(producerId)
                .orElseThrow(() -> new RuntimeException("Producer not found with id: " + producerId));

        Double averageRating = ratingRepository.calculateAverageRating(producerId);
        Long totalRatings = ratingRepository.countByProducerId(producerId);

        RatingStatsDTO stats = new RatingStatsDTO(producerId, averageRating != null ? averageRating : 0.0, totalRatings);

        // Посчитать количество оценок по звездам
        stats.setFiveStars(ratingRepository.countByProducerIdAndRatingValue(producerId, 5));
        stats.setFourStars(ratingRepository.countByProducerIdAndRatingValue(producerId, 4));
        stats.setThreeStars(ratingRepository.countByProducerIdAndRatingValue(producerId, 3));
        stats.setTwoStars(ratingRepository.countByProducerIdAndRatingValue(producerId, 2));
        stats.setOneStar(ratingRepository.countByProducerIdAndRatingValue(producerId, 1));

        return stats;
    }

    /**
     * Удалить рейтинг
     */
    @Transactional
    public void deleteRating(Long customerId, Long producerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        User producer = userRepository.findById(producerId)
                .orElseThrow(() -> new RuntimeException("Producer not found"));

        Rating rating = ratingRepository.findByCustomerAndProducer(customer, producer)
                .orElseThrow(() -> new RuntimeException("Rating not found"));

        ratingRepository.delete(rating);
    }

    /**
     * Проверить, поставил ли заказчик оценку исполнителю
     */
    public boolean hasUserRatedProducer(Long customerId, Long producerId) {
        return ratingRepository.existsByCustomerIdAndProducerId(customerId, producerId);
    }

    /**
     * Конвертировать Rating в RatingResponseDTO
     */
    private RatingResponseDTO convertToResponseDTO(Rating rating) {
        return new RatingResponseDTO(
                rating.getId(),
                rating.getCustomer().getId(),
                rating.getCustomer().getFullName(),
                rating.getProducer().getId(),
                rating.getProducer().getFullName(),
                rating.getRatingValue(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}