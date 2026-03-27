package com.example.delivery.repository

import com.example.delivery.models.ShipmentSearchResponse
import com.example.delivery.network.ApiClient
import com.example.delivery.network.ShipmentApiService
import kotlin.Result

class ShipmentRepository {
    
    private val apiService = ApiClient.getRetrofit().create(ShipmentApiService::class.java)
    
    /**
     * Rechercher une livraison par code-barres
     */
    suspend fun searchShipment(barcode: String, driverId: Int): Result<ShipmentSearchResponse> {
        return try {
            val response = apiService.searchShipment(barcode, driverId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Rechercher par numéro de suivi
     */
    suspend fun searchByTrackingNumber(trackingNumber: String, driverId: Int): Result<ShipmentSearchResponse> {
        return try {
            val response = apiService.searchByTrackingNumber(trackingNumber, driverId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Marquer une livraison comme complétée
     */
    suspend fun markAsDelivered(shipmentId: Int, driverId: Int): Result<ShipmentSearchResponse> {
        return try {
            val response = apiService.markAsDelivered(shipmentId, driverId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
