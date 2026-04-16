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

import com.example.delivery.screens.LoginScreen

import com.example.delivery.screens.HomeScreen

import com.example.delivery.screens.TourneeScreen

import com.example.delivery.screens.DeliveryTrackingScreenWithDetails

import com.example.delivery.screens.PODScreen

import com.example.delivery.screens.main.ProfileScreen

import com.example.delivery.screens.NewHistoryScreen

import com.example.delivery.screens.SettingsScreen

import com.example.delivery.screens.ThemeSettingsScreen

import com.example.delivery.screens.OrderDetailsScreen

import com.example.delivery.screens.TripTestScreen

import com.example.delivery.screens.DeliveryTrackingTestScreen

import com.example.delivery.screens.DateFilterTestScreen

import com.example.delivery.screens.DeliveryValidationScreen

import com.example.delivery.screens.TripDetailScreen

// DriverMapScreen disabled - using web navigation instead
// import com.example.delivery.screens.DriverMapScreen

import com.example.delivery.screens.ReturnsScreen

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

                                onBackPressed = {

                                    navController.popBackStack()

                                },

                                onNavigateToDelivery = { delivery ->

                                    // Navigate to delivery details if needed

                                    navController.navigate("order_details/${delivery.shipmentId}")

                                },

                                onNavigateToMap = { delivery ->

                                    // Navigate to internal driver map screen

                                    navController.navigate(Screen.DriverMap.createRoute(delivery.shipmentId))

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

                                    // Navigate to internal driver map screen

                                    navController.navigate(Screen.DriverMap.createRoute(delivery.shipmentId))

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

                            NewHistoryScreen(navController = navController)

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

                        composable(

                            route = "returns/{shipmentId}",

                            arguments = listOf(

                                androidx.navigation.navArgument("shipmentId") {

                                    type = androidx.navigation.NavType.IntType

                                }

                            )

                        ) { backStackEntry ->

                            val shipmentId = backStackEntry.arguments?.getInt("shipmentId")

                            ReturnsScreen(

                                navController = navController,

                                shipmentId = shipmentId ?: 0

                            )

                        }

                        composable("${Screen.TripDetail.route}/{tripId}") { backStackEntry ->

                            val tripId = backStackEntry.arguments?.getString("tripId")?.toIntOrNull() ?: 0

                            TripDetailScreen(

                                tripId = tripId,

                                navController = navController

                            )

                        }

                        composable(

                            route = Screen.DriverMap.route,

                            arguments = listOf(

                                androidx.navigation.navArgument("shipmentId") {

                                    type = androidx.navigation.NavType.IntType

                                }

                            )

                        ) { backStackEntry ->

                            val shipmentId = backStackEntry.arguments?.getInt("shipmentId")

                            // Create a mock delivery item for navigation

                            // In a real app, you would fetch this from your repository

                            val mockDelivery = com.example.delivery.models.DeliveryItem(

                                sequence = 1,

                                shipmentId = shipmentId ?: 0,

                                status = "TO_PLAN",

                                podDone = false,

                                shipmentNo = "SHIP-$shipmentId",

                                destinationId = 1,

                                deliveryAddress = "Adresse de livraison",

                                deliveryCity = "Paris",

                                deliveryZipCode = "75000",

                                deliveryCountry = "France",

                                clientName = "Client Test",

                                clientPhone = "+33612345678",

                                fullAddress = "Adresse de livraison, Paris 75000",

                                locationCity = "Paris",

                                locationPostalCode = "75000",

                                distanceKm = 5.0,

                                estimatedDuration = 15,

                                quantity = 1,

                                uom = "unité",

                                latitude = 48.8566, // Default coordinates (Paris)

                                longitude = 2.3522

                            )

                            // DriverMapScreen disabled - using web navigation instead
                            /*
                            DriverMapScreen(

                                delivery = mockDelivery,

                                onBackPressed = {

                                    navController.popBackStack()

                                },

                                navController = navController

                            )
                            */

                        }

                    }

                }

            }

        }

    }

}

