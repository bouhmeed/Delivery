package com.example.delivery.models

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("tenantId")
    val tenantId: String?,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("role")
    val role: String?,
    
    @SerializedName("firstName")
    val firstName: String?,
    
    @SerializedName("lastName")
    val lastName: String?,
    
    @SerializedName("driverId")
    val driverId: String?,
    
    @SerializedName("isActive")
    val isActive: Boolean?,
    
    @SerializedName("createdAt")
    val createdAt: String?,
    
    @SerializedName("updatedAt")
    val updatedAt: String?
)
