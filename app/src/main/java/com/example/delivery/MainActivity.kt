package com.example.delivery

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.delivery.navigation.Screen
import com.example.delivery.screens.main.HomeScreen
import com.example.delivery.screens.main.LoginScreen
import com.example.delivery.screens.delivery.TourneeScreen
import com.example.delivery.screens.delivery.DeliveryTrackingScreenWithDetails
import com.example.delivery.screens.delivery.PODScreen
import com.example.delivery.screens.user.ProfileScreen
import com.example.delivery.screens.history.NewHistoryScreen
import com.example.delivery.screens.user.SettingsScreen
import com.example.delivery.screens.user.ThemeSettingsScreen
import com.example.delivery.screens.delivery.OrderDetailsScreen
import com.example.delivery.screens.trip.TripTestScreen
import com.example.delivery.screens.test.DeliveryTrackingTestScreen
import com.example.delivery.screens.test.DateFilterTestScreen
import com.example.delivery.screens.delivery.DeliveryValidationScreen
import com.example.delivery.screens.delivery.ReturnsScreen
import com.example.delivery.screens.trip.TripDetailScreen
import com.example.delivery.screens.delivery.NewShipmentDetailScreen
import com.example.delivery.ui.theme.DeliveryTheme
import kotlinx.coroutines.launch
import com.example.delivery.database.DatabaseManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🧪 TEST MINIMAL: Neon Data API avec JWT
        testNeonDataApi()

        setContent {
            DeliveryTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
                                navController = navController,
                                onBackPressed = {
                                    navController.popBackStack()
                                },
                                onNavigateToDelivery = { delivery ->
                                    navController.navigate("order_details/${delivery.shipmentId}")
                                },
                                onNavigateToMap = { delivery ->
                                    navController.navigate(Screen.DriverMap.createRoute(delivery.shipmentId))
                                },
                                onValidationClick = { delivery ->
                                    navController.navigate(Screen.DeliveryValidation.createRoute(delivery.shipmentId))
                                },
                                onCallClick = { delivery ->
                                    handleCall(delivery.clientPhone)
                                }
                            )
                        }

                        composable("delivery?date={date}") { backStackEntry ->
                            val date = backStackEntry.arguments?.getString("date")
                            DeliveryTrackingScreenWithDetails(
                                navController = navController,
                                selectedDate = date,
                                onNavigateToDelivery = { delivery ->
                                    navController.navigate("order_details/${delivery.shipmentId}")
                                },
                                onNavigateToMap = { delivery ->
                                    navController.navigate(Screen.DriverMap.createRoute(delivery.shipmentId))
                                },
                                onValidationClick = { delivery ->
                                    navController.navigate(Screen.DeliveryValidation.createRoute(delivery.shipmentId))
                                },
                                onCallClick = { delivery ->
                                    handleCall(delivery.clientPhone)
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

                        composable("order_details/{orderId}") {
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
                            
                            // Mock delivery item for mapping screen if needed
                            val mockDelivery = com.example.delivery.models.delivery.DeliveryItem(
                                sequence = 1,
                                shipmentId = shipmentId ?: 0,
                                status = "TO_PLAN",
                                podDone = false,
                                shipmentNo = "SHIP-$shipmentId",
                                type = "OUTBOUND",
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
                                latitude = 48.8566,
                                longitude = 2.3522
                            )
                            
                            // DriverMapScreen(
                            //    delivery = mockDelivery,
                            //    onBackPressed = { navController.popBackStack() },
                            //    navController = navController
                            // )
                        }

                        composable(
                            route = Screen.ShipmentDetail.route,
                            arguments = listOf(
                                androidx.navigation.navArgument("shipmentId") {
                                    type = androidx.navigation.NavType.IntType
                                },
                                androidx.navigation.navArgument("driverId") {
                                    type = androidx.navigation.NavType.IntType
                                }
                            )
                        ) { backStackEntry ->
                            val shipmentId = backStackEntry.arguments?.getInt("shipmentId") ?: 0
                            val driverId = backStackEntry.arguments?.getInt("driverId") ?: 0
                            
                            NewShipmentDetailScreen(
                                shipmentId = shipmentId,
                                driverId = driverId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToMap = { address ->
                                    // Map navigation logic here
                                },
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleCall(phoneNumber: String?) {
        if (phoneNumber != null) {
            try {
                val callIntent = android.content.Intent(android.content.Intent.ACTION_CALL).apply {
                    data = android.net.Uri.parse("tel:$phoneNumber")
                }
                startActivity(callIntent)
            } catch (e: Exception) {
                try {
                    val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:$phoneNumber")
                    }
                    startActivity(dialIntent)
                } catch (e2: Exception) {
                    val clipboardManager = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Phone Number", phoneNumber)
                    clipboardManager.setPrimaryClip(clip)
                    android.widget.Toast.makeText(this, "Numéro copié: $phoneNumber", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            android.widget.Toast.makeText(this, "Aucun numéro de téléphone disponible", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun testNeonDataApi() {
        lifecycleScope.launch {
            // STEP 5: RUN BASIC TESTS
            DatabaseManager.testConnection()
            DatabaseManager.testCountDrivers()
            DatabaseManager.testListDrivers()
            DatabaseManager.testFullDriverTable()
            DatabaseManager.testListShipments()
            
            // STEP 6: RUN HOME SCREEN TESTS
            DatabaseManager.testGetUserByEmail()
            DatabaseManager.testGetDriverById()
            DatabaseManager.testGetVehicleByDriverId()
            DatabaseManager.testGetVehicleMaintenance()
        }
    }
}
