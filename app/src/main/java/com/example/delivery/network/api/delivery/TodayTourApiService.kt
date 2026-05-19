package com.example.delivery.network.api.delivery

import com.example.delivery.models.ApiResponse
import com.example.delivery.models.delivery.TodayTourResponse
import com.example.delivery.models.delivery.Shipment
import retrofit2.Response
import retrofit2.http.*

interface TodayTourApiService {
    
    @GET("api/today-tour/driver/{driverId}")
    suspend fun getTodayTour(
        @Path("driverId") driverId: Int
    ): Response<TodayTourResponse>
    
    @PUT("api/today-tour/shipment/{shipmentId}/complete")
    suspend fun completeShipment(
        @Path("shipmentId") shipmentId: Int
    ): Response<ApiResponse<Shipment>>
    
    companion object {
        private const val TAG = "TodayTourApiService"
    }
}
