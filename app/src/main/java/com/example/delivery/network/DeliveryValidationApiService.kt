package com.example.delivery.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Data models for delivery validation
data class DeliveryProofRequest(
    val shipmentId: Int,
    val imageUrl: String,
    val signatureUrl: String? = null
)

data class DeliveryValidationRequest(
    val shipmentId: Int,
    val signerName: String,
    val signatureData: String,
    val imageData: String? = null,
    val notes: String? = null
)

data class DeliveryProofResponse(
    val success: Boolean,
    val message: String,
    val data: DeliveryProof? = null,
    val proofId: Int? = null,
    val error: String? = null
)

data class DeliveryProof(
    val id: Int,
    val shipmentId: Int,
    val imageUrl: String,
    val signatureUrl: String?,
    val createdAt: String,
    val shipmentNo: String?,
    val shipmentStatus: String?
)

interface DeliveryValidationApiService {
    
    @POST("api/delivery-validation/proof")
    suspend fun saveDeliveryProof(@Body request: DeliveryProofRequest): Response<DeliveryProofResponse>
    
    @GET("api/delivery-validation/proof/{shipmentId}")
    suspend fun getDeliveryProof(@Path("shipmentId") shipmentId: Int): Response<DeliveryProofResponse>
    
    @POST("api/delivery-validation/validate")
    suspend fun validateDelivery(@Body request: DeliveryValidationRequest): Response<DeliveryProofResponse>
}
