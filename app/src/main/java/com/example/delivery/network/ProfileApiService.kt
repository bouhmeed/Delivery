package com.example.delivery.network

import com.example.delivery.models.DriverProfile
import com.example.delivery.models.DriverStatsSummary
import com.example.delivery.models.VehicleInfo
import com.example.delivery.models.DepotInfo
import com.example.delivery.models.ProfileResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProfileApiService {
    @GET("api/profile/{driverId}")
    suspend fun getDriverProfile(@Path("driverId") driverId: Int): Response<ProfileResponse>
    
    @GET("api/profile/{driverId}/stats")
    suspend fun getDriverStats(@Path("driverId") driverId: Int): Response<DriverStatsSummary>
    
    @GET("api/vehicles/{vehicleId}")
    suspend fun getVehicleInfo(@Path("vehicleId") vehicleId: Int): Response<VehicleInfo>
    
    @GET("api/locations/{depotId}")
    suspend fun getDepotInfo(@Path("depotId") depotId: Int): Response<DepotInfo>
}
