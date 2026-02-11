package com.example.newtestproject.model

data class RatingRequest(
    val producerId: Long,
    val ratingValue: Int
)

data class RatingStats(
    val producerId: Long? = null,
    val averageRating: Double? = null,
    val totalRatings: Long? = null,
    val fiveStars: Long? = null,
    val fourStars: Long? = null,
    val threeStars: Long? = null,
    val twoStars: Long? = null,
    val oneStar: Long? = null
)

data class RatingResponse(
    val id: Long? = null,
    val customerId: Long? = null,
    val customerName: String? = null,
    val producerId: Long? = null,
    val producerName: String? = null,
    val ratingValue: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)