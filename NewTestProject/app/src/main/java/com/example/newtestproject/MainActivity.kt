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
import android.net.Uri
import com.example.newtestproject.screen.screenComponents.LanguageButton
import com.example.newtestproject.screen.screenComponents.ProfileButton
import com.example.newtestproject.util.LanguageManager
import com.example.newtestproject.util.LanguagePrefs
import com.example.newtestproject.screen.AuthScreen
import com.example.newtestproject.screen.FirstScreen
import com.example.newtestproject.screen.GreetingScreen
import com.example.newtestproject.screen.OrdersScreen
import com.example.newtestproject.screen.ProfileScreen
import com.example.newtestproject.screen.UserPortfolioScreen
import com.example.newtestproject.screen.UsersPortfolioScreen
import com.example.newtestproject.screen.CartScreen
import com.example.newtestproject.screen.ProductDetailScreen
import com.example.newtestproject.screen.screenComponents.OrdersButton
import com.example.newtestproject.screen.screenComponents.PortfolioButton
import com.example.newtestproject.screen.screenComponents.CartButton
import com.example.newtestproject.util.CartStore

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
            composable("portfolios") {
                UsersPortfolioScreen(
                    onGoToOrders = {
                        navController.navigate("orders") {
                            launchSingleTop = true
                        }
                    },
                    onOpenUserPortfolio = { userId, userLabel, sellerKey ->
                        val encodedLabel = Uri.encode(userLabel)
                        val encodedSellerKey = Uri.encode(sellerKey)
                        navController.navigate("portfolio/$userId/$encodedLabel/$encodedSellerKey")
                    }
                )
            }
            composable(
                route = "portfolio/{userId}/{userLabel}/{sellerKey}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.LongType },
                    navArgument("userLabel") { type = NavType.StringType },
                    navArgument("sellerKey") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
                val userLabel = backStackEntry.arguments?.getString("userLabel") ?: ""
                val sellerKey = backStackEntry.arguments?.getString("sellerKey") ?: ""
                UserPortfolioScreen(
                    userId = userId,
                    userLabel = userLabel,
                    sellerKey = sellerKey,
                    onBackToPortfolios = { navController.popBackStack() },
                    onOpenProduct = { productId ->
                        navController.navigate("product/$productId")
                    }
                )
            }
            composable(
                route = "product/{productId}",
                arguments = listOf(
                    navArgument("productId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                ProductDetailScreen(
                    productId = productId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("cart") {
                CartScreen(
                    onBack = { navController.popBackStack() }
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
    val showPortfolios = currentRoute != "first" && currentRoute != "auth" && currentRoute != "portfolios"
    val showOrders = currentRoute != "first" && currentRoute != "auth" && currentRoute != "orders"
    val showCart = currentRoute != "first" && currentRoute != "auth" && currentRoute != "cart"

    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        actions = {
            if (showPortfolios) PortfolioButton(navController)
            if (showOrders) OrdersButton(navController)
            if (showCart) CartButton(navController)
            if (showProfile) ProfileButton(navController)

            LanguageButton()
        }
    )
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val savedLang = LanguagePrefs.loadLanguage(this)
        LanguageManager.applyLanguage(this, savedLang)

        super.onCreate(savedInstanceState)
        CartStore.initialize(applicationContext)
        setContent {
            MyApp()
        }
    }
}
