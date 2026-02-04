package com.example.newtestproject.screen.screenComponents

import androidx.compose.material.icons.Icons
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.newtestproject.R

@Composable
fun PortfolioButton(
    navController: NavController,
    onClick: () -> Unit = { navController.navigate("portfolios") { launchSingleTop = true } }
) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.Default.Work,
            contentDescription = stringResource(id = R.string.all_portfolios)
        )
    }
}
