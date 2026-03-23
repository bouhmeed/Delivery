package com.example.delivery.network

import com.example.delivery.models.TripHistory
import com.example.delivery.models.DriverStats
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface HistoryApiService {
    @GET("api/history/driver/{driverId}")
    suspend fun getDriverHistory(@Path("driverId") driverId: String): Response<TripHistory>
    
    @GET("api/history/stats/driver/{driverId}")
    suspend fun getDriverStats(@Path("driverId") driverId: String): Response<DriverStats>
}
