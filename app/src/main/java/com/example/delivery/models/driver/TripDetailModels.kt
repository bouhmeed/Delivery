package com.example.delivery.models.driver

import com.google.gson.annotations.SerializedName

// Modèle principal pour les détails complets d'un trajet
data class TripDetailResponse(
    val success: Boolean,
    val data: TripDetailData,
    val error: String? = null
)

data class TripDetailData(
    @SerializedName("trip") val trip: TripDetail,
    @SerializedName("shipments") val shipments: List<ShipmentDetail>,
    @SerializedName("stops") val stops: List<TripStopDetail>,
    @SerializedName("driver") val driver: DriverDetail,
    @SerializedName("vehicle") val vehicle: VehicleDetail,
    @SerializedName("depot") val depot: LocationDetail
)

data class TripDetail(
    val id: Int,
    @SerializedName("tripId") val tripId: String,
    @SerializedName("tripDate") val tripDate: String,
    @SerializedName("depotId") val depotId: Int,
    @SerializedName("driverId") val driverId: Int?,
    @SerializedName("vehicleId") val vehicleId: Int?,
    val status: String,
    @SerializedName("tenantId") val tenantId: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("totalShipments") val totalShipments: Int,
    @SerializedName("completedShipments") val completedShipments: Int,
    @SerializedName("totalDistance") val totalDistance: Double,
    @SerializedName("estimatedDuration") val estimatedDuration: Int,
    @SerializedName("actualDuration") val actualDuration: Int?
)

data class ShipmentDetail(
    val id: Int,
    @SerializedName("shipmentNo") val shipmentNo: String?,
    @SerializedName("customerId") val customerId: Int?,
    val type: String,
    @SerializedName("originId") val originId: Int,
    @SerializedName("destinationId") val destinationId: Int,
    val priority: String,
    @SerializedName("requestedPickup") val requestedPickup: String?,
    @SerializedName("requestedDelivery") val requestedDelivery: String?,
    val status: String,
    val description: String,
    val quantity: Int,
    val uom: String,
    @SerializedName("packaging") val packaging: String?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("volume") val volume: Double?,
    @SerializedName("stackable") val stackable: Boolean?,
    @SerializedName("carrier") val carrier: String?,
    @SerializedName("trackingNumber") val trackingNumber: String?,
    @SerializedName("deliveryAddress") val deliveryAddress: String?,
    @SerializedName("deliveryCity") val deliveryCity: String?,
    @SerializedName("deliveryZipCode") val deliveryZipCode: String?,
    @SerializedName("deliveryCountry") val deliveryCountry: String,
    @SerializedName("driverId") val driverId: Int?,
    @SerializedName("vehicleId") val vehicleId: Int?,
    @SerializedName("estimatedDuration") val estimatedDuration: Int?,
    @SerializedName("plannedEnd") val plannedEnd: String?,
    @SerializedName("plannedStart") val plannedStart: String?,
    @SerializedName("distanceKm") val distanceKm: Double?,
    @SerializedName("tripSequence") val tripSequence: Int?,
    @SerializedName("shipmentRole") val shipmentRole: String?,
    @SerializedName("linkStatus") val linkStatus: String?,
    @SerializedName("podDone") val podDone: Boolean?,
    @SerializedName("returnsDone") val returnsDone: Boolean?,
    @SerializedName("customerName") val customerName: String?,
    @SerializedName("customerAddress") val customerAddress: String?,
    @SerializedName("customerCity") val customerCity: String?,
    @SerializedName("customerPhone") val customerPhone: String?,
    @SerializedName("originName") val originName: String?,
    @SerializedName("originAddress") val originAddress: String?,
    @SerializedName("originCity") val originCity: String?,
    @SerializedName("destinationName") val destinationName: String?,
    @SerializedName("destinationAddress") val destinationAddress: String?,
    @SerializedName("destinationCity") val destinationCity: String?,
    @SerializedName("vehicleName") val vehicleName: String?,
    @SerializedName("vehicleRegistration") val vehicleRegistration: String?,
    @SerializedName("origin") val origin: LocationDetail,
    @SerializedName("destination") val destination: LocationDetail,
    @SerializedName("customer") val customer: ClientDetail?,
    @SerializedName("executionStatus") val executionStatus: TripShipmentExecutionStatus?
)

data class TripStopDetail(
    val id: Int,
    @SerializedName("tripId") val tripId: Int,
    val sequence: Int,
    @SerializedName("locationId") val locationId: Int,
    @SerializedName("stopType") val stopType: String,
    @SerializedName("locationName") val locationName: String?,
    @SerializedName("locationAddress") val locationAddress: String?,
    @SerializedName("locationCity") val locationCity: String?,
    @SerializedName("locationPostalCode") val locationPostalCode: String?,
    @SerializedName("estimatedArrival") val estimatedArrival: String?,
    @SerializedName("actualArrival") val actualArrival: String?,
    @SerializedName("estimatedDeparture") val estimatedDeparture: String?,
    @SerializedName("actualDeparture") val actualDeparture: String?,
    @SerializedName("shipments") val shipments: List<ShipmentDetail> = emptyList()
)

data class DriverDetail(
    val id: Int,
    val name: String,
    @SerializedName("licenseNumber") val licenseNumber: String?,
    @SerializedName("employmentType") val employmentType: String?,
    val status: String,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("postalCode") val postalCode: String?
)

data class VehicleDetail(
    val id: Int,
    val name: String,
    val registration: String,
    @SerializedName("capacityWeight") val capacityWeight: Double,
    @SerializedName("capacityVolume") val capacityVolume: Double,
    val type: String?,
    val status: String?,
    @SerializedName("year") val year: Int
)

data class LocationDetail(
    val id: Int,
    val name: String,
    val address: String?,
    val city: String?,
    @SerializedName("postalCode") val postalCode: String?,
    val country: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("locationType") val locationType: String?
)

data class ClientDetail(
    val id: Int,
    val name: String,
    val address: String?,
    val city: String?,
    @SerializedName("postalCode") val postalCode: String?,
    val phone: String?,
    val email: String?,
    val contact: String?
)

data class TripShipmentExecutionStatus(
    @SerializedName("tripShipmentLinkId") val tripShipmentLinkId: Int,
    val role: String,
    val status: String,
    @SerializedName("podDone") val podDone: Boolean,
    @SerializedName("returnsDone") val returnsDone: Boolean,
    val sequence: Int,
    @SerializedName("updatedAt") val updatedAt: String
)

data class TripListResponse(
    val success: Boolean,
    val data: TripListData,
    val error: String? = null
)

data class TripListData(
    @SerializedName("trips") val trips: List<TripSummary>,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class TripSummary(
    val id: Int,
    @SerializedName("tripId") val tripId: String,
    @SerializedName("tripDate") val tripDate: String,
    val status: String,
    @SerializedName("driverName") val driverName: String?,
    @SerializedName("vehicleName") val vehicleName: String?,
    @SerializedName("depotName") val depotName: String,
    @SerializedName("totalShipments") val totalShipments: Int,
    @SerializedName("completedShipments") val completedShipments: Int,
    @SerializedName("totalDistance") val totalDistance: Double,
    @SerializedName("estimatedDuration") val estimatedDuration: Int,
    @SerializedName("completionPercentage") val completionPercentage: Int
)

enum class TripDateFilter(val displayName: String) {
    @SerializedName("today") TODAY("Aujourd'hui"),
    @SerializedName("upcoming") UPCOMING("À venir"),
    @SerializedName("past") PAST("Passés"),
    @SerializedName("all") ALL("Tous")
}

enum class TripStatusFilter(val displayName: String) {
    @SerializedName("planning") PLANNING("Planification"),
    @SerializedName("ready") READY("Prêt"),
    @SerializedName("in_progress") IN_PROGRESS("En cours"),
    @SerializedName("completed") COMPLETED("Terminé"),
    @SerializedName("cancelled") CANCELLED("Annulé"),
    @SerializedName("all") ALL("Tous")
}
