package com.example.delivery.models

// Trip is already defined in Trip.kt, so we just use it here

data class TripShipmentLink(
    val id: Int,
    val tripId: Int,
    val shipmentId: Int,
    val status: String,
    val podDone: Boolean,
    val sequence: Int
)

data class TripProgress(
    val totalDeliveries: Int,
    val completedDeliveries: Int,
    val percentage: Float,
    val isCompleted: Boolean
) {
    val progressText: String
        get() = "$completedDeliveries / $totalDeliveries livraisons complétées"
    
    val percentageText: String
        get() = "${percentage.toInt()}%"
}

data class TripWithProgress(
    val trip: Trip,
    val progress: TripProgress
)
