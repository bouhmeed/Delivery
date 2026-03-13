package com.example.delivery

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.delivery.navigation.Screen
import com.example.delivery.screens.*
import com.example.delivery.ui.theme.DeliveryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            DeliveryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation()
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthCallback(intent)
    }
    
    private fun handleAuthCallback(intent: Intent) {
        val uri = intent.data
        if (uri != null && uri.toString().startsWith("delivery://auth0/callback")) {
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            
            if (code != null) {
                // Handle successful authentication
                // You can store the code and state for later use
                // Or directly exchange for tokens here
                println("Auth0 callback received: code=$code, state=$state")
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
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
        composable("order_details/{orderId}") {
            OrderDetailsScreen(navController = navController)
        }
        composable(Screen.History.route) {
            DeliveryHistoryScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.POD.route) {
            PODScreen(navController = navController)
        }
    }
}