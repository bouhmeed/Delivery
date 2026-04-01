package com.example.delivery.repository

import com.example.delivery.models.*
import com.example.delivery.network.ApiClient
import com.example.delivery.network.DeliveryTrackingApiService
import com.example.delivery.network.StatusUpdateRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class DeliveryTrackingRepository {
    
    private val apiService: DeliveryTrackingApiService = ApiClient.getRetrofitInstance()
        .create(DeliveryTrackingApiService::class.java)
    
    /**
     * Get trip for a specific date and driver
     */
    fun getTripForDate(driverId: Int, date: String): Flow<Result<TripWithDeliveries>> = flow {
        emit(Result.loading())
        
        try {
            val response = apiService.getTripForDate(driverId, date)
            
            if (response.isSuccessful && response.body() != null) {
                val tripWithDeliveries = response.body()!!
                emit(Result.success(tripWithDeliveries))
            } else {
                emit(Result.error("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Result.error("Exception: ${e.message}"))
        }
    }
    
    /**
     * Get today's trip with deliveries for a driver
     */
    fun getTodayTripWithDeliveries(driverId: Int): Flow<Result<TripWithDeliveries>> = flow {
        emit(Result.loading())
        
        try {
            val response = apiService.getTodayTripWithDeliveries(driverId)
            
            if (response.isSuccessful && response.body() != null) {
                val tripWithDeliveries = response.body()!!
                emit(Result.success(tripWithDeliveries))
            } else {
                emit(Result.error("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Result.error("Exception: ${e.message}"))
        }
    }
    
    /**
     * Get today's trip only
     */
    fun getTodayTrip(driverId: Int): Flow<Result<Trip?>> = flow {
        emit(Result.loading())
        
        try {
            val response = apiService.getTodayTrip(driverId)
            
            if (response.isSuccessful && response.body() != null) {
                val todayTripResponse = response.body()!!
                emit(Result.success(todayTripResponse.trip))
            } else {
                emit(Result.error("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Result.error("Exception: ${e.message}"))
        }
    }
    
    /**
     * Get deliveries for a specific trip
     */
    fun getTripDeliveries(tripId: Int): Flow<Result<List<DeliveryItem>>> = flow {
        emit(Result.loading())
        
        try {
            val response = apiService.getTripDeliveries(tripId)
            
            if (response.isSuccessful && response.body() != null) {
                val deliveries = response.body()!!
                emit(Result.success(deliveries))
            } else {
                emit(Result.error("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Result.error("Exception: ${e.message}"))
        }
    }
    
    /**
     * Update shipment status
     */
    suspend fun updateShipmentStatus(shipmentId: Int, status: String): Result<Unit> {
        return try {
            val statusUpdate = StatusUpdateRequest(status)
            val response = apiService.updateShipmentStatus(shipmentId, statusUpdate)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.error("Erreur: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Result.error("Exception: ${e.message}")
        }
    }
    
    /**
     * Complete delivery (mark as POD done)
     */
    suspend fun completeDelivery(shipmentId: Int): Result<Unit> {
        return try {
            val response = apiService.completeDelivery(shipmentId)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.error("Erreur: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Result.error("Exception: ${e.message}")
        }
    }
}
