package com.example.delivery.repository.user

import com.example.delivery.database.DatabaseManager
import com.example.delivery.models.*
import com.example.delivery.models.delivery.*
import com.example.delivery.models.driver.*
import com.example.delivery.models.user.*
import com.example.delivery.models.vehicle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import android.util.Log
import kotlin.Result

class DirectProfileRepository {
    private val TAG = "DirectProfileRepository"

    suspend fun getDriverProfile(driverId: Int): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 getDriverProfile: $driverId")
            
            // 1. Fetch Driver
            val driverQuery = """
                SELECT id, name, "licenseNumber", "licenseExpiry", "employmentType", 
                       "contractHoursWeek", "homeDepotId", "tenantId", status, address,
                       "assignedVehicle", city, country, "createdAt", "dateOfBirth",
                       email, "hireDate", phone, "postalCode", salary, "updatedAt"
                FROM "Driver" 
                WHERE id = $driverId LIMIT 1
            """.trimIndent()
            
            val driverRows = JSONObject(DatabaseManager.executeQuery(driverQuery)).optJSONArray("rows")
            if (driverRows == null || driverRows.length() == 0) {
                return@withContext Result.failure(Exception("Driver not found"))
            }
            val dJson = driverRows.getJSONObject(0)
            
            val profile = DriverProfile(
                id = dJson.getInt("id"),
                name = dJson.getString("name"),
                licenseNumber = getNullableString(dJson, "licenseNumber"),
                licenseExpiry = getNullableString(dJson, "licenseExpiry"),
                employmentType = dJson.optString("employmentType", "FULL_TIME"),
                contractHoursWeek = dJson.optInt("contractHoursWeek", 40),
                homeDepotId = dJson.optInt("homeDepotId", 0),
                tenantId = dJson.optInt("tenantId", 0),
                status = dJson.optString("status", "ACTIF"),
                address = getNullableString(dJson, "address"),
                assignedVehicle = getNullableString(dJson, "assignedVehicle"),
                city = getNullableString(dJson, "city"),
                country = getNullableString(dJson, "country"),
                dateOfBirth = getNullableString(dJson, "dateOfBirth"),
                email = getNullableString(dJson, "email"),
                hireDate = getNullableString(dJson, "hireDate"),
                phone = getNullableString(dJson, "phone"),
                postalCode = getNullableString(dJson, "postalCode"),
                salary = dJson.optDouble("salary", 0.0),
                createdAt = dJson.optString("createdAt", ""),
                updatedAt = dJson.optString("updatedAt", "")
            )

            // 2. Fetch Vehicle Info (if exists)
            var vehicleInfo: VehicleInfo? = null
            val vehicleQuery = """
                SELECT id, name, registration, "capacityWeight", "capacityVolume", type, 
                       EXTRACT(YEAR FROM "createdAt")::int as year, status 
                FROM "Vehicle" 
                WHERE "driverId" = $driverId LIMIT 1
            """.trimIndent()
            try {
                val vehicleRows = JSONObject(DatabaseManager.executeQuery(vehicleQuery)).optJSONArray("rows")
                if (vehicleRows != null && vehicleRows.length() > 0) {
                    val vJson = vehicleRows.getJSONObject(0)
                    vehicleInfo = VehicleInfo(
                        id = vJson.getInt("id"),
                        name = vJson.getString("name"),
                        registration = vJson.getString("registration"),
                        capacityWeight = vJson.optDouble("capacityWeight", 0.0),
                        capacityVolume = vJson.optDouble("capacityVolume", 0.0),
                        type = vJson.optString("type", "CAMION"),
                        year = vJson.optInt("year", 2024),
                        status = vJson.optString("status", "ACTIVE")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching vehicle details: ${e.message}")
            }

            // 3. Fetch Depot Info (using Location table)
            var depotInfo: DepotInfo? = null
            val homeDepotId = profile.homeDepotId
            if (homeDepotId > 0) {
                val depotQuery = """
                    SELECT id, name, address, city, "postalCode", phone, email 
                    FROM "Location" 
                    WHERE id = $homeDepotId LIMIT 1
                """.trimIndent()
                try {
                    val depotRows = JSONObject(DatabaseManager.executeQuery(depotQuery)).optJSONArray("rows")
                    if (depotRows != null && depotRows.length() > 0) {
                        val depJson = depotRows.getJSONObject(0)
                        depotInfo = DepotInfo(
                            id = depJson.getInt("id"),
                            name = depJson.getString("name"),
                            address = getNullableString(depJson, "address"),
                            city = getNullableString(depJson, "city"),
                            postalCode = getNullableString(depJson, "postalCode"),
                            phone = getNullableString(depJson, "phone"),
                            email = getNullableString(depJson, "email")
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching depot details: ${e.message}")
                }
            }

            Result.success(ProfileResponse(profile, vehicleInfo, depotInfo))
        } catch (e: Exception) {
            Log.e(TAG, "Error in getDriverProfile", e)
            Result.failure(e)
        }
    }

    suspend fun getDriverStats(driverId: Int): Result<DriverStatsSummary> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 getDriverStats: $driverId")

            // Query total trips and completed trips
            val tripsQuery = """
                SELECT 
                    COUNT(*)::int as total_trips,
                    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END)::int as completed_trips
                FROM "Trip"
                WHERE "driverId" = $driverId
            """.trimIndent()
            
            val tripsRows = JSONObject(DatabaseManager.executeQuery(tripsQuery)).optJSONArray("rows")
            val totalTrips = tripsRows?.optJSONObject(0)?.optInt("total_trips", 0) ?: 0
            val completedTrips = tripsRows?.optJSONObject(0)?.optInt("completed_trips", 0) ?: 0

            // Query shipments metrics
            val shipmentsQuery = """
                SELECT 
                    COUNT(*)::int as total_shipments,
                    COUNT(CASE WHEN status = 'DELIVERED' THEN 1 END)::int as delivered_shipments,
                    COALESCE(SUM(quantity), 0)::float as total_qty,
                    COALESCE(SUM(weight), 0)::float as total_weight,
                    COALESCE(AVG(weight), 0)::float as avg_weight
                FROM "Shipment"
                WHERE "driverId" = $driverId
            """.trimIndent()
            
            val shipmentsRows = JSONObject(DatabaseManager.executeQuery(shipmentsQuery)).optJSONArray("rows")
            val totalShipments = shipmentsRows?.optJSONObject(0)?.optInt("total_shipments", 0) ?: 0
            val deliveredShipments = shipmentsRows?.optJSONObject(0)?.optInt("delivered_shipments", 0) ?: 0
            val totalQuantity = shipmentsRows?.optJSONObject(0)?.optDouble("total_qty", 0.0) ?: 0.0
            val totalWeight = shipmentsRows?.optJSONObject(0)?.optDouble("total_weight", 0.0) ?: 0.0
            val averageWeight = shipmentsRows?.optJSONObject(0)?.optDouble("avg_weight", 0.0) ?: 0.0

            val successRate = if (totalTrips > 0) {
                ((completedTrips.toFloat() / totalTrips.toFloat()) * 100).toInt()
            } else {
                100
            }

            // Get first and last trip dates
            val datesQuery = """
                SELECT 
                    MIN("tripDate") as first_date,
                    MAX("tripDate") as last_date
                FROM "Trip"
                WHERE "driverId" = $driverId
            """.trimIndent()
            val datesRows = JSONObject(DatabaseManager.executeQuery(datesQuery)).optJSONArray("rows")
            val firstTripDate = datesRows?.optJSONObject(0)?.let { getNullableString(it, "first_date") }
            val lastTripDate = datesRows?.optJSONObject(0)?.let { getNullableString(it, "last_date") }

            val statsSummary = DriverStatsSummary(
                driverId = driverId,
                totalTrips = totalTrips,
                completedTrips = completedTrips,
                totalShipments = totalShipments,
                deliveredShipments = deliveredShipments,
                totalQuantity = totalQuantity,
                totalWeight = totalWeight,
                averageWeight = averageWeight,
                successRate = successRate,
                lastTripDate = lastTripDate,
                firstTripDate = firstTripDate
            )

            Result.success(statsSummary)
        } catch (e: Exception) {
            Log.e(TAG, "Error in getDriverStats", e)
            Result.failure(e)
        }
    }

    private fun getNullableString(json: JSONObject, key: String): String? {
        if (json.isNull(key)) return null
        val value = json.optString(key, null as String?)
        return if (value == "null") null else value
    }
}
