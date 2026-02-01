package com.example.newtestproject.screen

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.newtestproject.R
import com.example.newtestproject.RetrofitClient
import com.example.newtestproject.screen.screenComponents.AuthErrorMessage
import com.example.newtestproject.components.EncodeJwt
import com.example.newtestproject.model.ErrorResponse
import com.example.newtestproject.model.ServerAuthResponse
import com.example.newtestproject.model.User
import com.example.newtestproject.screen.screenComponents.*
import com.example.newtestproject.util.SessionPrefs
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
    var selectedRole by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity
    val webClientId = stringResource(id = R.string.default_web_client_id)
    val googleSignInClient = remember(webClientId) {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(webClientId)
                .build()
        )
    }
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken.isNullOrBlank()) {
                errorMessage = "Google token missing"
                return@rememberLauncherForActivityResult
            }


            val dic = mapOf("token" to  idToken)
            RetrofitClient.api.loginWithGoogle(dic)
                .enqueue(object : Callback<ServerAuthResponse> {
                    override fun onResponse(
                        call: Call<ServerAuthResponse>,
                        response: Response<ServerAuthResponse>
                    ) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            val payload = EncodeJwt(body?.token.toString())
                            if (body != null) {
                                SessionPrefs.saveTokens(
                                    context,
                                    serverToken = body.token,
                                    idToken = idToken
                                )
                            }
                            val name = payload?.login
                                ?: account.displayName
                                ?: account.email
                                ?: account.id
                                ?: login.ifBlank { "User" }
                            onLoginSuccess(name)
                        } else {
                            val errorStr = response.errorBody()?.string()
                            val parsedErr = try {
                                Gson().fromJson(errorStr, ErrorResponse::class.java)
                            } catch (_: Exception) {
                                null
                            }
                            errorMessage = parsedErr?.error
                                ?: ("Google auth failed " + (errorStr ?: response.code()
                                .toString()))
                        }
                    }

                    override fun onFailure(call: Call<ServerAuthResponse>, t: Throwable) {
                        errorMessage = t.message ?: "Network error"
                    }
                })
        } catch (ex: Exception) {
            errorMessage = "Google sign-in failed"
        }
    }

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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = if (isRegisterMode) Arrangement.Top else Arrangement.Center
    ) {
        AuthHeader(isRegisterMode)

        Spacer(modifier = Modifier.height(32.dp))

        LoginField(
            value = login,
            onValueChange = { login = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!isRegisterMode) {
            PasswordField(
                value = password,
                onValueChange = { password = it },
                labelRes = R.string.password,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            PasswordField(
                value = password,
                onValueChange = { password = it },
                labelRes = R.string.password,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = passwordSupportingColor,
                    unfocusedBorderColor = passwordSupportingColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            RepeatPasswordField(
                value = repeatPassword,
                onValueChange = { repeatPassword = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = passwordSupportingColor,
                    unfocusedBorderColor = passwordSupportingColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            FullNameField(
                value = fullName,
                onValueChange = { fullName = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            EmailField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            RoleSelectionField(
                selectedRole = selectedRole,
                onRoleSelected = { selectedRole = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AuthErrorMessage(errorMessage = errorMessage)
        if (errorMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        LoginButton(
            onClick = {
                if (login.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter login and password!"
                } else {
                    val credentials = mapOf("login" to login, "password" to password)
                    RetrofitClient.api.login(credentials).enqueue(object : Callback<ServerAuthResponse> {
                        override fun onResponse(call: Call<ServerAuthResponse>, response: Response<ServerAuthResponse>) {
                            if (response.isSuccessful) {
                                val body = response.body()
                                val payload = EncodeJwt(body?.token.toString())
                                if (body != null){
                                    SessionPrefs.saveTokens(
                                        context,
                                        serverToken = body.token,
                                        idToken = null
                                    )
                                }
                                onLoginSuccess(payload?.login.toString())
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
                        override fun onFailure(call: Call<ServerAuthResponse>, t: Throwable) {
                            errorMessage = t.message ?: "Network error"
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegisterMode
        )
        Spacer(modifier = Modifier.height(12.dp))

        RegisterButton(
            isRegisterMode = isRegisterMode,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (!isRegisterMode) {
                    isRegisterMode = true
                    errorMessage = ""
                    selectedRole = null
                } else {
                    if (login.isBlank() || password.isBlank() || fullName.isBlank() || email.isBlank() || repeatPassword.isBlank() || selectedRole == null) {
                        errorMessage = "Fill all fields for registration!"
                    } else if (password != repeatPassword) {
                        errorMessage = "Passwords do not match!"
                    } else {
                        val user = User(login, password, email, fullName, selectedRole)
                        RetrofitClient.api.register(user).enqueue(object : Callback<ServerAuthResponse> {
                            override fun onResponse(call: Call<ServerAuthResponse>, response: Response<ServerAuthResponse>) {
                                if (response.isSuccessful) {
                                    val body = response.body()
                                    val payload = EncodeJwt(body?.token.toString())
                                    if(body != null) {
                                        SessionPrefs.saveTokens(
                                            context,
                                            serverToken = body.token,
                                            idToken = null
                                        )
                                    }
                                    onRegisterSuccess(payload?.login.toString())
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
                            override fun onFailure(call: Call<ServerAuthResponse>, t: Throwable) {
                                errorMessage = t.message ?: "Network error"
                            }
                        })
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        GoogleSignInButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                errorMessage = ""
                val signInIntent = googleSignInClient.signInIntent
                if (activity != null) {
                    googleLauncher.launch(signInIntent)
                } else {
                    errorMessage = "Google sign-in failed"
                }
            }
        )

        if (isRegisterMode) {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}