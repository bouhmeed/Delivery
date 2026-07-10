package com.example.delivery.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.delivery.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Tour,
        Screen.Delivery,
        Screen.History,
        Screen.Profile
    )

    NavigationBar(
        containerColor = Color(0xFFEAF2F8)
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { screen ->
            // Vérifier si la route actuelle correspond à l'écran
            val isSelected = when (screen.route) {
                "delivery" -> currentRoute?.startsWith("delivery") == true
                "order_details" -> currentRoute?.startsWith("order_details") == true
                "deliveryValidation" -> currentRoute?.startsWith("deliveryValidation") == true
                else -> currentRoute == screen.route
            }

            NavigationBarItem(
                icon = { 
                    Icon(
                        when (screen.route) {
                            "home" -> Icons.Default.Home
                            "tour" -> Icons.Default.Map
                            "delivery" -> Icons.Default.LocalShipping
                            "history" -> Icons.Default.History
                            "profile" -> Icons.Default.Person
                            else -> Icons.Default.Home
                        },
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF4A90E2) else Color(0xFF6B7280)
                    )
                },
                label = { 
                    Text(
                        when (screen.route) {
                            "home" -> "Accueil"
                            "tour" -> "Tournée"
                            "delivery" -> "Suivi mes Tournées"
                            "history" -> "Historique"
                            "profile" -> "Profil"
                            else -> "Accueil"
                        }
                    )
                },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4A90E2),
                    unselectedIconColor = Color(0xFF6B7280),
                    selectedTextColor = Color(0xFF4A90E2),
                    unselectedTextColor = Color(0xFF6B7280),
                    indicatorColor = Color(0xFFE3F2FD)
                ),
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
