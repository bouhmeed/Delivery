package com.example.delivery.models

import com.google.gson.annotations.SerializedName

data class Driver(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("licenseNumber")
    val licenseNumber: String?,
    
    @SerializedName("licenseExpiry")
    val licenseExpiry: String?,
    
    @SerializedName("employmentType")
    val employmentType: String?,
    
    @SerializedName("contractHoursWeek")
    val contractHoursWeek: Int?,
    
    @SerializedName("homeDepotId")
    val homeDepotId: String?,
    
    @SerializedName("tenantId")
    val tenantId: String?,
    
    @SerializedName("status")
    val status: String?,
    
    @SerializedName("address")
    val address: String?,
    
    @SerializedName("assignedVehicle")
    val assignedVehicle: String?,
    
    @SerializedName("createdAt")
    val createdAt: String?,
    
    @SerializedName("updatedAt")
    val updatedAt: String?
)
