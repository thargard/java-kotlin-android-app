package com.example.newtestproject.model

data class ProductCreateRequest(
    val name: String,
    val description: String?,
    val category: String,
    val imageUrl: String?,
    val price: Double
)
