package com.example.delivery.models

data class TripHistory(
    val history: List<DeliveryHistoryItem>,
    val pagination: PaginationInfo
)

data class DeliveryHistoryItem(
    // Trip Information
    val id: String,
    val tripDate: String,
    val tripNumber: String?,
    val tripStatus: String,
    
    // Shipment Information
    val shipmentId: String,
    val shipmentNumber: String?,
    val shipmentStatus: String,
    val shipmentDescription: String,
    val quantity: Int,
    val uom: String,
    
    // Client Information
    val clientName: String?,
    val clientAddress: String?,
    val clientCity: String?,
    val clientPostalCode: String?,
    
    // Location Information
    val originName: String?,
    val originCity: String?,
    val originAddress: String?,
    val destinationName: String?,
    val destinationCity: String?,
    val destinationAddress: String?,
    
    // Vehicle Information
    val vehicleName: String?,
    val vehicleRegistration: String?,
    val vehicleType: String?,
    
    // Link Information
    val linkStatus: String?,
    val podDone: Boolean?,
    val sequence: Int?,
    
    // Driver Information
    val driverName: String?
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
    val totalShipments: Int,
    val pendingShipments: Int,
    val expeditionShipments: Int,
    val totalQuantity: Double,
    val totalWeight: Double,
    val averageWeight: Double,
    val lastTripDate: String?,
    val firstTripDate: String?,
    val successRate: Int
)

data class MonthlyTrend(
    val month: String,
    val trips: Int,
    val deliveries: Int,
    val completedTrips: Int,
    val deliveredShipments: Int,
    val totalQuantity: Double,
    val successRate: Double
)

data class PaginationInfo(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int
)
