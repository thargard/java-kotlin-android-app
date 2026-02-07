package com.example.newtestproject.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val imageUrl: String? = null,
    val sellerId: Long? = null,
    val sellerName: String? = null,
    val seller: User? = null,
    val category: String? = null,
    val createdAt: String? = null,
    val isAvailable: Boolean? = null
)
