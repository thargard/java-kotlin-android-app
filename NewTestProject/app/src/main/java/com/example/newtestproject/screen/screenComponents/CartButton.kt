package com.example.newtestproject.screen.screenComponents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.newtestproject.R

@Composable
fun CartButton(
    navController: NavController,
    onClick: () -> Unit = { navController.navigate("cart") { launchSingleTop = true } }
) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = stringResource(id = R.string.cart)
        )
    }
}
