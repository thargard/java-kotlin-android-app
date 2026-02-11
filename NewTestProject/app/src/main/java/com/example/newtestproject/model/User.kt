package com.example.newtestproject.model

data class User(
    val id: Long? = null,
    val login: String,
    val password: String,
    val email: String? = null,
    val fullName: String? = null,
    val role: String? = null
)
