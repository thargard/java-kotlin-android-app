package com.example.newtestproject.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.newtestproject.R
import com.example.newtestproject.RetrofitClient
import com.example.newtestproject.model.Product
import com.example.newtestproject.screen.screenComponents.PaymentDialog
import com.example.newtestproject.util.CartStore
import com.example.newtestproject.util.SessionPrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ProductDetailScreen(
    productId: Long,
    onBack: () -> Unit
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val token = SessionPrefs.getServerToken(context)
    val authHeader = token?.let { "Bearer $it" }

    LaunchedEffect(productId) {
        RetrofitClient.api.getProduct(productId)
            .enqueue(object : Callback<Product> {
                override fun onResponse(call: Call<Product>, response: Response<Product>) {
                    isLoading = false
                    if (response.isSuccessful) {
                        product = response.body()
                    } else {
                        errorMessage = "${context.getString(R.string.unknownError)}: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<Product>, t: Throwable) {
                    isLoading = false
                    errorMessage = "${context.getString(R.string.network_error)}: ${t.message}"
                }
            })
    }

    if (showPaymentDialog) {
        PaymentDialog(
            title = stringResource(id = R.string.buy),
            onConfirm = {
                if (authHeader == null) {
                    Toast.makeText(context, context.getString(R.string.login_required), Toast.LENGTH_SHORT).show()
                } else {
                    RetrofitClient.api.buyProduct(authHeader, productId)
                        .enqueue(object : Callback<Map<String, Any>> {
                            override fun onResponse(
                                call: Call<Map<String, Any>>,
                                response: Response<Map<String, Any>>
                            ) {
                                if (response.isSuccessful) {
                                    Toast.makeText(context, context.getString(R.string.purchase_success), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                                Toast.makeText(context, context.getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show()
                            }
                        })
                }
                showPaymentDialog = false
            },
            onDismiss = { showPaymentDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.product_details),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

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
            product == null -> {
                Text(
                    text = stringResource(id = R.string.unknownError),
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                val available = product?.isAvailable == true
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val imageUrl = product?.imageUrl
                        if (!imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = product?.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = product?.name ?: "-",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.price) + ": ${formatPrice(product?.price)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.category) + ": ${product?.category ?: "-"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.availability) + ": " +
                                (if (available) stringResource(id = R.string.available) else stringResource(id = R.string.not_available)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        product?.description?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val sellerLabel = product?.seller?.fullName
                            ?: product?.seller?.login
                            ?: product?.seller?.email
                            ?: product?.sellerName
                        sellerLabel?.let { seller ->
                            Text(
                                text = stringResource(id = R.string.user) + ": $seller",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (available) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (authHeader == null) {
                                    Toast.makeText(context, context.getString(R.string.login_required), Toast.LENGTH_SHORT).show()
                                } else {
                                    showPaymentDialog = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(id = R.string.buy))
                        }
                        Button(
                            onClick = {
                                if (authHeader == null) {
                                    Toast.makeText(context, context.getString(R.string.login_required), Toast.LENGTH_SHORT).show()
                                } else {
                                    product?.let { CartStore.add(it) }
                                    RetrofitClient.api.addProductToCart(authHeader, productId)
                                        .enqueue(object : Callback<Map<String, Any>> {
                                            override fun onResponse(
                                                call: Call<Map<String, Any>>,
                                                response: Response<Map<String, Any>>
                                            ) {
                                                // no-op
                                            }

                                            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                                                // no-op
                                            }
                                        })
                                    Toast.makeText(context, context.getString(R.string.product_added_to_cart), Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(id = R.string.add_to_cart))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.back))
        }
    }
}

private fun formatPrice(price: Double?): String {
    return if (price == null) "-" else String.format("%.2f", price)
}
