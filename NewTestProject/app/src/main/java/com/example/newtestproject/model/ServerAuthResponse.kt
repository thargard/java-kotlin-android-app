package com.example.newtestproject.model

data class ServerAuthResponse(
    val token: String,
    val fullName: String? = null
)
