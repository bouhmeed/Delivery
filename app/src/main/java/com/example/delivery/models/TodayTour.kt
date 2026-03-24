package com.example.delivery.models

import com.google.gson.annotations.SerializedName

data class TodayTourResponse(
    val success: Boolean,
    val data: TodayTourData,
    val error: String? = null
)

data class TodayTourData(
    @SerializedName("hasTour")
    val hasTour: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("tour")
    val tour: TourInfo? = null,
    @SerializedName("statistics")
    val statistics: TourStatistics? = null,
    @SerializedName("shipments")
    val shipments: List<Shipment> = emptyList()
)

data class TourInfo(
    val id: Int,
    @SerializedName("tripId")
    val tripId: String,
    val status: String,
    val date: String
)

data class TourStatistics(
    @SerializedName("totalShipments")
    val totalShipments: Int,
    @SerializedName("completedShipments")
    val completedShipments: Int,
    @SerializedName("remainingShipments")
    val remainingShipments: Int,
    @SerializedName("completionPercentage")
    val completionPercentage: Int,
    @SerializedName("progressBar")
    val progressBar: String
)

data class Shipment(
    val id: Int,
    @SerializedName("shipmentNo")
    val shipmentNo: String,
    val status: String,
    val description: String,
    val quantity: Int,
    val sequence: Int
)
