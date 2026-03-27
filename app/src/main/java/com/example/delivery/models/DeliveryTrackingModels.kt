package com.example.delivery.models

import com.google.gson.annotations.SerializedName

// Shipment Model - Livraison (specific for delivery tracking)
data class DeliveryShipment(
    val id: Int,
    @SerializedName("shipmentNo")
    val shipmentNo: String,
    @SerializedName("customerId")
    val customerId: Int?,
    @SerializedName("destinationId")
    val destinationId: Int,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String?,
    @SerializedName("deliveryCity")
    val deliveryCity: String?,
    @SerializedName("deliveryZipCode")
    val deliveryZipCode: String?,
    @SerializedName("deliveryCountry")
    val deliveryCountry: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("distanceKm")
    val distanceKm: Double?,
    @SerializedName("estimatedDuration")
    val estimatedDuration: Int?,
    @SerializedName("description")
    val description: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("uom")
    val uom: String
)

// Client Model
data class Client(
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("address")
    val address: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("postalCode")
    val postalCode: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("contact")
    val contact: String?
)

// Location Model
data class Location(
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("address")
    val address: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("postalCode")
    val postalCode: String?
)

// Combined Model for UI - Delivery Item
data class DeliveryItem(
    val sequence: Int,
    val shipmentId: Int,
    val status: String,
    val podDone: Boolean,
    val shipmentNo: String,
    val destinationId: Int,
    val deliveryAddress: String,
    val deliveryCity: String,
    val deliveryZipCode: String,
    val deliveryCountry: String,
    val clientName: String?,
    val clientPhone: String?,
    val fullAddress: String?,
    val locationCity: String?,
    val locationPostalCode: String?,
    val distanceKm: Double?,
    val estimatedDuration: Int?,
    val description: String,
    val quantity: Int,
    val uom: String,
    val tripIdentifier: String? = null // Added for display purposes
)

// Trip with Deliveries Response
data class TripWithDeliveries(
    val trip: com.example.delivery.models.Trip?,
    val deliveries: List<DeliveryItem>,
    val date: String
)

// Today's Trip Response
data class TodayTripResponse(
    val trip: com.example.delivery.models.Trip?,
    val exists: Boolean
)

// Status Update Request
data class StatusUpdateRequest(
    val status: String
)
