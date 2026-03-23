package com.example.delivery.repository

import com.example.delivery.models.Vehicle
import com.example.delivery.network.ApiClient
import com.example.delivery.network.VehicleApiService

class VehicleRepository {
    
    private val apiService = ApiClient.instance.create(VehicleApiService::class.java)
    
    suspend fun getVehicleById(vehicleId: String): Result<Vehicle> {
        return try {
            val response = apiService.getVehicleById(vehicleId)
            if (response.isSuccessful) {
                val vehicle = response.body()
                if (vehicle != null) {
                    Result.success(vehicle)
                } else {
                    Result.failure(Exception("Vehicle not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch vehicle: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getVehicleByDriverId(driverId: String): Result<Vehicle?> {
        return try {
            val response = apiService.getVehicleByDriverId(driverId)
            if (response.isSuccessful) {
                val vehicles = response.body()
                if (vehicles != null && vehicles.isNotEmpty()) {
                    Result.success(vehicles.first())
                } else {
                    Result.success(null) // Pas de véhicule assigné
                }
            } else {
                Result.failure(Exception("Failed to fetch vehicle: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
