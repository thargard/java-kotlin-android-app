package example.com.server.dto;

public class RatingStatsDTO {

    private Long producerId;
    private Double averageRating;
    private Long totalRatings;
    private Long fiveStars;
    private Long fourStars;
    private Long threeStars;
    private Long twoStars;
    private Long oneStar;

    // Constructors
    public RatingStatsDTO() {
    }

    public RatingStatsDTO(Long producerId, Double averageRating, Long totalRatings) {
        this.producerId = producerId;
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
    }

    // Getters and Setters
    public Long getProducerId() {
        return producerId;
    }

    public void setProducerId(Long producerId) {
        this.producerId = producerId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(Long totalRatings) {
        this.totalRatings = totalRatings;
    }

    public Long getFiveStars() {
        return fiveStars;
    }

    public void setFiveStars(Long fiveStars) {
        this.fiveStars = fiveStars;
    }

    public Long getFourStars() {
        return fourStars;
    }

    public void setFourStars(Long fourStars) {
        this.fourStars = fourStars;
    }

    public Long getThreeStars() {
        return threeStars;
    }

    public void setThreeStars(Long threeStars) {
        this.threeStars = threeStars;
    }

    public Long getTwoStars() {
        return twoStars;
    }

    public void setTwoStars(Long twoStars) {
        this.twoStars = twoStars;
    }

    public Long getOneStar() {
        return oneStar;
    }

    public void setOneStar(Long oneStar) {
        this.oneStar = oneStar;
    }
}