package com.example.delivery.network.api.driver

import com.example.delivery.models.driver.Driver
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DriverApiService {
    @GET("api/drivers/{id}")
    suspend fun getDriverById(@Path("id") id: String): Response<Driver>
}
