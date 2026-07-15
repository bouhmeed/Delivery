package com.example.delivery.models.user

data class DriverProfile(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val email: String?,
    val licenseNumber: String?,
    val licenseExpiry: String?,
    val licenseIssueDate: String?,
    val employmentType: String,
    val contractHoursWeek: Int,
    val status: String,
    val tenantId: Int,
    val addressId: Int?,
    val dateOfBirth: String?,
    val hireDate: String?,
    val assignedVehicleId: Int?,
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
    val registrationNumber: String,
    val brand: String?,
    val model: String?,
    val category: String,
    val year: Int,
    val capacityWeight: Double?,
    val capacityVolume: Double?,
    val status: String,
    val fuelType: String,
    val hasLiftGate: Boolean,
    val hasRefrigeration: Boolean,
    val hasGPS: Boolean
)

data class AddressInfo(
    val id: Int,
    val label: String,
    val address1: String,
    val address2: String?,
    val city: String,
    val postalCode: String,
    val country: String,
    val contactName: String?,
    val contactPhone: String?
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
    val address: AddressInfo?
)
