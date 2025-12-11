package com.example.newtestproject.screen.authComponents

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.newtestproject.R

@Composable
fun AuthHeader(isRegisterMode: Boolean) {
    Text(
        if (!isRegisterMode) stringResource(id = R.string.log_reg) else stringResource(id = R.string.reg_acc),
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
fun LoginButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(stringResource(id = R.string.login_button))
    }
}

@Composable
fun RegisterButton(
    isRegisterMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            if (!isRegisterMode)
                stringResource(id = R.string.register_button)
            else
                stringResource(id = R.string.reg_acc_button)
        )
    }
}

@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text = stringResource(id = R.string.google_sign_in))
    }
}

