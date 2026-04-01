package com.example.delivery.network

import com.example.delivery.models.ShipmentSearchResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ShipmentApiService {
    
    /**
     * Rechercher une livraison par code-barres ou numéro de suivi
     */
    @GET("api/shipment")
    suspend fun searchShipment(
        @Query("barcode") barcode: String,
        @Query("driverId") driverId: Int
    ): Response<ShipmentSearchResponse>
    
    /**
     * Rechercher une livraison par numéro de suivi uniquement
     */
    @GET("api/shipment/tracking")
    suspend fun searchByTrackingNumber(
        @Query("trackingNumber") trackingNumber: String,
        @Query("driverId") driverId: Int
    ): Response<ShipmentSearchResponse>
    
    /**
     * Obtenir les expéditions d'un trip spécifique
     */
    @GET("api/shipment/trip/{tripId}")
    suspend fun getShipmentsByTrip(
        @retrofit2.http.Path("tripId") tripId: Int
    ): Response<List<com.example.delivery.models.ShipmentSearchDetail>>
    
    /**
     * Marquer une livraison comme complétée
     */
    @GET("api/shipment/complete")
    suspend fun markAsDelivered(
        @Query("shipmentId") shipmentId: Int,
        @Query("driverId") driverId: Int
    ): Response<ShipmentSearchResponse>
    
    /**
     * Mettre à jour le statut d'une livraison dans TripShipmentLink
     */
    @PUT("api/shipment/trip-shipment/{id}/status")
    suspend fun updateTripShipmentStatus(
        @Path("id") id: Int,
        @Body request: StatusUpdateRequest
    ): Response<StatusUpdateResponse>
}

/**
 * Request body pour la mise à jour de statut
 */
data class StatusUpdateRequest(
    val status: String // "NON_DEMARRE" | "EN_COURS" | "TERMINE"
)

/**
 * Response pour la mise à jour de statut
 */
data class StatusUpdateResponse(
    val success: Boolean,
    val message: String,
    val data: StatusUpdateData?
)

data class StatusUpdateData(
    val tripShipmentLink: TripShipmentLinkUpdate,
    val tripId: Int,
    val shipmentId: Int
)

data class TripShipmentLinkUpdate(
    val id: Int,
    val status: String,
    val podDone: Boolean,
    val updatedAt: String
)
