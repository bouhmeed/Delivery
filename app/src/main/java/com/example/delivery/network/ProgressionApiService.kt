package com.example.delivery.network

import com.example.delivery.models.Trip
import com.example.delivery.models.TripShipmentLink
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProgressionApiService {
    
    @GET("api/trips/today")
    suspend fun getTodayTrip(@Query("driverId") driverId: Int): Response<Trip?>
    
    @GET("api/trips/{tripId}/shipments")
    suspend fun getTripShipments(@Path("tripId") tripId: String): Response<List<TripShipmentLink>>
}
