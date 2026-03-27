package com.example.delivery.repository

import com.example.delivery.models.*
import com.example.delivery.network.ApiClient
import com.example.delivery.network.ShipmentDetailApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ShipmentDetailRepository {
    
    private val apiService = ApiClient.getRetrofit().create(ShipmentDetailApiService::class.java)
    
    /**
     * Récupérer les détails complets d'une livraison depuis les tables réelles
     */
    fun getShipmentDetails(shipmentId: Int): Flow<Result<ShipmentDetailFull>> = flow {
        emit(Result.loading())
        
        try {
            val response = apiService.getShipmentDetails(shipmentId)
            
            if (!response.isSuccessful) {
                emit(Result.error("Erreur: ${response.code()} - ${response.message()}"))
                return@flow
            }
            
            val body = response.body()
            if (body == null || !body.success) {
                emit(Result.error(body?.error ?: "Réponse invalide"))
                return@flow
            }
            
            emit(Result.success(body.data.shipment))
            
        } catch (e: Exception) {
            emit(Result.error("Erreur réseau: ${e.message}"))
        }
    }
    
    /**
     * Mettre à jour le statut d'une livraison
     */
    suspend fun updateShipmentStatus(shipmentId: Int, status: String): Result<String> {
        return try {
            val response = apiService.updateShipmentStatus(
                shipmentId, 
                UpdateShipmentStatusRequest(status)
            )
            
            if (response.isSuccessful) {
                Result.success("Statut mis à jour avec succès")
            } else {
                Result.error("Erreur: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.error("Erreur réseau: ${e.message}")
        }
    }
    
    /**
     * Marquer une livraison comme complétée (POD done)
     */
    suspend fun completeShipment(shipmentId: Int): Result<String> {
        return try {
            val response = apiService.completeShipment(shipmentId)
            
            if (response.isSuccessful) {
                Result.success("Livraison complétée avec succès")
            } else {
                Result.error("Erreur: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.error("Erreur réseau: ${e.message}")
        }
    }
    
    /**
     * Obtenir le statut d'affichage correct basé sur podDone et status
     */
    fun getDisplayStatus(shipment: ShipmentDetailFull): ShipmentDisplayStatus {
        return when {
            shipment.trip?.podDone == true -> ShipmentDisplayStatus.COMPLETED
            shipment.trip?.linkStatus == "EN_COURS" -> ShipmentDisplayStatus.IN_PROGRESS
            shipment.trip?.linkStatus == "NON_DEMARRE" -> ShipmentDisplayStatus.NOT_STARTED
            shipment.status == "DELIVERED" -> ShipmentDisplayStatus.COMPLETED
            shipment.status == "EXPEDITION" -> ShipmentDisplayStatus.IN_PROGRESS
            shipment.status == "TO_PLAN" -> ShipmentDisplayStatus.NOT_STARTED
            else -> ShipmentDisplayStatus.NOT_STARTED
        }
    }
    
    /**
     * Vérifier si la livraison appartient à la tournée actuelle du chauffeur
     */
    fun belongsToCurrentTrip(shipment: ShipmentDetailFull, driverId: Int): Boolean {
        return shipment.trip != null && shipment.driver?.id == driverId
    }
    
    /**
     * Obtenir la séquence dans la tournée
     */
    fun getTripSequence(shipment: ShipmentDetailFull): Int? {
        return shipment.trip?.sequence
    }
}
