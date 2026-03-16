package com.example.delivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.delivery.auth.AuthManager
import com.example.delivery.navigation.Screen
import com.example.delivery.screens.*
import com.example.delivery.ui.theme.DeliveryTheme

class MainActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager(this)
        
        setContent {
            DeliveryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Collect authentication state
                    val isLoggedIn by authManager.isLoggedIn.collectAsState()
                    
                    // Determine start destination based on authentication state
                    val startDestination = if (isLoggedIn) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }
                    
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(navController = navController, authManager = authManager)
                        }
                        composable(Screen.Home.route) {
                            HomeScreen(navController = navController)
                        }
                        composable(Screen.Tour.route) {
                            TourneeScreen(navController = navController)
                        }
                        composable(Screen.Delivery.route) {
                            OrdersListScreen(navController = navController)
                        }
                        composable(Screen.POD.route) {
                            PODScreen(navController = navController)
                        }
                        composable(Screen.Profile.route) {
                            SettingsScreen(navController = navController, authManager = authManager)
                        }
                        composable(Screen.History.route) {
                            DeliveryHistoryScreen(navController = navController)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(navController = navController, authManager = authManager)
                        }
                        composable("order_details/{orderId}") { backStackEntry ->
                            OrderDetailsScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
