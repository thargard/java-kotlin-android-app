package com.example.newtestproject.model

data class MessageDto(
    val id: Long? = null,
    val senderId: Long? = null,
    val senderName: String? = null,
    val receiverId: Long? = null,
    val receiverName: String? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val isRead: Boolean? = null
)
