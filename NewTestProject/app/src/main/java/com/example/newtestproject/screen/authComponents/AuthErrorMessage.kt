package com.example.newtestproject.screen.authComponents

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.newtestproject.R

@Composable
fun mapErrorMessage(errorMessage: String): String? {
    if (errorMessage.isBlank()) return null

    var secondPart = ""
    var message = errorMessage

    if (message.startsWith("Login failed")) {
        val prefix = "Login failed"
        secondPart = message.substringAfter(prefix).trim()
        message = prefix
    }
    if (message.startsWith("Registration failed")) {
        val prefix = "Registration failed"
        secondPart = message.substringAfter(prefix).trim()
        message = prefix
    }
    if (message.startsWith("Google auth failed")) {
        val prefix = "Google auth failed"
        secondPart = message.substringAfter(prefix).trim()
        message = prefix
    }

    return when (message) {
        "Please enter login and password!" -> stringResource(id = R.string.fill_fields_error_1)
        "Fill all fields for registration!" -> stringResource(id = R.string.fill_fields_error_2)
        "Network error" -> stringResource(id = R.string.network_error)
        "Google sign-in failed" -> stringResource(id = R.string.google_sign_in_failed)
        "Google token missing" -> stringResource(id = R.string.google_token_missing)
        "Passwords do not match!" -> stringResource(id = R.string.password_mismatch_error)
        "Login failed" -> stringResource(id = R.string.login_failed) + secondPart
        "Registration failed" -> stringResource(id = R.string.registration_failed) + secondPart
        "Google auth failed" -> stringResource(id = R.string.google_auth_failed) + secondPart
        else -> "unknown"
    }
}

@Composable
fun AuthErrorMessage(errorMessage: String) {
    val text = mapErrorMessage(errorMessage) ?: return
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error
    )
}

