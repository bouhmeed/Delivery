package com.example.delivery.network.api.user

import com.example.delivery.models.user.DriverProfile
import com.example.delivery.models.user.DriverStatsSummary
import com.example.delivery.models.user.VehicleInfo
import com.example.delivery.models.user.DepotInfo
import com.example.delivery.models.user.ProfileResponse
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
