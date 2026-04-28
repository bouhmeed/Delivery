package com.example.delivery.models

import com.google.gson.annotations.SerializedName

data class Trip(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("tripDate")
    val tripDate: String,
    
    @SerializedName("depotId")
    val depotId: String?,
    
    @SerializedName("driverId")
    val driverId: String?,
    
    @SerializedName("vehicleId")
    val vehicleId: String?,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("tenantId")
    val tenantId: String?,
    
    @SerializedName("createdAt")
    val createdAt: String?,
    
    @SerializedName("tripId")
    val tripId: String?,
    
    @SerializedName("depotName")
    val depotName: String? = null,
    
    @SerializedName("depotAddress")
    val depotAddress: String? = null,
    
    @SerializedName("depotCity")
    val depotCity: String? = null,
    
    @SerializedName("depotPostalCode")
    val depotPostalCode: String? = null
)
