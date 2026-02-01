package com.example.newtestproject.components

import com.auth0.android.jwt.JWT
import com.example.newtestproject.model.JwtPayload

fun EncodeJwt(token: String): JwtPayload? {
    try {
        val jwt = JWT(token)
        val userId = jwt.getClaim("id").asLong()
        val userLogin = jwt.getClaim("login").asString()
        val userRole = jwt.getClaim("role").asString()
        val userEmail = jwt.subject
        return JwtPayload(
            id = userId,
            login = userLogin.toString(),
            role = userRole.toString(),
            email = userEmail.toString()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        println("Token decode error: ${e.message}}")
        return null
    }
}