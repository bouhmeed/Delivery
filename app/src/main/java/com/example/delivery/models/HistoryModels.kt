package com.example.delivery.models

data class TripHistory(
    val history: List<TripHistoryItem>,
    val pagination: PaginationInfo
)

data class TripHistoryItem(
    val id: String,
    val tripDate: String,
    val vehicleName: String?,
    val licensePlate: String?,
    val tripStatus: String,
    val shipmentCount: Int,
    val totalQuantity: Int,
    val startLocation: String?,
    val endLocation: String?
)

data class DriverStats(
    val stats: DriverStatsInfo,
    val monthlyTrends: List<MonthlyTrend>
)

data class DriverStatsInfo(
    val driverId: String,
    val totalTrips: Int,
    val completedTrips: Int,
    val deliveredShipments: Int,
    val totalDistance: Double,
    val averageRating: Double?
)

data class MonthlyTrend(
    val month: String,
    val trips: Int,
    val deliveries: Int,
    val successRate: Double
)

data class PaginationInfo(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int
)
