package com.example.newtestproject.model

data class JwtPayload(
    val id: Long?,
    val login: String,
    val role: String,
    val email: String
)
