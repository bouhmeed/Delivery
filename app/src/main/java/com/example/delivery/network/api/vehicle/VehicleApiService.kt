package com.example.delivery.network.api.vehicle

import com.example.delivery.models.vehicle.Vehicle
import com.example.delivery.models.vehicle.VehicleMaintenance
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VehicleApiService {
    @GET("api/vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: String): Response<Vehicle>
    
    @GET("api/vehicles")
    suspend fun getVehicleByDriverId(@Query("driverId") driverId: String): Response<List<Vehicle>>
    
    @GET("api/vehicles/{id}/maintenance")
    suspend fun getVehicleMaintenance(@Path("id") id: String): Response<List<VehicleMaintenance>>
}
