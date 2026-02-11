package com.example.newtestproject.screen.screenComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.newtestproject.R

@Composable
fun PaymentDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val cardNumberState = remember { mutableStateOf("") }
    val cardHolderState = remember { mutableStateOf("") }
    val cardExpiryState = remember { mutableStateOf("") }
    val cardCvvState = remember { mutableStateOf("") }

    val cardNumberDigits = cardNumberState.value.filter { it.isDigit() }
    val cardNumberValid = cardNumberDigits.length in 13..19
    val cardHolderValid = cardHolderState.value.trim().length >= 3
    val expiryValid = Regex("^(0[1-9]|1[0-2])/(\\d{2})$")
        .matches(cardExpiryState.value.trim())
    val cvvValid = Regex("^\\d{3,4}$").matches(cardCvvState.value.trim())

    val isFormValid = cardNumberValid && cardHolderValid && expiryValid && cvvValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = cardNumberState.value,
                    onValueChange = { cardNumberState.value = it },
                    label = { Text(stringResource(id = R.string.card_number)) },
                    isError = cardNumberState.value.isBlank() || !cardNumberValid,
                    supportingText = {
                        if (cardNumberState.value.isBlank()) {
                            Text(stringResource(id = R.string.required_field))
                        } else if (!cardNumberValid) {
                            Text(stringResource(id = R.string.invalid_card_number))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cardHolderState.value,
                    onValueChange = { cardHolderState.value = it },
                    label = { Text(stringResource(id = R.string.card_holder)) },
                    isError = cardHolderState.value.isBlank() || !cardHolderValid,
                    supportingText = {
                        if (cardHolderState.value.isBlank()) {
                            Text(stringResource(id = R.string.required_field))
                        } else if (!cardHolderValid) {
                            Text(stringResource(id = R.string.invalid_card_holder))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cardExpiryState.value,
                    onValueChange = { cardExpiryState.value = it },
                    label = { Text(stringResource(id = R.string.card_expiry)) },
                    isError = cardExpiryState.value.isBlank() || !expiryValid,
                    supportingText = {
                        if (cardExpiryState.value.isBlank()) {
                            Text(stringResource(id = R.string.required_field))
                        } else if (!expiryValid) {
                            Text(stringResource(id = R.string.invalid_card_expiry))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cardCvvState.value,
                    onValueChange = { cardCvvState.value = it },
                    label = { Text(stringResource(id = R.string.card_cvv)) },
                    isError = cardCvvState.value.isBlank() || !cvvValid,
                    supportingText = {
                        if (cardCvvState.value.isBlank()) {
                            Text(stringResource(id = R.string.required_field))
                        } else if (!cvvValid) {
                            Text(stringResource(id = R.string.invalid_card_cvv))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isFormValid
            ) {
                Text(stringResource(id = R.string.pay))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(id = R.string.close))
            }
        }
    )
}
