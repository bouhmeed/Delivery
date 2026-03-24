package com.example.delivery.network

import com.example.delivery.models.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Query

interface TripDetailApiService {
    
    // Obtenir les détails complets d'un trajet depuis les tables réelles
    @GET("api/tournee/{tripId}/details")
    suspend fun getTripDetails(
        @Path("tripId") tripId: Int
    ): Response<TripDetailResponse>
    
    // Obtenir la liste des trajets avec filtres
    @GET("api/trips")
    suspend fun getTrips(
        @Query("driverId") driverId: Int,
        @Query("dateFilter") dateFilter: String? = null, // today, upcoming, past, all
        @Query("statusFilter") statusFilter: String? = null, // planning, ready, in_progress, completed, cancelled, all
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<TripListResponse>
    
    // Obtenir les trajets pour une date spécifique
    @GET("api/trips/by-date")
    suspend fun getTripsByDate(
        @Query("driverId") driverId: Int,
        @Query("date") date: String, // Format: YYYY-MM-DD
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<TripListResponse>
    
    // Mettre à jour le statut d'un trajet
    @PUT("api/trips/{tripId}/status")
    suspend fun updateTripStatus(
        @Path("tripId") tripId: Int,
        @Body request: UpdateTripStatusRequest
    ): Response<ApiResponse<TripDetail>>
    
    // Démarrer un trajet
    @POST("api/trips/{tripId}/start")
    suspend fun startTrip(
        @Path("tripId") tripId: Int,
        @Body request: StartTripRequest? = null
    ): Response<ApiResponse<TripDetail>>
    
    // Terminer un trajet
    @POST("api/trips/{tripId}/complete")
    suspend fun completeTrip(
        @Path("tripId") tripId: Int,
        @Body request: CompleteTripRequest? = null
    ): Response<ApiResponse<TripDetail>>
    
    // Mettre à jour les informations d'un arrêt
    @PUT("api/trips/{tripId}/stops/{stopId}")
    suspend fun updateTripStop(
        @Path("tripId") tripId: Int,
        @Path("stopId") stopId: Int,
        @Body request: UpdateTripStopRequest
    ): Response<ApiResponse<TripStopDetail>>
    
    // Marquer un shipment comme livré dans un trajet
    @PUT("api/trips/{tripId}/shipments/{shipmentId}/deliver")
    suspend fun deliverShipment(
        @Path("tripId") tripId: Int,
        @Path("shipmentId") shipmentId: Int,
        @Body request: DeliverShipmentRequest
    ): Response<ApiResponse<ShipmentDetail>>
    
    // Obtenir les statistiques des trajets pour un chauffeur
    @GET("api/trips/stats/driver/{driverId}")
    suspend fun getDriverTripStats(
        @Path("driverId") driverId: Int,
        @Query("period") period: String? = null // week, month, year
    ): Response<ApiResponse<TripStatsResponse>>
}

// Modèles de requête
data class UpdateTripStatusRequest(
    val status: String,
    val notes: String? = null
)

data class StartTripRequest(
    @SerializedName("actualStartTime")
    val actualStartTime: String? = null, // ISO 8601 timestamp
    val notes: String? = null
)

data class CompleteTripRequest(
    @SerializedName("actualEndTime")
    val actualEndTime: String? = null, // ISO 8601 timestamp
    @SerializedName("totalDistance")
    val totalDistance: Double? = null,
    @SerializedName("actualDuration")
    val actualDuration: Int? = null, // minutes
    val notes: String? = null
)

data class UpdateTripStopRequest(
    @SerializedName("actualArrival")
    val actualArrival: String? = null, // ISO 8601 timestamp
    @SerializedName("actualDeparture")
    val actualDeparture: String? = null, // ISO 8601 timestamp
    val notes: String? = null
)

data class DeliverShipmentRequest(
    @SerializedName("deliveryTime")
    val deliveryTime: String, // ISO 8601 timestamp
    @SerializedName("deliveryNotes")
    val deliveryNotes: String? = null,
    @SerializedName("recipientName")
    val recipientName: String? = null,
    @SerializedName("signatureUrl")
    val signatureUrl: String? = null,
    @SerializedName("photoUrls")
    val photoUrls: List<String>? = null
)

// Réponse pour les statistiques
data class TripStatsResponse(
    @SerializedName("totalTrips")
    val totalTrips: Int,
    @SerializedName("completedTrips")
    val completedTrips: Int,
    @SerializedName("totalDistance")
    val totalDistance: Double,
    @SerializedName("totalDuration")
    val totalDuration: Int, // minutes
    @SerializedName("averageTripDuration")
    val averageTripDuration: Double, // minutes
    @SerializedName("totalShipments")
    val totalShipments: Int,
    @SerializedName("deliveredShipments")
    val deliveredShipments: Int,
    @SerializedName("completionRate")
    val completionRate: Double, // percentage
    @SerializedName("periodStats")
    val periodStats: List<PeriodTripStats>
)

data class PeriodTripStats(
    val period: String, // week, month, day
    val periodLabel: String, // "Semaine 1", "Janvier", "Lundi"
    @SerializedName("tripCount")
    val tripCount: Int,
    @SerializedName("shipmentCount")
    val shipmentCount: Int,
    @SerializedName("distance")
    val distance: Double,
    @SerializedName("duration")
    val duration: Int
)
