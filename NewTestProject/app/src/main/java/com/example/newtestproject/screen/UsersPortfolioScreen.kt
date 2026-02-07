package com.example.newtestproject.screen

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

data class PortfolioUserEntry(
    val userId: Long,
    val label: String,
    val completedCount: Int
)

@Composable
fun UsersPortfolioScreen(
    onGoToOrders: () -> Unit,
    onOpenUserPortfolio: (Long, String) -> Unit
) {
    var entries by remember { mutableStateOf<List<PortfolioUserEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        RetrofitClient.api.getAllOrders()
            .enqueue(object : Callback<List<Order>> {
                override fun onResponse(
                    call: Call<List<Order>>,
                    response: Response<List<Order>>
                ) {
                    isLoading = false
                    if (response.isSuccessful) {
                        val orders = response.body() ?: emptyList()
                        entries = buildPortfolioEntries(orders)
                    } else {
                        errorMessage = "Error loading portfolios: ${response.code()}"
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
            text = stringResource(id = R.string.all_portfolios),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
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
            entries.isEmpty() -> {
                Text(
                    text = stringResource(id = R.string.no_portfolios),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries) { entry ->
                        PortfolioUserCard(
                            entry = entry,
                            onClick = { onOpenUserPortfolio(entry.userId, entry.label) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioUserCard(
    entry: PortfolioUserEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(id = R.string.completed_works_count, entry.completedCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun buildPortfolioEntries(orders: List<Order>): List<PortfolioUserEntry> {
    val grouped = orders.groupBy { order ->
        order.userId ?: order.user?.id
    }

    return grouped
        .filterKeys { it != null }
        .map { (userId, userOrders) ->
            val safeUserId = userId ?: 0L
            val label = userOrders
                .mapNotNull { it.user?.login ?: it.user?.email }
                .firstOrNull()
                ?: "ID: $safeUserId"
            val completedCount = userOrders.count { it.status == OrderStatus.COMPLETED }
            PortfolioUserEntry(
                userId = safeUserId,
                label = label,
                completedCount = completedCount
            )
        }
        .sortedByDescending { it.completedCount }
}
