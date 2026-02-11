package example.com.server.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RatingRequestDTO {

    @NotNull(message = "Producer ID is required")
    private Long producerId;

    @NotNull(message = "Rating value is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer ratingValue;

    // Constructors
    public RatingRequestDTO() {
    }

    public RatingRequestDTO(Long producerId, Integer ratingValue) {
        this.producerId = producerId;
        this.ratingValue = ratingValue;
    }

    // Getters and Setters
    public Long getProducerId() {
        return producerId;
    }

    public void setProducerId(Long producerId) {
        this.producerId = producerId;
    }

    public Integer getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(Integer ratingValue) {
        this.ratingValue = ratingValue;
    }
}