package com.example.newtestproject

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.newtestproject.screen.screenComponents.LanguageButton
import com.example.newtestproject.screen.screenComponents.ProfileButton
import com.example.newtestproject.util.LanguageManager
import com.example.newtestproject.util.LanguagePrefs
import com.example.newtestproject.screen.AuthScreen
import com.example.newtestproject.screen.FirstScreen
import com.example.newtestproject.screen.GreetingScreen
import com.example.newtestproject.screen.OrdersScreen
import com.example.newtestproject.screen.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            MyTopAppBar(navController = navController)
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
                        navController.navigate("orders") {
                            popUpTo("first") { inclusive = false }
                        }
                    },
                    onRegisterSuccess = { fullName ->
                        navController.navigate("orders") {
                            popUpTo("first") { inclusive = false }
                        }
                    }
                )
            }
            composable("orders") {
                OrdersScreen(
                    onLogout = {
                        navController.popBackStack("first", inclusive = false)
                    }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onLogout = {
                        navController.popBackStack("first", inclusive = false)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showProfile = currentRoute != "first" && currentRoute != "auth"

    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        actions = {
            if (showProfile) {
                ProfileButton(navController)
            }
            LanguageButton()
        }
    )
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val savedLang = LanguagePrefs.loadLanguage(this)
        LanguageManager.applyLanguage(this, savedLang)

        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}