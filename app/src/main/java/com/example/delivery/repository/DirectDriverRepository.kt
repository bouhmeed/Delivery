package com.example.delivery.repository

import com.example.delivery.models.Driver
import com.example.delivery.database.DatabaseManager
import org.json.JSONObject
import android.util.Log
import kotlin.Result

class DirectDriverRepository {
    
    private val TAG = "DirectDriverRepository"
    
    /**
     * Get driver by ID using direct Neon connection
     */
    suspend fun getDriverById(driverId: String): Result<Driver> {
        return try {
            Log.d(TAG, "🔍 Getting driver by ID: $driverId")
            
            val jsonResponse = DatabaseManager.getDriverById(driverId.toInt())
            Log.d(TAG, "📦 JSON Response: $jsonResponse")
            
            val jsonObject = JSONObject(jsonResponse)
            val rows = jsonObject.optJSONArray("rows")
            
            if (rows != null && rows.length() > 0) {
                val driverJson = rows.getJSONObject(0)
                
                val driver = Driver(
                    id = driverJson.getInt("id").toString(),
                    name = driverJson.getString("name"),
                    licenseNumber = driverJson.optString("licenseNumber", null),
                    licenseExpiry = driverJson.optString("licenseExpiry", null),
                    employmentType = driverJson.optString("employmentType", "FULL_TIME"),
                    contractHoursWeek = driverJson.optInt("contractHoursWeek", 40),
                    homeDepotId = if (driverJson.has("homeDepotId") && !driverJson.isNull("homeDepotId")) {
                        driverJson.getInt("homeDepotId").toString()
                    } else {
                        null
                    },
                    tenantId = if (driverJson.has("tenantId") && !driverJson.isNull("tenantId")) {
                        driverJson.getInt("tenantId").toString()
                    } else {
                        null
                    },
                    status = driverJson.optString("status", "ACTIF"),
                    address = driverJson.optString("address", null),
                    assignedVehicle = driverJson.optString("assignedVehicle", null),
                    createdAt = driverJson.optString("createdAt", ""),
                    updatedAt = driverJson.optString("updatedAt", "")
                )
                
                Log.d(TAG, "✅ Driver found: ${driver.name}")
                Result.success(driver)
            } else {
                Log.w(TAG, "⚠️ No driver found with ID: $driverId")
                Result.failure(Exception("Driver not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting driver by ID", e)
            Result.failure(e)
        }
    }
}
