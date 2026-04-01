package com.example.delivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.delivery.navigation.Screen
import com.example.delivery.screens.*
import com.example.delivery.ui.theme.DeliveryTheme

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            DeliveryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route
                    ) {
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
                            DeliveryTrackingScreenWithDetails(
                                driverId = 5, // Change to 5 for testing, or get from user session
                                navController = navController,
                                onNavigateToDelivery = { delivery ->
                                    // Navigate to delivery details if needed
                                    navController.navigate("order_details/${delivery.shipmentId}")
                                },
                                onNavigateToMap = { delivery ->
                                    // Open maps with delivery address
                                    // You can implement map navigation here
                                },
                                onValidationClick = { delivery ->
                                    // Navigate to delivery validation screen with shipmentId
                                    navController.navigate(Screen.DeliveryValidation.createRoute(delivery.shipmentId))
                                },
                                onCallClick = { delivery ->
                                    // Handle call click
                                    val phoneNumber = delivery.clientPhone
                                    if (phoneNumber != null) {
                                        println("📞 APPEL du numéro: $phoneNumber")
                                        try {
                                            // Essayer avec ACTION_CALL pour appeler directement
                                            val callIntent = android.content.Intent(android.content.Intent.ACTION_CALL).apply {
                                                data = android.net.Uri.parse("tel:$phoneNumber")
                                            }
                                            ContextCompat.startActivity(this@MainActivity, callIntent, null)
                                        } catch (e: Exception) {
                                            try {
                                                // Fallback: ACTION_DIAL pour ouvrir l'application téléphone
                                                val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                    data = android.net.Uri.parse("tel:$phoneNumber")
                                                }
                                                ContextCompat.startActivity(this@MainActivity, dialIntent, null)
                                            } catch (e2: Exception) {
                                                // Dernier recours: copier le numéro
                                                val clipboardManager = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clip = android.content.ClipData.newPlainText("Phone Number", phoneNumber)
                                                clipboardManager.setPrimaryClip(clip)
                                                android.widget.Toast.makeText(
                                                    this@MainActivity,
                                                    "Numéro copié: $phoneNumber",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } else {
                                        android.widget.Toast.makeText(
                                            this@MainActivity,
                                            "Aucun numéro de téléphone disponible",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onBackPressed = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("delivery?date={date}") { backStackEntry ->
                            val date = backStackEntry.arguments?.getString("date")
                            DeliveryTrackingScreenWithDetails(
                                driverId = 5, // Change to 5 for testing, or get from user session
                                navController = navController,
                                selectedDate = date, // Passer la date spécifique
                                onNavigateToDelivery = { delivery ->
                                    // Navigate to delivery details if needed
                                    navController.navigate("order_details/${delivery.shipmentId}")
                                },
                                onNavigateToMap = { delivery ->
                                    // Open maps with delivery address
                                    // You can implement map navigation here
                                },
                                onValidationClick = { delivery ->
                                    // Navigate to delivery validation screen with shipmentId
                                    navController.navigate(Screen.DeliveryValidation.createRoute(delivery.shipmentId))
                                },
                                onCallClick = { delivery ->
                                    // Handle call click
                                    val phoneNumber = delivery.clientPhone
                                    if (phoneNumber != null) {
                                        println("📞 APPEL du numéro: $phoneNumber")
                                        try {
                                            // Essayer avec ACTION_CALL pour appeler directement
                                            val callIntent = android.content.Intent(android.content.Intent.ACTION_CALL).apply {
                                                data = android.net.Uri.parse("tel:$phoneNumber")
                                            }
                                            ContextCompat.startActivity(this@MainActivity, callIntent, null)
                                        } catch (e: Exception) {
                                            try {
                                                // Fallback: ACTION_DIAL pour ouvrir l'application téléphone
                                                val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                    data = android.net.Uri.parse("tel:$phoneNumber")
                                                }
                                                ContextCompat.startActivity(this@MainActivity, dialIntent, null)
                                            } catch (e2: Exception) {
                                                // Dernier recours: copier le numéro
                                                val clipboardManager = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clip = android.content.ClipData.newPlainText("Phone Number", phoneNumber)
                                                clipboardManager.setPrimaryClip(clip)
                                                android.widget.Toast.makeText(
                                                    this@MainActivity,
                                                    "Numéro copié: $phoneNumber",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } else {
                                        android.widget.Toast.makeText(
                                            this@MainActivity,
                                            "Aucun numéro de téléphone disponible",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onBackPressed = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(Screen.POD.route) {
                            PODScreen(navController = navController)
                        }
                        composable(Screen.Profile.route) {
                            ProfileScreen(navController = navController)
                        }
                        composable(Screen.History.route) {
                            HistoryScreen(navController = navController)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(navController = navController)
                        }
                        composable(Screen.ThemeSettings.route) {
                            ThemeSettingsScreen(navController = navController)
                        }
                        composable("order_details/{orderId}") { backStackEntry ->
                            OrderDetailsScreen(navController = navController)
                        }
                        composable(Screen.TripTest.route) {
                            TripTestScreen(navController = navController)
                        }
                        composable(Screen.DeliveryTest.route) {
                            DeliveryTrackingTestScreen(
                                onBackPressed = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(Screen.DateFilterTest.route) {
                            DateFilterTestScreen(
                                onBackPressed = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            route = Screen.DeliveryValidation.route,
                            arguments = listOf(
                                androidx.navigation.navArgument("shipmentId") {
                                    type = androidx.navigation.NavType.IntType
                                }
                            )
                        ) { backStackEntry ->
                            val shipmentId = backStackEntry.arguments?.getInt("shipmentId")
                            DeliveryValidationScreen(
                                navController = navController,
                                shipmentId = shipmentId
                            )
                        }
                        composable("${Screen.TripDetail.route}/{tripId}") { backStackEntry ->
                            val tripId = backStackEntry.arguments?.getString("tripId")?.toIntOrNull() ?: 0
                            TripDetailScreen(
                                tripId = tripId,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}
