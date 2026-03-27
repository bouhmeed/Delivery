package com.example.delivery.network

import com.example.delivery.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ShipmentDetailApiService {
    
    // Obtenir les détails complets d'une livraison
    @GET("api/shipments/{shipmentId}/details")
    suspend fun getShipmentDetails(
        @Path("shipmentId") shipmentId: Int
    ): Response<ShipmentDetailResponse>
    
    // Mettre à jour le statut d'une livraison
    @PUT("api/delivery-tracking/shipments/{shipmentId}/status")
    suspend fun updateShipmentStatus(
        @Path("shipmentId") shipmentId: Int,
        @Body request: UpdateShipmentStatusRequest
    ): Response<UpdateStatusResponse>
    
    // Marquer une livraison comme complétée (POD done)
    @PUT("api/delivery-tracking/shipments/{shipmentId}/complete")
    suspend fun completeShipment(
        @Path("shipmentId") shipmentId: Int
    ): Response<UpdateStatusResponse>
    
    // Télécharger une image de preuve de livraison
    @Multipart
    @POST("api/shipments/{shipmentId}/upload-proof")
    suspend fun uploadDeliveryProof(
        @Path("shipmentId") shipmentId: Int,
        @Part image: MultipartBody.Part,
        @Part("documentType") documentType: String,
        @Part("proofId") proofId: String
    ): Response<UpdateStatusResponse>
}

// Réponse simple pour les opérations de mise à jour
data class UpdateStatusResponse(
    val success: Boolean,
    val message: String
)
