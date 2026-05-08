package com.example.delivery.repository

import com.example.delivery.models.Vehicle
import com.example.delivery.models.VehicleMaintenance
import com.example.delivery.models.MaintenanceAlert
import com.example.delivery.network.ApiClient
import com.example.delivery.network.VehicleApiService
import kotlin.Result
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
    
    suspend fun getVehicleMaintenance(vehicleId: String): Result<List<VehicleMaintenance>> {
        return try {
            val response = apiService.getVehicleMaintenance(vehicleId)
            if (response.isSuccessful) {
                val maintenance = response.body()
                if (maintenance != null) {
                    Result.success(maintenance)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("Failed to fetch vehicle maintenance: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun calculateMaintenanceAlert(maintenance: List<VehicleMaintenance>): MaintenanceAlert? {
        if (maintenance.isEmpty()) return null
        
        val latest = maintenance.first()
        val nextDate = latest.nextMaintenance
        
        if (nextDate == null) return null
        
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val nextMaintenanceDate = LocalDate.parse(nextDate, formatter)
        val today = LocalDate.now()
        val daysRemaining = ChronoUnit.DAYS.between(today, nextMaintenanceDate).toInt()
        
        val warningLevel = when {
            daysRemaining <= 7 -> com.example.delivery.models.WarningLevel.URGENT
            daysRemaining <= 30 -> com.example.delivery.models.WarningLevel.WARNING
            else -> com.example.delivery.models.WarningLevel.NORMAL
        }
        
        return MaintenanceAlert(
            type = latest.type,
            nextDate = nextDate,
            daysRemaining = daysRemaining,
            warningLevel = warningLevel,
            vehicleName = latest.vehicleName,
            registration = latest.registration
        )
    }
}
