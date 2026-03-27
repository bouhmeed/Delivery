package com.example.delivery.repository

import com.example.delivery.models.Driver
import com.example.delivery.network.ApiClient
import com.example.delivery.network.DriverApiService
import kotlin.Result

class DriverRepository {
    
    private val apiService = ApiClient.instance.create(DriverApiService::class.java)
    
    suspend fun getDriverById(driverId: String): Result<Driver> {
        return try {
            val response = apiService.getDriverById(driverId)
            if (response.isSuccessful) {
                val driver = response.body()
                if (driver != null) {
                    Result.success(driver)
                } else {
                    Result.failure(Exception("Driver not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch driver: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
