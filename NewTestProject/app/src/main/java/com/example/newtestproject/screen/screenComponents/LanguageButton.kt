package com.example.newtestproject.screen.screenComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.example.newtestproject.R
import com.example.newtestproject.components.currentActivity
import com.example.newtestproject.util.LanguageManager

@Composable
fun LanguageButton(

) {
    val activity = currentActivity()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { showDialog = true }) {
        Icon(Icons.Default.Language, contentDescription = "language")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(R.string.choose_language)) },
            text = {
                Column {
                    TextButton(onClick = {
                        LanguageManager.applyLanguage(activity ?: context, "ru")
                        showDialog = false
                    }) { Text("Русский") }

                    TextButton(onClick = {
                        LanguageManager.applyLanguage(activity ?: context, "en")
                        showDialog = false
                    }) { Text("English") }

                    TextButton(onClick = {
                        LanguageManager.applyLanguage(activity ?: context, null)
                        showDialog = false
                    }) { Text(stringResource(R.string.follow_system)) }
                }
            },
            confirmButton = {}
        )
    }
}