package com.example.newtestproject.model

data class ConversationDto(
    val otherUserId: Long? = null,
    val otherUserName: String? = null,
    var lastMessage: String? = null,
    var lastMessageAt: String? = null,
    var unreadCount: Long? = null,
    var isLastMessageFromMe: Boolean? = null
)


