package com.example.newtestproject.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.newtestproject.R
import com.example.newtestproject.RetrofitClient
import com.example.newtestproject.model.ErrorResponse
import com.example.newtestproject.model.User
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun AuthScreen(
    onLoginSuccess: (String) -> Unit,
    onRegisterSuccess: (String) -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val passwordsFilled = password.isNotBlank() && repeatPassword.isNotBlank()
    val passwordsMatch = passwordsFilled && password == repeatPassword
    val passwordsMismatch = passwordsFilled && password != repeatPassword
    val passwordSupportingColor = when {
        passwordsMatch -> Color(0xFF4CAF50)
        passwordsMismatch -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (!isRegisterMode) stringResource(id = R.string.log_reg) else stringResource(id = R.string.reg_acc),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text(stringResource(id = R.string.login)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!isRegisterMode) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
        } else {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                supportingText = {},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = passwordSupportingColor,
                    unfocusedBorderColor = passwordSupportingColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = repeatPassword,
                onValueChange = { repeatPassword = it },
                label = { Text(stringResource(id = R.string.second_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                supportingText = {},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = passwordSupportingColor,
                    unfocusedBorderColor = passwordSupportingColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(stringResource(id = R.string.full_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.email)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage.isNotBlank()) {
            var secondPart: String = ""
            if (errorMessage.startsWith("Login failed")){
                val prefix = "Login failed"
                secondPart = errorMessage.substringAfter(prefix).trim()
                errorMessage = prefix
            }
            if (errorMessage.startsWith("Registration failed")){
                val prefix = "Registration failed"
                secondPart = errorMessage.substringAfter(prefix).trim()
                errorMessage = prefix
            }
            Text(
                when(errorMessage) {
                    "Please enter login and password!" -> stringResource(id = R.string.fill_fields_error_1)
                    "Fill all fields for registration!" -> stringResource(id = R.string.fill_fields_error_2)
                    "Network error" -> stringResource(id = R.string.network_error)
                    "Passwords do not match!" -> stringResource(id = R.string.password_mismatch_error)
                    "Login failed" -> stringResource(id = R.string.login_failed) + secondPart
                    "Registration failed" -> stringResource(id = R.string.registration_failed) + secondPart
                    else -> "unknown"
                },
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (login.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter login and password!"
                } else {
                    val credentials = mapOf("login" to login, "password" to password)
                    RetrofitClient.api.login(credentials).enqueue(object : Callback<User> {
                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            if (response.isSuccessful) {
                                val user = response.body()
                                onLoginSuccess(user?.fullName ?: user?.login ?: login)
                            } else {
                                val errorStr = response.errorBody()?.string()
                                val parsedErr = try {
                                    Gson().fromJson(errorStr, ErrorResponse::class.java)
                                } catch(_: Exception) {
                                    null
                                }
                                errorMessage = parsedErr?.error ?: ("Login failed " + (errorStr ?: response.code().toString()))
                            }
                        }
                        override fun onFailure(call: Call<User>, t: Throwable) {
                            errorMessage = t.message ?: "Network error"
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegisterMode
        ) {
            Text(stringResource(id = R.string.login_button))
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                if (!isRegisterMode) {
                    isRegisterMode = true
                    errorMessage = ""
                } else {
                    if (login.isBlank() || password.isBlank() || fullName.isBlank() || email.isBlank() || repeatPassword.isBlank()) {
                        errorMessage = "Fill all fields for registration!"
                    } else if (password != repeatPassword) {
                        errorMessage = "Passwords do not match!"
                    } else {
                        val user = User(login, password, email, fullName)
                        RetrofitClient.api.register(user).enqueue(object : Callback<User> {
                            override fun onResponse(call: Call<User>, response: Response<User>) {
                                if (response.isSuccessful) {
                                    val newUser = response.body()
                                    onRegisterSuccess(newUser?.fullName ?: newUser?.login ?: fullName)
                                } else {
                                    val errorStr = response.errorBody()?.string()
                                    val parsedErr = try {
                                        Gson().fromJson(errorStr, ErrorResponse::class.java)
                                    } catch(_: Exception) {
                                        null
                                    }
                                    errorMessage = parsedErr?.error ?: ("Registration failed " + (errorStr ?: response.code().toString()))
                                }
                            }
                            override fun onFailure(call: Call<User>, t: Throwable) {
                                errorMessage = t.message ?: "Network error"
                            }
                        })
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (!isRegisterMode)
                    stringResource(id = R.string.register_button)
                else
                    stringResource(id = R.string.reg_acc_button)
            )
        }
    }
}