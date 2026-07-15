package com.example.delivery.repository.driver

import com.example.delivery.models.driver.Driver
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
            
            val query = """
                SELECT id, "firstName", "lastName", "licenseNumber", "licenseExpiry", "licenseIssueDate",
                       "employmentType", "contractHoursWeek", "tenantId", "addressId", "dateOfBirth", "hireDate",
                       "assignedVehicleId", status, phone, email, "createdAt", "updatedAt"
                FROM "Driver" 
                WHERE id = $driverId
            """.trimIndent()
            
            val jsonResponse = DatabaseManager.executeQuery(query)
            Log.d(TAG, "📦 JSON Response: $jsonResponse")
            
            val jsonObject = JSONObject(jsonResponse)
            val rows = jsonObject.optJSONArray("rows")
            
            if (rows != null && rows.length() > 0) {
                val driverJson = rows.getJSONObject(0)
                
                val firstName = driverJson.optString("firstName", "")
                val lastName = driverJson.optString("lastName", "")
                val driverName = if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                    "$firstName $lastName"
                } else {
                    firstName.ifEmpty { lastName }.ifEmpty { "" }
                }
                
                val driver = Driver(
                    id = driverJson.getInt("id").toString(),
                    name = driverName,
                    licenseNumber = driverJson.optString("licenseNumber", null as String?),
                    licenseExpiry = driverJson.optString("licenseExpiry", null as String?),
                    employmentType = driverJson.optString("employmentType", "FULL_TIME"),
                    contractHoursWeek = driverJson.optInt("contractHoursWeek", 40),
                    homeDepotId = null, // homeDepotId removed from new schema
                    tenantId = if (driverJson.has("tenantId") && !driverJson.isNull("tenantId")) {
                        driverJson.getInt("tenantId").toString()
                    } else {
                        null
                    },
                    status = driverJson.optString("status", "ACTIF"),
                    address = driverJson.optString("addressId", null as String?), // Changed to addressId
                    assignedVehicle = driverJson.optString("assignedVehicleId", null as String?), // Changed to assignedVehicleId
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
