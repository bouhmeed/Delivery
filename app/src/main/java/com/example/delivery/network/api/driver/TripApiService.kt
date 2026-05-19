package com.example.delivery.network.api.driver

import com.example.delivery.models.driver.Trip
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TripApiService {
    @GET("api/trips")
    suspend fun getAllTrips(): Response<List<Trip>>
    
    @GET("api/trips/{id}")
    suspend fun getTripById(@Path("id") id: String): Response<Trip>
    
    @GET("api/trips/driver/{driverId}")
    suspend fun getTripsByDriver(@Path("driverId") driverId: String): Response<List<Trip>>
    
    @GET("api/trips/status/{status}")
    suspend fun getTripsByStatus(@Path("status") status: String): Response<List<Trip>>
}
