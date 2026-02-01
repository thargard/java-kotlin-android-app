package com.example.newtestproject

import com.example.newtestproject.model.User
import com.example.newtestproject.model.ServerAuthResponse
import com.example.newtestproject.model.Order
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/greet")
    fun greet(@Query("name") name: String): Call<String>

    @POST("/api/auth/register")
    fun register(@Body user: User): Call<ServerAuthResponse>

    @POST("/api/auth/login")
    fun login(@Body credentials: Map<String, String>): Call<ServerAuthResponse>

    @POST("/api/auth/google")
    fun loginWithGoogle(@Body request: Map<String, String>): Call<ServerAuthResponse>

    @GET("/api/orders")
    fun getAllOrders(): Call<List<Order>>

    @GET("/api/orders/user/{userId}")
    fun getOrdersByUser(@Path("userId") userId: Long): Call<List<Order>>
}