package com.example.newtestproject.model

import com.google.gson.annotations.SerializedName

data class Order(
    val id: Long?,
    val user: OrderUser? = null,
    @SerializedName("userId")
    val userId: Long? = null,
    val description: String?,
    val status: OrderStatus,
    @SerializedName("createdAt")
    val createdAt: String?
)

data class OrderUser(
    val id: Long?,
    val login: String? = null,
    val email: String? = null
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}