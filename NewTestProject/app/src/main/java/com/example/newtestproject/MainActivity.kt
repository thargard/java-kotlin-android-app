package com.example.newtestproject

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.newtestproject.components.LanguageButton
import com.example.newtestproject.util.LanguageManager
import com.example.newtestproject.util.LanguagePrefs
import com.example.newtestproject.screen.FirstScreen
import com.example.newtestproject.screen.AuthScreen
import com.example.newtestproject.screen.GreetingScreen


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val savedLang = LanguagePrefs.loadLanguage(this)
        LanguageManager.applyLanguage(this, savedLang)

        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.app_name)) },
                        actions = { LanguageButton() }
                    )
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = "first",
                    modifier = Modifier.padding(padding)
                ) {
                    composable("first") {
                        FirstScreen(
                            onStartAuth = {
                                navController.navigate("auth") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable("auth") {
                        AuthScreen(
                            onLoginSuccess = { fullName ->
                                navController.navigate("greeting/${Uri.encode(fullName)}") {
                                    popUpTo("first") { inclusive = false }
                                }
                            },
                            onRegisterSuccess = { fullName ->
                                navController.navigate("greeting/${Uri.encode(fullName)}") {
                                    popUpTo("first") { inclusive = false }
                                }
                            }
                        )
                    }
                    composable(
                        route = "greeting/{fullName}",
                        arguments = listOf(navArgument("fullName") { type = NavType.StringType }),
                        deepLinks = listOf(navDeepLink { uriPattern = "newtestproject://greeting/{fullName}" })
                    ) { backStackEntry ->
                        val fullName = backStackEntry.arguments?.getString("fullName") ?: ""
                        GreetingScreen(
                            fullName = fullName,
                            onLogout = {
                                navController.popBackStack("first", inclusive = false)
                            }
                        )
                    }
                }
            }
        }
    }
}