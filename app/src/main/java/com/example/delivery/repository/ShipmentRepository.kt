package com.example.delivery.repository

import com.example.delivery.models.ShipmentSearchResponse
import com.example.delivery.network.ApiClient
import com.example.delivery.network.ShipmentApiService
import com.example.delivery.network.StatusUpdateRequest
import com.example.delivery.network.StatusUpdateRequestV2
import com.example.delivery.network.StatusUpdateResponse
import com.example.delivery.network.StatusUpdateResponseV2
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
    
    /**
     * Mettre à jour le statut d'une livraison dans TripShipmentLink (legacy - use v2 for new code)
     */
    suspend fun updateTripShipmentStatus(tripShipmentLinkId: Int, status: String): Result<StatusUpdateResponse> {
        return try {
            println("🔄 Repository: Mise à jour statut TripShipmentLink $tripShipmentLinkId -> $status")
            
            val request = StatusUpdateRequest(status)
            val response = apiService.updateTripShipmentStatus(tripShipmentLinkId, request)
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                println("✅ Repository: Statut mis à jour avec succès - ${responseBody.message}")
                Result.success(responseBody)
            } else {
                val errorCode = response.code()
                val errorMessage = response.errorBody()?.string() ?: response.message()
                val errorDetails = "Erreur $errorCode: $errorMessage"
                println("❌ Repository: Échec mise à jour statut - $errorDetails")
                Result.failure(Exception(errorDetails))
            }
        } catch (e: Exception) {
            println("❌ Repository: Exception lors de la mise à jour du statut - ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Mettre à jour le statut d'une livraison dans TripShipmentLink (v2)
     * Also updates Shipment status and auto-completes Trip if all deliveries are TERMINE
     */
    suspend fun updateTripShipmentStatusV2(tripShipmentLinkId: Int, status: String, driverId: Int? = null): Result<StatusUpdateResponseV2> {
        return try {
            println("🔄 Repository: Mise à jour statut v2 TripShipmentLink $tripShipmentLinkId -> $status")
            
            val request = StatusUpdateRequestV2(
                status = status,
                updateShipment = true,
                driverId = driverId
            )
            val response = apiService.updateTripShipmentStatusV2(tripShipmentLinkId, request)
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                if (responseBody.success) {
                    println("✅ Repository: Statut v2 mis à jour avec succès - tripAutoCompleted=${responseBody.data?.tripAutoCompleted}")
                } else {
                    println("⚠️ Repository: Statut v2 mis à jour mais success=false - ${responseBody.message}")
                }
                Result.success(responseBody)
            } else {
                val errorCode = response.code()
                val errorMessage = response.errorBody()?.string() ?: response.message()
                val errorDetails = "Erreur $errorCode: $errorMessage"
                println("❌ Repository: Échec mise à jour statut v2 - $errorDetails")
                Result.failure(Exception(errorDetails))
            }
        } catch (e: Exception) {
            println("❌ Repository: Exception lors de la mise à jour du statut v2 - ${e.message}")
            Result.failure(e)
        }
    }

}
