package com.example.newtestproject.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newtestproject.R
import com.example.newtestproject.RetrofitClient
import com.example.newtestproject.model.Product
import com.example.newtestproject.screen.screenComponents.ProductCard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun UserPortfolioScreen(
    userId: Long,
    userLabel: String,
    sellerKey: String,
    onBackToPortfolios: () -> Unit,
    onOpenProduct: (Long) -> Unit
) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId, sellerKey) {
        val call = if (userId >= 0L) {
            RetrofitClient.api.getProducts(sellerId = userId, size = 200)
        } else {
            RetrofitClient.api.getProducts(size = 200)
        }
        call.enqueue(object : Callback<com.example.newtestproject.model.ProductPageResponse> {
            override fun onResponse(
                call: Call<com.example.newtestproject.model.ProductPageResponse>,
                response: Response<com.example.newtestproject.model.ProductPageResponse>
            ) {
                isLoading = false
                if (response.isSuccessful) {
                    val allProducts = response.body()?.content ?: emptyList()
                    products = if (userId >= 0L) {
                        allProducts
                    } else {
                        allProducts.filter { product ->
                            val key = product.seller?.login
                                ?: product.seller?.email
                                ?: product.sellerName
                                ?: product.sellerId?.toString()
                                ?: "unknown"
                            key == sellerKey
                        }
                    }
                } else {
                    errorMessage = "Error loading portfolio: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<com.example.newtestproject.model.ProductPageResponse>, t: Throwable) {
                isLoading = false
                errorMessage = "Network error: ${t.message}"
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.user_portfolio_title, userLabel),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: stringResource(id = R.string.unknownError),
                    color = MaterialTheme.colorScheme.error
                )
            }
            products.isEmpty() -> {
                Text(
                    text = stringResource(id = R.string.no_products_in_portfolio),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(products) { product ->
                        val productId = product.id
                        ProductCard(
                            product = product,
                            onClick = if (productId != null) {
                                { onOpenProduct(productId) }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }

        Button(onClick = onBackToPortfolios) {
            Text(stringResource(id = R.string.back_to_portfolios))
        }

    }
}
