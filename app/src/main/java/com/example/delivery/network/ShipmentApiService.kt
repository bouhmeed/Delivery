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
     * Mettre à jour le statut d'une livraison dans TripShipmentLink (legacy)
     */
    @PUT("api/shipment/trip-shipment/{tripShipmentLinkId}/status")
    suspend fun updateTripShipmentStatus(
        @Path("tripShipmentLinkId") tripShipmentLinkId: Int,
        @Body request: StatusUpdateRequest
    ): Response<StatusUpdateResponse>
    
    /**
     * Mettre à jour le statut d'une livraison dans TripShipmentLink (v2)
     * Also updates Shipment status and auto-completes Trip if all deliveries are TERMINE
     */
    @PUT("api/delivery-tracking/v2/trip-shipment/{tripShipmentLinkId}/status")
    suspend fun updateTripShipmentStatusV2(
        @Path("tripShipmentLinkId") tripShipmentLinkId: Int,
        @Body request: StatusUpdateRequestV2
    ): Response<StatusUpdateResponseV2>
    
    /**
     * Obtenir les dates des expéditions pour un chauffeur (pour le calendrier)
     */
    @GET("api/shipment/dates/{driverId}")
    suspend fun getShipmentDates(
        @Path("driverId") driverId: Int
    ): Response<ShipmentDatesResponse>
}

/**
 * Request body pour la mise à jour de statut v2
 */
data class StatusUpdateRequestV2(
    val status: String, // "TO_PLAN" | "EXPEDITION" | "DELIVERED"
    val updateShipment: Boolean = true,
    val driverId: Int? = null
)

/**
 * Response pour la mise à jour de statut v2
 */
data class StatusUpdateResponseV2(
    val success: Boolean,
    val message: String,
    val data: StatusUpdateDataV2?
)

data class StatusUpdateDataV2(
    val tripShipmentLink: TripShipmentLinkUpdate,
    val shipment: TripShipmentLinkUpdate?,
    val tripAutoCompleted: Boolean,
    val tripId: Int,
    val shipmentId: Int
)

/**
 * Request body pour la mise à jour de statut (legacy)
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

/**
 * Response pour les dates des expéditions
 */
data class ShipmentDatesResponse(
    val success: Boolean,
    val data: List<String>, // Liste de dates au format "yyyy-MM-dd"
    val message: String? = null
)
