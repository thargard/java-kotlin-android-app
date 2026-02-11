package com.example.newtestproject.model

data class ProductPageResponse(
    val content: List<Product> = emptyList(),
    val totalElements: Long? = null,
    val totalPages: Int? = null,
    val page: Int? = null,
    val size: Int? = null
)
