package com.example.delivery.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Tour : Screen("tour")
    object Delivery : Screen("delivery")
    object POD : Screen("pod")
    object Profile : Screen("profile")
    object History : Screen("history")
    object Settings : Screen("settings")
    object Help : Screen("help")
    object TripTest : Screen("tripTest")
    object TripDetail : Screen("tripDetail")
}
