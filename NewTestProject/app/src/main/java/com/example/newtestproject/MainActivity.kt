package com.example.newtestproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.newtestproject.model.User
import com.example.newtestproject.model.ErrorResponse
import com.google.gson.Gson


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
                    if (!isRegisterMode) "Login or Register" else "Register Account",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it },
                    label = { Text("login") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (!isRegisterMode) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                } else {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
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
                        label = { Text("Repeat Password") },
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
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage.isNotBlank()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
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
                                        // Proceed to SecondActivity with user info
                                        val intent = Intent(this@MainActivity, SecondActivity::class.java)
                                        intent.putExtra("login", user?.login)
                                        intent.putExtra("fullName", user?.fullName)
                                        intent.putExtra("email", user?.email)
                                        startActivity(intent)
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
                    Text("Login")
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(onClick = {
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
                                        val intent = Intent(this@MainActivity, SecondActivity::class.java)
                                        intent.putExtra("login", newUser?.login)
                                        intent.putExtra("fullName", newUser?.fullName)
                                        intent.putExtra("email", newUser?.email)
                                        startActivity(intent)
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
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (!isRegisterMode) "Register" else "Submit Registration")
                }
            }
        }
    }
}