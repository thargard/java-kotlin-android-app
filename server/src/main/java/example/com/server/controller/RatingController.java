package example.com.server.controller;

import example.com.server.dto.RatingRequestDTO;
import example.com.server.dto.RatingResponseDTO;
import example.com.server.dto.RatingStatsDTO;
import example.com.server.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*") // Настройте правильный CORS для продакшена
public class RatingController {

    @Autowired
    private RatingService ratingService;

    /**
     * Создать или обновить рейтинг
     * POST /api/ratings
     * Body: { "producerId": 123, "ratingValue": 5 }
     * Header: customerId (в реальном приложении это должно быть из JWT токена)
     */
    @PostMapping
    public ResponseEntity<RatingResponseDTO> createOrUpdateRating(
            @RequestHeader("customerId") Long customerId,
            @Valid @RequestBody RatingRequestDTO requestDTO) {
        try {
            RatingResponseDTO response = ratingService.createOrUpdateRating(customerId, requestDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Получить рейтинг, который заказчик поставил исполнителю
     * GET /api/ratings/customer/{customerId}/producer/{producerId}
     */
    @GetMapping("/customer/{customerId}/producer/{producerId}")
    public ResponseEntity<RatingResponseDTO> getRatingByCustomerAndProducer(
            @PathVariable Long customerId,
            @PathVariable Long producerId) {
        Optional<RatingResponseDTO> rating = ratingService.getRatingByCustomerAndProducer(customerId, producerId);
        return rating.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Получить все рейтинги для исполнителя
     * GET /api/ratings/producer/{producerId}
     */
    @GetMapping("/producer/{producerId}")
    public ResponseEntity<List<RatingResponseDTO>> getRatingsByProducer(@PathVariable Long producerId) {
        List<RatingResponseDTO> ratings = ratingService.getRatingsByProducer(producerId);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Получить статистику рейтинга исполнителя
     * GET /api/ratings/producer/{producerId}/stats
     */
    @GetMapping("/producer/{producerId}/stats")
    public ResponseEntity<RatingStatsDTO> getProducerRatingStats(@PathVariable Long producerId) {
        try {
            RatingStatsDTO stats = ratingService.getProducerRatingStats(producerId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Проверить, поставил ли заказчик оценку исполнителю
     * GET /api/ratings/check?customerId={customerId}&producerId={producerId}
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> hasUserRatedProducer(
            @RequestParam Long customerId,
            @RequestParam Long producerId) {
        boolean hasRated = ratingService.hasUserRatedProducer(customerId, producerId);
        return ResponseEntity.ok(hasRated);
    }

    /**
     * Удалить рейтинг
     * DELETE /api/ratings/customer/{customerId}/producer/{producerId}
     */
    @DeleteMapping("/customer/{customerId}/producer/{producerId}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable Long customerId,
            @PathVariable Long producerId) {
        try {
            ratingService.deleteRating(customerId, producerId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}