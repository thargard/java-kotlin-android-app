package com.example.newtestproject.screen.screenComponents

import androidx.compose.material.icons.Icons
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.newtestproject.R

@Composable
fun ProfileButton(
    navController: NavController,
    onClick: () -> Unit = { navController.navigate("profile") { launchSingleTop = true } }
) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.Default.Person,
            contentDescription = stringResource(id = R.string.personal_cabinet)
        )
    }
}
