package com.example.delivery.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        when (screen.route) {
                            "home" -> Icons.Default.Home
                            "tour" -> Icons.Default.DirectionsCar
                            "delivery" -> Icons.Default.LocalShipping
                            "history" -> Icons.Default.History
                            "profile" -> Icons.Default.Person
                            else -> Icons.Default.Home
                        },
                        contentDescription = null
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
                selected = currentRoute == screen.route,
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
