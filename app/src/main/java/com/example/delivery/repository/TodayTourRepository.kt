package com.example.delivery.repository

import com.example.delivery.models.TodayTourResponse
import com.example.delivery.models.ApiResponse
import com.example.delivery.models.Shipment
import com.example.delivery.network.ApiClient
import com.example.delivery.network.TodayTourApiService
import android.util.Log

class TodayTourRepository {
    
    private val apiService = ApiClient.getRetrofit().create(TodayTourApiService::class.java)
    private val TAG = "TodayTourRepository"
    
    suspend fun getTodayTour(driverId: Int): Result<TodayTourResponse> {
        Log.d(TAG, "Getting today's tour for driver: $driverId")
        Log.d(TAG, "API URL: ${ApiClient.getRetrofit().baseUrl().toUrl()}")
        
        return try {
            val response = apiService.getTodayTour(driverId)
            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response message: ${response.message()}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "Response body: $body")
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "API Error - Code: ${response.code()}, Message: ${response.message()}, Error body: $errorBody")
                Result.failure(Exception("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception", e)
            Result.failure(e)
        }
    }
    
    suspend fun completeShipment(shipmentId: Int): Result<ApiResponse<Shipment>> {
        Log.d(TAG, "Completing shipment: $shipmentId")
        
        return try {
            val response = apiService.completeShipment(shipmentId)
            Log.d(TAG, "Complete shipment response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Complete shipment error - Code: ${response.code()}, Error body: $errorBody")
                Result.failure(Exception("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Complete shipment exception", e)
            Result.failure(e)
        }
    }
}
