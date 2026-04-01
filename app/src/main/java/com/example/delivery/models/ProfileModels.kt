package com.example.delivery.models

data class DriverProfile(
    val id: Int,
    val name: String,
    val licenseNumber: String?,
    val licenseExpiry: String?,
    val employmentType: String,
    val contractHoursWeek: Int,
    val homeDepotId: Int,
    val tenantId: Int,
    val status: String,
    val address: String?,
    val assignedVehicle: String?,
    val city: String?,
    val country: String?,
    val dateOfBirth: String?,
    val email: String?,
    val hireDate: String?,
    val phone: String?,
    val postalCode: String?,
    val salary: Double,
    val createdAt: String,
    val updatedAt: String
)

data class DriverStatsSummary(
    val driverId: Int,
    val totalTrips: Int,
    val completedTrips: Int,
    val totalShipments: Int,
    val deliveredShipments: Int,
    val totalQuantity: Double,
    val totalWeight: Double,
    val averageWeight: Double,
    val successRate: Int,
    val lastTripDate: String?,
    val firstTripDate: String?
)

data class VehicleInfo(
    val id: Int,
    val name: String,
    val registration: String,
    val capacityWeight: Double,
    val capacityVolume: Double,
    val type: String,
    val year: Int,
    val status: String
)

data class DepotInfo(
    val id: Int,
    val name: String,
    val address: String?,
    val city: String?,
    val postalCode: String?,
    val phone: String?,
    val email: String?
)

data class ProfileResponse(
    val profile: DriverProfile,
    val vehicle: VehicleInfo?,
    val depot: DepotInfo?
)
