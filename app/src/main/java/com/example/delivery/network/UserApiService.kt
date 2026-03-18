package com.example.delivery.network

import com.example.delivery.models.UserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {
    
    @GET("api/user/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<UserResponse>
    
    @GET("api/user/profile")
    suspend fun getUserProfile(@Query("email") email: String): Response<UserResponse>
}
