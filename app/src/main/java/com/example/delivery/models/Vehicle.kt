package com.example.delivery.models

import com.google.gson.annotations.SerializedName

data class Vehicle(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("registration")
    val registration: String?,
    
    @SerializedName("capacityWeight")
    val capacityWeight: Int?,
    
    @SerializedName("capacityVolume")
    val capacityVolume: Int?,
    
    @SerializedName("tenantId")
    val tenantId: String?,
    
    @SerializedName("dernierControle")
    val dernierControle: String?,
    
    @SerializedName("driverId")
    val driverId: String?,
    
    @SerializedName("prochainControle")
    val prochainControle: String?,
    
    @SerializedName("status")
    val status: String?,
    
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("createdAt")
    val createdAt: String?,
    
    @SerializedName("updatedAt")
    val updatedAt: String?
)
