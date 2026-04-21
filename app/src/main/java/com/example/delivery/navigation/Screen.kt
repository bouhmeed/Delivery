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
    object ThemeSettings : Screen("theme_settings")
    object Help : Screen("help")
    object TripTest : Screen("tripTest")
    object TripDetail : Screen("tripDetail")
    object DeliveryTest : Screen("deliveryTest")
    object DateFilterTest : Screen("dateFilterTest")
    object DeliveryValidation : Screen("deliveryValidation/{shipmentId}") {
        fun createRoute(shipmentId: Int) = "deliveryValidation/$shipmentId"
    }
    object DriverMap : Screen("driverMap/{shipmentId}") {
        fun createRoute(shipmentId: Int) = "driverMap/$shipmentId"
    }
    object ShipmentDetail : Screen("shipmentDetail/{shipmentId}/{driverId}") {
        fun createRoute(shipmentId: Int, driverId: Int) = "shipmentDetail/$shipmentId/$driverId"
    }
}
