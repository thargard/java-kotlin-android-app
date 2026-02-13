package com.example.newtestproject.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newtestproject.R
import com.example.newtestproject.RetrofitClient
import com.example.newtestproject.components.EncodeJwt
import com.example.newtestproject.model.Product
import com.example.newtestproject.screen.screenComponents.ProductCard
import com.example.newtestproject.util.SessionPrefs
import com.example.newtestproject.model.RatingStats
import com.example.newtestproject.model.RatingResponse
import com.example.newtestproject.model.RatingRequest
import com.example.newtestproject.screen.screenComponents.RatingStars
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun UserPortfolioScreen(
    userId: Long,
    userLabel: String,
    sellerKey: String,
    onBackToPortfolios: () -> Unit,
    onOpenProduct: (Long) -> Unit,
    unknownErrorMessage: String = stringResource(R.string.unknownError),
    networkErrorMessage: String = stringResource(R.string.network_error),
    saveMessage: String = stringResource(R.string.save)
) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var ratingStats by remember { mutableStateOf<RatingStats?>(null) }
    var ratingStatsLoading by remember { mutableStateOf(true) }
    var ratingStatsError by remember { mutableStateOf<String?>(null) }
    var myRating by remember { mutableStateOf<Int?>(null) }
    var myRatingLoading by remember { mutableStateOf(false) }
    var myRatingError by remember { mutableStateOf<String?>(null) }
    var isSubmittingRating by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val token = SessionPrefs.getServerToken(context)
    val payload = token?.let { EncodeJwt(it) }
    val currentUserId = payload?.id

    fun loadRatingStats() {
        if (userId < 0L) {
            ratingStatsLoading = false
            ratingStatsError = null
            return
        }
        ratingStatsLoading = true
        RetrofitClient.api.getProducerRatingStats(userId)
            .enqueue(object : Callback<RatingStats> {
                override fun onResponse(
                    call: Call<RatingStats>,
                    response: Response<RatingStats>
                ) {
                    ratingStatsLoading = false
                    if (response.isSuccessful) {
                        ratingStats = response.body()
                        ratingStatsError = null
                    } else {
                        ratingStatsError = "Error loading rating: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<RatingStats>, t: Throwable) {
                    ratingStatsLoading = false
                    ratingStatsError = "Network error: ${t.message}"
                }
            })
    }

    fun loadMyRating() {
        if (currentUserId == null || userId < 0L || currentUserId == userId) {
            myRating = null
            myRatingLoading = false
            myRatingError = null
            return
        }
        myRatingLoading = true
        RetrofitClient.api.getRatingByCustomerAndProducer(currentUserId, userId)
            .enqueue(object : Callback<RatingResponse> {
                override fun onResponse(
                    call: Call<RatingResponse>,
                    response: Response<RatingResponse>
                ) {
                    myRatingLoading = false
                    if (response.isSuccessful) {
                        myRating = response.body()?.ratingValue
                        myRatingError = null
                    } else if (response.code() == 404) {
                        myRating = null
                        myRatingError = null
                    } else {
                        myRatingError = "Error loading your rating: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                    myRatingLoading = false
                    myRatingError = "Network error: ${t.message}"
                }
            })
    }

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

    LaunchedEffect(userId) {
        loadRatingStats()
    }

    LaunchedEffect(currentUserId, userId) {
        loadMyRating()
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when {
                    ratingStatsLoading -> {
                        Text(text = stringResource(id = R.string.rating_loading))
                    }
                    ratingStatsError != null -> {
                        Text(
                            text = ratingStatsError ?: stringResource(id = R.string.unknownError),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        val total = ratingStats?.totalRatings ?: 0L
                        val average = ratingStats?.averageRating ?: 0.0
                        val ratingText = if (total > 0) {
                            stringResource(id = R.string.rating_with_count, average, total)
                        } else {
                            stringResource(id = R.string.rating_empty)
                        }
                        Text(
                            text = ratingText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                val canRate = currentUserId != null && userId >= 0L && currentUserId != userId
                if (canRate) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.rate_user),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        RatingStars(
                            rating = myRating,
                            editable = !isSubmittingRating,
                            onRatingSelected = { selected ->
                                if (isSubmittingRating) return@RatingStars
                                isSubmittingRating = true
                                myRatingError = null
                                val request = RatingRequest(
                                    producerId = userId,
                                    ratingValue = selected
                                )
                                RetrofitClient.api.createOrUpdateRating(currentUserId, request)
                                    .enqueue(object : Callback<RatingResponse> {
                                        override fun onResponse(
                                            call: Call<RatingResponse>,
                                            response: Response<RatingResponse>
                                        ) {
                                            isSubmittingRating = false
                                            if (response.isSuccessful) {
                                                myRating = response.body()?.ratingValue ?: selected
                                                loadRatingStats()
                                                Toast.makeText(
                                                    context,
                                                    saveMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                myRatingError = "Error saving rating: ${response.code()}"
                                                Toast.makeText(
                                                    context,
                                                    myRatingError ?: unknownErrorMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                                            isSubmittingRating = false
                                            myRatingError = "Network error: ${t.message}"
                                            Toast.makeText(
                                                context,
                                                myRatingError ?: networkErrorMessage,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    })
                            }
                        )
                        if (myRatingLoading || isSubmittingRating) {
                            Spacer(modifier = Modifier.size(8.dp))
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        }
                    }
                    if (myRatingError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = myRatingError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (currentUserId == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(id = R.string.login_required),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

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
