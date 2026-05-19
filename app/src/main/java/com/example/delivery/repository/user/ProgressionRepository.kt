package com.example.delivery.repository.user

import com.example.delivery.repository.Result

import com.example.delivery.models.driver.Trip
import com.example.delivery.models.TripProgress
import com.example.delivery.models.TripShipmentLink
import com.example.delivery.models.TripWithProgress
import com.example.delivery.network.config.ApiClient
import com.example.delivery.network.api.ProgressionApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

class ProgressionRepository {
    
    private val apiService = ApiClient.getRetrofit().create(ProgressionApiService::class.java)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    fun getTodayTripProgress(driverId: Int): Flow<Result<TripWithProgress?>> = flow {
        emit(Result.loading())
        
        try {
            // Get today's trip
            val todayTripResponse = apiService.getTodayTrip(driverId)
            
            if (!todayTripResponse.isSuccessful) {
                emit(Result.error("Erreur: ${todayTripResponse.code()}"))
                return@flow
            }
            
            val trip = todayTripResponse.body()
            if (trip == null) {
                emit(Result.success(null))
                return@flow
            }
            
            // Get shipments for this trip
            val shipmentsResponse = apiService.getTripShipments(trip.id.toString())
            
            if (!shipmentsResponse.isSuccessful) {
                emit(Result.error("Erreur chargement livraisons: ${shipmentsResponse.code()}"))
                return@flow
            }
            
            val shipments = shipmentsResponse.body() ?: emptyList()
            val progress = calculateProgress(shipments, trip.status)
            
            emit(Result.success(TripWithProgress(trip, progress)))
            
        } catch (e: Exception) {
            emit(Result.error("Erreur réseau: ${e.message}"))
        }
    }
    
    private fun calculateProgress(shipments: List<TripShipmentLink>, tripStatus: String): TripProgress {
        val total = shipments.size
        val completed = shipments.count { it.podDone }
        val percentage = if (total > 0) (completed.toFloat() / total) * 100 else 0f
        val isCompleted = tripStatus == "COMPLETED" || (total > 0 && completed == total)
        
        return TripProgress(total, completed, percentage, isCompleted)
    }
    
    fun refreshTripProgress(driverId: Int): Flow<Result<TripWithProgress?>> {
        return getTodayTripProgress(driverId)
    }
}
