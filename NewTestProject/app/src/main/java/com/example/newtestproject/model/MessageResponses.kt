package com.example.newtestproject.model

data class ConversationsResponse(
    val conversations: List<ConversationDto>? = null
)

data class ConversationMessagesResponse(
    val otherUserId: Long? = null,
    val messages: List<MessageDto>? = null
)

data class UnreadCountResponse(
    val unreadCount: Long? = null
)
