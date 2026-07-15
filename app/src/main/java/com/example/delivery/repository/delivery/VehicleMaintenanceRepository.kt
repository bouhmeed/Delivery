package com.example.delivery.repository.delivery

import android.util.Log
import com.example.delivery.database.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class VehicleMaintenanceInfo(
    val hasMaintenance: Boolean = false,
    val maintenanceType: String = "",
    val maintenanceDate: String = "",
    val nextMaintenanceDate: String = "",
    val status: String = "",
    val mileage: Double? = null,
    val description: String = "",
    val daysUntilNextMaintenance: Int = 0,
    val isUrgent: Boolean = false
)

class VehicleMaintenanceRepository {
    private val TAG = "VehicleMaintenanceRepository"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getVehicleMaintenanceInfo(driverId: Int): Result<VehicleMaintenanceInfo> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Fetching vehicle maintenance for driverId: $driverId")

            // First get the driver's assigned vehicle ID
            val driverQuery = """
                SELECT "assignedVehicleId"
                FROM "Driver"
                WHERE id = $driverId
                LIMIT 1
            """.trimIndent()

            Log.d(TAG, "🔍 Driver query: $driverQuery")
            val driverResult = DatabaseManager.executeQuery(driverQuery)
            Log.d(TAG, "🔍 Driver query result: $driverResult")
            
            val driverJson = JSONObject(driverResult)
            val driverRows = driverJson.optJSONArray("rows")
            Log.d(TAG, "🔍 Driver rows count: ${driverRows?.length() ?: 0}")

            if (driverRows == null || driverRows.length() == 0) {
                Log.d(TAG, "⚠️ Driver not found")
                return@withContext Result.success(VehicleMaintenanceInfo())
            }

            val driverRow = driverRows.getJSONObject(0)
            val vehicleId = driverRow.optInt("assignedVehicleId", 0)
            Log.d(TAG, "🔍 Assigned vehicle ID: $vehicleId")

            if (vehicleId == 0) {
                Log.d(TAG, "⚠️ No vehicle assigned to driver")
                return@withContext Result.success(VehicleMaintenanceInfo())
            }

            // Get the most recent maintenance record
            val maintenanceQuery = """
                SELECT 
                    type,
                    description,
                    date,
                    nextMaintenanceDate,
                    status,
                    mileage
                FROM "VehicleMaintenance"
                WHERE vehicleId = $vehicleId
                ORDER BY date DESC
                LIMIT 1
            """.trimIndent()

            Log.d(TAG, "🔍 Maintenance query: $maintenanceQuery")
            
            val maintenanceResult = try {
                DatabaseManager.executeQuery(maintenanceQuery)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error executing maintenance query", e)
                return@withContext Result.success(VehicleMaintenanceInfo())
            }
            
            Log.d(TAG, "🔍 Maintenance query result: $maintenanceResult")
            
            val maintenanceJson = JSONObject(maintenanceResult)
            val maintenanceRows = maintenanceJson.optJSONArray("rows")
            Log.d(TAG, "🔍 Maintenance rows count: ${maintenanceRows?.length() ?: 0}")

            if (maintenanceRows == null || maintenanceRows.length() == 0) {
                Log.d(TAG, "⚠️ No maintenance records found")
                return@withContext Result.success(VehicleMaintenanceInfo())
            }

            val maintenanceRow = maintenanceRows.getJSONObject(0)
            val type = maintenanceRow.optString("type", "")
            val description = maintenanceRow.optString("description", "")
            val date = maintenanceRow.optString("date", "")
            val nextMaintenanceDate = maintenanceRow.optString("nextMaintenanceDate", "")
            val status = maintenanceRow.optString("status", "")
            val maintenanceMileage = maintenanceRow.optDouble("mileage", 0.0)

            Log.d(TAG, "✅ Maintenance found: type=$type, date=$date, nextDate=$nextMaintenanceDate, status=$status")

            // Calculate days until next maintenance
            val daysUntilNext = if (nextMaintenanceDate.isNotEmpty()) {
                try {
                    val nextDate = dateFormat.parse(nextMaintenanceDate)
                    val today = Calendar.getInstance().time
                    val diff = nextDate.time - today.time
                    (diff / (1000 * 60 * 60 * 24)).toInt()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing date", e)
                    Int.MAX_VALUE
                }
            } else {
                Int.MAX_VALUE
            }

            val isUrgent = daysUntilNext <= 7 // Urgent if within 7 days

            val result = VehicleMaintenanceInfo(
                hasMaintenance = true,
                maintenanceType = type,
                maintenanceDate = date,
                nextMaintenanceDate = nextMaintenanceDate,
                status = status,
                mileage = maintenanceMileage,
                description = description,
                daysUntilNextMaintenance = daysUntilNext,
                isUrgent = isUrgent
            )

            Log.d(TAG, "✅ Returning maintenance info: urgent=$isUrgent, days=$daysUntilNext")
            Result.success(result)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching vehicle maintenance", e)
            Result.failure(e)
        }
    }
}
