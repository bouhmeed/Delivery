package com.example.delivery.models.delivery

import com.google.gson.annotations.SerializedName

data class ShipmentDetailResponse(
    val success: Boolean,
    val data: ShipmentDetailData,
    val error: String? = null
)

data class ShipmentDetailData(
    @SerializedName("shipment")
    val shipment: ShipmentDetailFull
)

data class ShipmentDetailFull(
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
    val weight: Double?,
    val volume: Double?,
    @SerializedName("stackable") val stackable: Boolean?,
    val carrier: String?,
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
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("customer") val customer: CustomerDetail?,
    @SerializedName("origin") val origin: ShipmentLocationDetail?,
    @SerializedName("destination") val destination: ShipmentLocationDetail?,
    @SerializedName("driver") val driver: DriverSimple?,
    @SerializedName("vehicle") val vehicle: VehicleSimple?,
    @SerializedName("trip") val trip: TripShipmentInfo?,
    @SerializedName("deliveryImages") val deliveryImages: List<DeliveryImage>,
    @SerializedName("deliveryDocuments") val deliveryDocuments: List<DeliveryDocument>,
    @SerializedName("deliveryProof") val deliveryProof: DeliveryProof?
)

data class CustomerDetail(
    val id: Int, val name: String?, val address: String?, val city: String?,
    @SerializedName("postalCode") val postalCode: String?,
    val phone: String?, val email: String?, val contact: String?
)

data class ShipmentLocationDetail(
    val id: Int, val name: String?, val address: String?, val city: String?,
    @SerializedName("postalCode") val postalCode: String?
)

data class DriverSimple(val id: Int, val name: String?, val phone: String?)

data class VehicleSimple(val id: Int, val name: String?, val registration: String?)

data class TripShipmentInfo(
    val id: Int,
    @SerializedName("tripId") val tripId: String?,
    @SerializedName("tripDate") val tripDate: String?,
    val status: String?, val sequence: Int?, val role: String?,
    @SerializedName("linkStatus") val linkStatus: String?,
    @SerializedName("podDone") val podDone: Boolean?,
    @SerializedName("returnsDone") val returnsDone: Boolean?
)

data class DeliveryImage(
    val id: String,
    @SerializedName("gedDocId") val gedDocId: String,
    val url: String,
    @SerializedName("documentType") val documentType: String,
    @SerializedName("proofId") val proofId: String,
    @SerializedName("createdAt") val createdAt: String
)

data class DeliveryDocument(
    val id: Int,
    @SerializedName("blNumber") val blNumber: String,
    @SerializedName("pdfUrl") val pdfUrl: String?,
    val signed: Boolean,
    @SerializedName("createdAt") val createdAt: String
)

data class DeliveryProof(
    val id: Int,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("signatureUrl") val signatureUrl: String?,
    @SerializedName("createdAt") val createdAt: String?
)

data class UpdateShipmentStatusRequest(val status: String, val notes: String? = null)

data class CompleteShipmentRequest(
    @SerializedName("deliveryTime") val deliveryTime: String,
    @SerializedName("deliveryNotes") val deliveryNotes: String? = null,
    @SerializedName("recipientName") val recipientName: String? = null,
    @SerializedName("signatureUrl") val signatureUrl: String? = null,
    @SerializedName("photoUrls") val photoUrls: List<String>? = null
)

enum class ShipmentDisplayStatus { NOT_STARTED, IN_PROGRESS, COMPLETED }

fun ShipmentDisplayStatus.getDisplayInfo(): Pair<String, String> {
    return when (this) {
        ShipmentDisplayStatus.NOT_STARTED -> "À faire" to "#2196F3"
        ShipmentDisplayStatus.IN_PROGRESS -> "En cours" to "#FF9800"
        ShipmentDisplayStatus.COMPLETED -> "Terminé" to "#4CAF50"
    }
}
