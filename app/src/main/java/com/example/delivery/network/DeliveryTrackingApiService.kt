package com.example.delivery.network

import com.example.delivery.models.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.Query

interface DeliveryTrackingApiService {
    
    /**
     * Get trip for a specific date and driver
     * @param driverId The driver's ID
     * @param date The date in YYYY-MM-DD format
     * @return Trip with associated deliveries
     */
    @GET("api/delivery-tracking/date")
    suspend fun getTripForDate(
        @Query("driverId") driverId: Int,
        @Query("date") date: String
    ): Response<TripWithDeliveries>
    
    /**
     * Get today's trip for a specific driver
     * @param driverId The driver's ID
     * @return Today's trip or null if no trip exists
     */
    @GET("api/delivery-tracking/today")
    suspend fun getTodayTrip(@Query("driverId") driverId: Int): Response<TodayTripResponse>
    
    /**
     * Get all deliveries for a specific trip
     * @param tripId The trip's ID
     * @return List of delivery items with full details
     */
    @GET("api/delivery-tracking/trips/{tripId}/deliveries")
    suspend fun getTripDeliveries(@Path("tripId") tripId: Int): Response<List<DeliveryItem>>
    
    /**
     * Get trip with deliveries in one call
     * @param driverId The driver's ID
     * @return Trip with associated deliveries
     */
    @GET("api/delivery-tracking/today/with-deliveries")
    suspend fun getTodayTripWithDeliveries(@Query("driverId") driverId: Int): Response<TripWithDeliveries>
    
    /**
     * Update delivery status
     * @param shipmentId The shipment's ID
     * @param status New status
     * @return Success response
     */
    @PUT("api/delivery-tracking/shipments/{shipmentId}/status")
    suspend fun updateShipmentStatus(
        @Path("shipmentId") shipmentId: Int,
        @Body statusUpdate: StatusUpdateRequest
    ): Response<ApiResponse<Unit>>
    
    /**
     * Mark delivery as completed (POD done)
     * @param shipmentId The shipment's ID
     * @return Success response
     */
    @PUT("api/delivery-tracking/shipments/{shipmentId}/complete")
    suspend fun completeDelivery(@Path("shipmentId") shipmentId: Int): Response<ApiResponse<Unit>>
}
