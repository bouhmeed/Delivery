package com.example.delivery.models

import com.google.gson.annotations.SerializedName

data class VehicleMaintenance(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("vehicleId")
    val vehicleId: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("nextMaintenance")
    val nextMaintenance: String?,
    
    @SerializedName("estimatedCost")
    val estimatedCost: Double?,
    
    @SerializedName("notes")
    val notes: String?,
    
    @SerializedName("technician")
    val technician: String?,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("vehicleName")
    val vehicleName: String?,
    
    @SerializedName("registration")
    val registration: String?
)

data class MaintenanceAlert(
    val type: String,
    val nextDate: String?,
    val daysRemaining: Int?,
    val warningLevel: WarningLevel,
    val vehicleName: String?,
    val registration: String?
)

enum class WarningLevel {
    NORMAL,
    WARNING,
    URGENT
}
