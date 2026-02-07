package com.example.newtestproject.screen.screenComponents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Paid
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.newtestproject.R

@Composable
fun OrdersButton(
    navController: NavController,
    onClick: () -> Unit = { navController.navigate("orders") { launchSingleTop = true } }
) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.Default.Paid,
            contentDescription = stringResource(id = R.string.allOrders)
        )
    }
}