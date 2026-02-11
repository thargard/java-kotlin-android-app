package com.example.newtestproject.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newtestproject.R
import com.example.newtestproject.RetrofitClient
import com.example.newtestproject.components.EncodeJwt
import com.example.newtestproject.model.Order
import com.example.newtestproject.util.SessionPrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.newtestproject.model.RatingStats

@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var ratingStats by remember { mutableStateOf<RatingStats?>(null) }
    var ratingLoading by remember { mutableStateOf(true) }
    var ratingError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val token = SessionPrefs.getServerToken(context)
    val payload = token?.let { EncodeJwt(it) }
    val authHeader = token?.let { "Bearer $it" }

    LaunchedEffect(Unit) {
        val userId = payload?.id
        if (userId == null) {
            errorMessage = context.getString(R.string.unknownError)
            isLoading = false
            ratingLoading = false
            ratingError = context.getString(R.string.unknownError)
            return@LaunchedEffect
        }

        RetrofitClient.api.getOrdersByUser(userId)
            .enqueue(object : Callback<List<Order>> {
                override fun onResponse(
                    call: Call<List<Order>>,
                    response: Response<List<Order>>
                ) {
                    isLoading = false
                    if (response.isSuccessful) {
                        orders = response.body() ?: emptyList()
                    } else {
                        errorMessage = "${context.getString(R.string.unknownError)}: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                    isLoading = false
                    errorMessage = "${context.getString(R.string.network_error)}: ${t.message}"
                }
            })

        RetrofitClient.api.getProducerRatingStats(userId)
            .enqueue(object : Callback<RatingStats> {
                override fun onResponse(
                    call: Call<RatingStats>,
                    response: Response<RatingStats>
                ) {
                    ratingLoading = false
                    if (response.isSuccessful) {
                        ratingStats = response.body()
                    } else {
                        ratingError = "${context.getString(R.string.unknownError)}: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<RatingStats>, t: Throwable) {
                    ratingLoading = false
                    ratingError = "${context.getString(R.string.network_error)}: ${t.message}"
                }
            })
    }

    if (showCreateDialog) {
        CreateProductDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { name, priceText, category, description, imageUrl ->
                if (authHeader == null) {
                    Toast.makeText(context, context.getString(R.string.login_required), Toast.LENGTH_SHORT).show()
                    return@CreateProductDialog
                }
                if (name.isBlank() || category.isBlank()) {
                    Toast.makeText(context, context.getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show()
                    return@CreateProductDialog
                }
                val price = priceText.toDoubleOrNull()
                if (price == null || price <= 0.0) {
                    Toast.makeText(context, context.getString(R.string.invalid_price), Toast.LENGTH_SHORT).show()
                    return@CreateProductDialog
                }

                val payload = com.example.newtestproject.model.ProductCreateRequest(
                    name = name.trim(),
                    description = description.trim().ifBlank { null },
                    category = category.trim(),
                    imageUrl = imageUrl.trim().ifBlank { null },
                    price = price
                )

                RetrofitClient.api.createProduct(authHeader, payload)
                    .enqueue(object : Callback<com.example.newtestproject.model.Product> {
                        override fun onResponse(
                            call: Call<com.example.newtestproject.model.Product>,
                            response: Response<com.example.newtestproject.model.Product>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, context.getString(R.string.product_created), Toast.LENGTH_SHORT).show()
                                showCreateDialog = false
                            } else {
                                Toast.makeText(context, context.getString(R.string.product_create_failed), Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<com.example.newtestproject.model.Product>, t: Throwable) {
                            Toast.makeText(context, context.getString(R.string.product_create_failed), Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.personal_cabinet),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.login) + ": ${payload?.login ?: "—"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.email) + ": ${payload?.email ?: "—"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when {
                    ratingLoading -> {
                        Text(text = stringResource(id = R.string.rating_loading))
                    }
                    ratingError != null -> {
                        Text(
                            text = ratingError ?: "",
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
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.create_product))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.my_orders),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            orders.isEmpty() -> {
                Text(
                    text = stringResource(id = R.string.noOrders),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(orders) { order ->
                        OrderCard(order = order)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.logout))
        }
    }
}

@Composable
private fun CreateProductDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit
) {
    val nameState = remember { mutableStateOf("") }
    val priceState = remember { mutableStateOf("") }
    val categoryState = remember { mutableStateOf("") }
    val descriptionState = remember { mutableStateOf("") }
    val imageUrlState = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.create_product)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    label = { Text(stringResource(id = R.string.product_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceState.value,
                    onValueChange = { priceState.value = it },
                    label = { Text(stringResource(id = R.string.price)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = categoryState.value,
                    onValueChange = { categoryState.value = it },
                    label = { Text(stringResource(id = R.string.category)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descriptionState.value,
                    onValueChange = { descriptionState.value = it },
                    label = { Text(stringResource(id = R.string.product_description)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = imageUrlState.value,
                    onValueChange = { imageUrlState.value = it },
                    label = { Text(stringResource(id = R.string.image_url)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit(
                    nameState.value,
                    priceState.value,
                    categoryState.value,
                    descriptionState.value,
                    imageUrlState.value
                )
            }) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}
