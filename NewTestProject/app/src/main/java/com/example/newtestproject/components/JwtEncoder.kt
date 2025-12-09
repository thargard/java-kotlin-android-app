package com.example.newtestproject.components

import com.auth0.android.jwt.JWT
import com.example.newtestproject.model.JwtPayload

fun EncodeJwt(token: String): JwtPayload? {
    try {
        val jwt = JWT(token)
        val userLogin = jwt.getClaim("login").asString()
        val userEmail = jwt.subject
        return JwtPayload(
            login = userLogin.toString(),
            email = userEmail.toString()
        )
    }catch (e:Exception){
        e.printStackTrace()
        println("Token decode error: ${e.message}}")
        return null
    }

}