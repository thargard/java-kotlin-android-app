package com.example.newtestproject.screen

import android.widget.Toast
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newtestproject.R
import com.example.newtestproject.RetrofitClient
import com.example.newtestproject.screen.screenComponents.PaymentDialog
import com.example.newtestproject.util.CartStore
import com.example.newtestproject.util.SessionPrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun CartScreen(
    onBack: () -> Unit
) {
    val items = CartStore.items
    val showPaymentDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val token = SessionPrefs.getServerToken(context)
    val authHeader = token?.let { "Bearer $it" }

    if (showPaymentDialog.value) {
        PaymentDialog(
            title = stringResource(id = R.string.buy_all),
            onConfirm = {
                if (authHeader == null) {
                    Toast.makeText(context, context.getString(R.string.login_required), Toast.LENGTH_SHORT).show()
                } else {
                    items
                        .filter { it.product.isAvailable == true }
                        .forEach { item ->
                            val productId = item.product.id ?: return@forEach
                            RetrofitClient.api.buyProduct(authHeader, productId)
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
                        }
                    CartStore.clear()
                    Toast.makeText(context, context.getString(R.string.purchase_success), Toast.LENGTH_SHORT).show()
                }
                showPaymentDialog.value = false
            },
            onDismiss = { showPaymentDialog.value = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.your_cart),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.empty_cart),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    CartItemCard(
                        item = item,
                        onRemove = { CartStore.remove(item.product.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.total) + ": ${formatPrice(CartStore.totalPrice())}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = { showPaymentDialog.value = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.buy_all))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.back))
        }
    }
}

@Composable
private fun CartItemCard(
    item: com.example.newtestproject.util.CartItem,
    onRemove: () -> Unit
) {
    val available = item.product.isAvailable == true
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.product.name ?: "-",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.price) + ": ${formatPrice(item.product.price)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.quantity) + ": ${item.quantity}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.availability) + ": " +
                    (if (available) stringResource(id = R.string.available) else stringResource(id = R.string.not_available)),
                style = MaterialTheme.typography.bodySmall,
                color = if (available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onRemove) {
                    Text(stringResource(id = R.string.remove))
                }
            }
        }
    }
}

private fun formatPrice(price: Double?): String {
    return if (price == null) "-" else String.format("%.2f", price)
}
