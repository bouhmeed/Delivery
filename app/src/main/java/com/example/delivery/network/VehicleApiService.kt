package com.example.delivery.network

import com.example.delivery.models.Vehicle
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VehicleApiService {
    @GET("api/vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: String): Response<Vehicle>
    
    @GET("api/vehicles")
    suspend fun getVehicleByDriverId(@Query("driverId") driverId: String): Response<List<Vehicle>>
}
