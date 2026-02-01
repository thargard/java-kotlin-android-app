package com.example.newtestproject.screen

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

@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val token = SessionPrefs.getServerToken(context)
    val payload = token?.let { EncodeJwt(it) }

    LaunchedEffect(Unit) {
        val userId = payload?.id
        if (userId == null) {
            errorMessage = context.getString(R.string.unknownError)
            isLoading = false
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

        // Логин и email из JWT
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
