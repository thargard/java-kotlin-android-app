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
import com.example.newtestproject.model.Order
import com.example.newtestproject.model.OrderStatus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun UserPortfolioScreen(
    userId: Long,
    userLabel: String,
    onBackToPortfolios: () -> Unit
) {
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        RetrofitClient.api.getOrdersByUser(userId)
            .enqueue(object : Callback<List<Order>> {
                override fun onResponse(
                    call: Call<List<Order>>,
                    response: Response<List<Order>>
                ) {
                    isLoading = false
                    if (response.isSuccessful) {
                        orders = (response.body() ?: emptyList())
                            .filter { it.status == OrderStatus.COMPLETED }
                    } else {
                        errorMessage = "Error loading portfolio: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<List<Order>>, t: Throwable) {
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
            orders.isEmpty() -> {
                Text(
                    text = stringResource(id = R.string.no_completed_orders),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(orders) { order ->
                        OrderCard(order = order)
                    }
                }
            }
        }

        Button(onClick = onBackToPortfolios) {
            Text(stringResource(id = R.string.back_to_portfolios))
        }

    }
}
