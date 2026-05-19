package com.example.delivery.repository

import com.example.delivery.models.UserResponse
import com.example.delivery.database.DatabaseManager
import org.json.JSONObject
import android.util.Log
import kotlin.Result

class DirectUserRepository {
    
    private val TAG = "DirectUserRepository"
    
    /**
     * Get user by email using direct Neon connection
     */
    suspend fun getUserByEmail(email: String): Result<UserResponse> {
        return try {
            Log.d(TAG, "🔍 Getting user by email: $email")
            
            val jsonResponse = DatabaseManager.getUserByEmail(email)
            Log.d(TAG, "📦 JSON Response: $jsonResponse")
            
            val jsonObject = JSONObject(jsonResponse)
            val rows = jsonObject.optJSONArray("rows")
            
            if (rows != null && rows.length() > 0) {
                val userJson = rows.getJSONObject(0)
                
                val user = UserResponse(
                    id = userJson.getInt("id").toString(),
                    tenantId = if (userJson.has("tenantId") && !userJson.isNull("tenantId")) {
                        userJson.getInt("tenantId").toString()
                    } else {
                        null
                    },
                    email = userJson.getString("email"),
                    firstName = userJson.optString("firstName", ""),
                    lastName = userJson.optString("lastName", ""),
                    driverId = if (userJson.has("driverId") && !userJson.isNull("driverId")) {
                        userJson.getInt("driverId").toString()
                    } else {
                        null
                    },
                    role = userJson.optString("role", ""),
                    isActive = userJson.optBoolean("isActive", true),
                    createdAt = userJson.optString("createdAt", ""),
                    updatedAt = userJson.optString("updatedAt", "")
                )
                
                Log.d(TAG, "✅ User found: ${user.firstName} ${user.lastName}")
                Log.d(TAG, "👤 User Details - ID: ${user.id}, Email: ${user.email}, Role: ${user.role}, DriverId: ${user.driverId}, TenantId: ${user.tenantId}")
                Result.success(user)
            } else {
                Log.w(TAG, "⚠️ No user found with email: $email")
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting user by email", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user profile by email using direct Neon connection
     */
    suspend fun getUserProfile(email: String): Result<UserResponse> {
        return getUserByEmail(email) // Same method for now
    }
}
