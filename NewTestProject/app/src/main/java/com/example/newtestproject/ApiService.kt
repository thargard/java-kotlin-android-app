package com.example.newtestproject

import com.example.newtestproject.model.User
import com.example.newtestproject.model.ServerAuthResponse
import com.example.newtestproject.model.Order
import com.example.newtestproject.model.Product
import com.example.newtestproject.model.ProductPageResponse
import com.example.newtestproject.model.RatingStats
import com.example.newtestproject.model.RatingResponse
import com.example.newtestproject.model.RatingRequest
import com.example.newtestproject.model.ConversationsResponse
import com.example.newtestproject.model.ConversationMessagesResponse
import com.example.newtestproject.model.MessageDto
import com.example.newtestproject.model.SendMessageRequest
import com.example.newtestproject.model.UnreadCountResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PATCH
import retrofit2.http.Query
import retrofit2.http.Header

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

    @GET("/api/products")
    fun getProducts(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("sellerId") sellerId: Long? = null,
        @Query("availableOnly") availableOnly: Boolean? = null
    ): Call<ProductPageResponse>

    @GET("/api/products/{id}")
    fun getProduct(@Path("id") id: Long): Call<Product>

    @POST("/api/products")
    fun createProduct(
        @Header("Authorization") authorization: String,
        @Body body: com.example.newtestproject.model.ProductCreateRequest
    ): Call<Product>

    @POST("/api/products/{id}/cart")
    fun addProductToCart(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): Call<Map<String, Any>>

    @POST("/api/products/{id}/buy")
    fun buyProduct(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): Call<Map<String, Any>>

    @POST("/api/ratings")
    fun createOrUpdateRating(
        @Header("customerId") customerId: Long,
        @Body body: RatingRequest
    ): Call<RatingResponse>

    @GET("/api/ratings/customer/{customerId}/producer/{producerId}")
    fun getRatingByCustomerAndProducer(
        @Path("customerId") customerId: Long,
        @Path("producerId") producerId: Long
    ): Call<RatingResponse>

    @GET("/api/ratings/producer/{producerId}/stats")
    fun getProducerRatingStats(
        @Path("producerId") producerId: Long
    ): Call<RatingStats>

    @GET("/api/messages/conversations")
    fun getConversations(
        @Header("Authorization") authorization: String
    ): Call<ConversationsResponse>

    @GET("/api/messages/conversation/{otherUserId}")
    fun getConversation(
        @Header("Authorization") authorization: String,
        @Path("otherUserId") otherUserId: Long
    ): Call<ConversationMessagesResponse>

    @POST("/api/messages/send")
    fun sendMessage(
        @Header("Authorization") authorization: String,
        @Body body: SendMessageRequest
    ): Call<MessageDto>

    @PATCH("/api/messages/{messageId}/read")
    fun markMessageAsRead(
        @Header("Authorization") authorization: String,
        @Path("messageId") messageId: Long
    ): Call<Map<String, Any>>

    @GET("/api/messages/unread/count")
    fun getUnreadCount(
        @Header("Authorization") authorization: String
    ): Call<UnreadCountResponse>
}
