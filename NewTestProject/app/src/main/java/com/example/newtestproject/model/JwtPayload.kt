package com.example.newtestproject.model

data class JwtPayload(
    val login: String,
    val role: String,
    val email: String
)
