package com.example.newtestproject

import com.example.newtestproject.model.User
import com.example.newtestproject.model.ServerAuthResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("api/greet")
    fun greet(@Query("name") name: String): Call<String>

    @POST("/api/auth/register")
    fun register(@Body user: User): Call<User>

    @POST("/api/auth/login")
    fun login(@Body credentials: Map<String, String>): Call<User>

    @POST("/api/auth/google")
    fun loginWithGoogle(@Body request: Map<String, String>): Call<ServerAuthResponse>
}