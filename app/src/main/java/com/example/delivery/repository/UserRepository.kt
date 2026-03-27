package com.example.delivery.repository

import com.example.delivery.models.UserResponse
import com.example.delivery.network.ApiClient
import com.example.delivery.network.UserApiService
import kotlin.Result

class UserRepository {
    
    private val apiService = ApiClient.instance.create(UserApiService::class.java)
    
    suspend fun getUserByEmail(email: String): Result<UserResponse> {
        return try {
            val response = apiService.getUserByEmail(email)
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserProfile(email: String): Result<UserResponse> {
        return try {
            val response = apiService.getUserProfile(email)
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User profile not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch user profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
