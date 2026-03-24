package com.example.delivery.network

import com.example.delivery.models.ShipmentSearchResponse
import retrofit2.Response
import retrofit2.http.GET
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
     * Marquer une livraison comme complétée
     */
    @GET("api/shipment/complete")
    suspend fun markAsDelivered(
        @Query("shipmentId") shipmentId: Int,
        @Query("driverId") driverId: Int
    ): Response<ShipmentSearchResponse>
}
