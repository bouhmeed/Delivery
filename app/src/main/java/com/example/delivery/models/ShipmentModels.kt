package com.example.delivery.models

import com.google.gson.annotations.SerializedName

// Modèle pour une réponse de recherche de livraison
data class ShipmentSearchResponse(
    val success: Boolean,
    val data: ShipmentSearchData?,
    val message: String? = null
)

data class ShipmentSearchData(
    val shipment: ShipmentSearchDetail,
    val belongsToCurrentTour: Boolean,
    val tourSequence: Int? = null,
    val client: ClientInfo? = null
)

// Modèle détaillé pour une livraison
data class ShipmentSearchDetail(
    val id: Int,
    @SerializedName("shipmentNo")
    val shipmentNo: String,
    @SerializedName("trackingNumber")
    val trackingNumber: String?,
    val status: String,
    val description: String,
    val quantity: Int,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String?,
    @SerializedName("deliveryCity")
    val deliveryCity: String?,
    @SerializedName("deliveryZipCode")
    val deliveryZipCode: String?,
    @SerializedName("customerId")
    val customerId: Int,
    val priority: String,
    @SerializedName("plannedStart")
    val plannedStart: String? = null,
    @SerializedName("plannedEnd")
    val plannedEnd: String? = null
)

// Informations du client
data class ClientInfo(
    val id: Int,
    val name: String,
    val address: String,
    val city: String,
    val postalCode: String,
    val phone: String? = null
)

// État de la recherche pour le ViewModel
sealed class ShipmentSearchState {
    object Idle : ShipmentSearchState()
    object Loading : ShipmentSearchState()
    data class Success(val data: ShipmentSearchData) : ShipmentSearchState()
    data class Error(val message: String) : ShipmentSearchState()
}

// État du scanner
sealed class ScannerState {
    object Idle : ScannerState()
    object Scanning : ScannerState()
    data class Success(val barcode: String) : ScannerState()
    data class Error(val message: String) : ScannerState()
}

// Modèle pour l'entrée manuelle
data class ManualSearchRequest(
    val barcode: String,
    val driverId: Int
)
