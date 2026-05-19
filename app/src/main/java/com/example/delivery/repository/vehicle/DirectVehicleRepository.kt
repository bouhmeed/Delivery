package com.example.delivery.repository.vehicle

 
import com.example.delivery.models.vehicle.Vehicle
import com.example.delivery.models.vehicle.VehicleMaintenance
import com.example.delivery.models.vehicle.MaintenanceAlert
import com.example.delivery.database.DatabaseManager
import org.json.JSONObject
import android.util.Log
import kotlin.Result
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
 
class DirectVehicleRepository {
 
    private val TAG = "DirectVehicleRepository"
 
    /**
     * Get vehicle by ID using direct Neon connection
     */
    suspend fun getVehicleById(vehicleId: String): Result<Vehicle> {
        return try {
            Log.d(TAG, "🔍 Getting vehicle by ID: $vehicleId")
 
            val query = """
                SELECT id, name, registration, "capacityWeight", "capacityVolume", 
                       tenantId, "dernierControle", driverId, "prochainControle", 
                       status, type, "createdAt", "updatedAt"
                FROM "Vehicle" 
                WHERE id = $vehicleId
            """.trimIndent()
 
            val jsonResponse = DatabaseManager.executeQuery(query)
            Log.d(TAG, "📦 JSON Response: $jsonResponse")
 
            val jsonObject = JSONObject(jsonResponse)
            val rows = jsonObject.optJSONArray("rows")
 
            if (rows != null && rows.length() > 0) {
                val vehicleJson = rows.getJSONObject(0)
 
                val vehicle = Vehicle(
                    id = vehicleJson.getInt("id").toString(),
                    name = vehicleJson.getString("name"),
                    registration = vehicleJson.getString("registration"),
                    capacityWeight = vehicleJson.optInt("capacityWeight"),
                    capacityVolume = vehicleJson.optInt("capacityVolume"),
                    tenantId = vehicleJson.getInt("tenantId").toString(),
                    dernierControle = vehicleJson.optString("dernierControle", null as String?),
                    driverId = if (vehicleJson.has("driverId") && !vehicleJson.isNull("driverId")) {
                        vehicleJson.getInt("driverId").toString()
                    } else {
                        null
                    },
                    prochainControle = vehicleJson.optString("prochainControle", null as String?),
                    status = vehicleJson.optString("status", "ACTIVE"),
                    type = vehicleJson.optString("type", "CAMION"),
                    createdAt = vehicleJson.optString("createdAt", null as String?),
                    updatedAt = vehicleJson.optString("updatedAt", null as String?)
                )
 
                Log.d(TAG, "✅ Vehicle found: ${vehicle.name}")
                Result.success(vehicle)
            } else {
                Log.w(TAG, "⚠️ No vehicle found with ID: $vehicleId")
                Result.failure(Exception("Vehicle not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting vehicle by ID", e)
            Result.failure(e)
        }
    }
 
    /**
     * Get vehicle by driver ID using direct Neon connection
     */
    suspend fun getVehicleByDriverId(driverId: String): Result<Vehicle?> {
        return try {
            Log.d(TAG, "🔍 Getting vehicle by driver ID: $driverId")
 
            val jsonResponse = DatabaseManager.getVehicleByDriverId(driverId.toInt())
            Log.d(TAG, "📦 JSON Response: $jsonResponse")
 
            val jsonObject = JSONObject(jsonResponse)
            val rows = jsonObject.optJSONArray("rows")
 
            if (rows != null && rows.length() > 0) {
                val vehicleJson = rows.getJSONObject(0)
 
                val vehicle = Vehicle(
                    id = vehicleJson.getInt("id").toString(),
                    name = vehicleJson.getString("name"),
                    registration = vehicleJson.getString("registration"),
                    capacityWeight = vehicleJson.optInt("capacityWeight"),
                    capacityVolume = vehicleJson.optInt("capacityVolume"),
                    tenantId = vehicleJson.getInt("tenantId").toString(),
                    dernierControle = vehicleJson.optString("dernierControle", null as String?),
                    driverId = if (vehicleJson.has("driverId") && !vehicleJson.isNull("driverId")) {
                        vehicleJson.getInt("driverId").toString()
                    } else {
                        null
                    },
                    prochainControle = vehicleJson.optString("prochainControle", null as String?),
                    status = vehicleJson.optString("status", "ACTIVE"),
                    type = vehicleJson.optString("type", "CAMION"),
                    createdAt = vehicleJson.optString("createdAt", null as String?),
                    updatedAt = vehicleJson.optString("updatedAt", null as String?)
                )

                Log.d(TAG, "✅ Vehicle found: ${vehicle.name}")
                Result.success(vehicle)
            } else {
                Log.w(TAG, "⚠️ No vehicle found for driver: $driverId")
                Result.success(null) // Pas de véhicule assigné
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting vehicle by driver ID", e)
            Result.failure(e)
        }
    }
 
    /**
     * Get vehicle maintenance by vehicle ID using direct Neon connection
     */
    suspend fun getVehicleMaintenance(vehicleId: String): Result<List<VehicleMaintenance>> {
        return try {
            Log.d(TAG, "🔍 Getting vehicle maintenance for vehicle ID: $vehicleId")
 
            val jsonResponse = DatabaseManager.getVehicleMaintenance(vehicleId.toInt())
            Log.d(TAG, "📦 JSON Response: $jsonResponse")
 
            val jsonObject = JSONObject(jsonResponse)
            val rows = jsonObject.optJSONArray("rows")
 
            if (rows != null && rows.length() > 0) {
                val maintenanceList = mutableListOf<VehicleMaintenance>()
 
                for (i in 0 until rows.length()) {
                    val maintenanceJson = rows.getJSONObject(i)
 
                    val maintenance = VehicleMaintenance(
                        id = maintenanceJson.getInt("id").toString(),
                        vehicleId = maintenanceJson.getInt("vehicleId").toString(),
                        type = maintenanceJson.getString("type"),
                        date = maintenanceJson.optString("date", ""),
                        nextMaintenance = maintenanceJson.optString("nextMaintenance", null as String?),
                        estimatedCost = if (maintenanceJson.has("estimatedCost") && !maintenanceJson.isNull("estimatedCost")) {
                            maintenanceJson.getDouble("estimatedCost")
                        } else {
                            null
                        },
                        notes = maintenanceJson.optString("notes", null as String?),
                        technician = maintenanceJson.optString("technician", null as String?),
                        status = maintenanceJson.optString("status", "PENDING"),
                        vehicleName = maintenanceJson.optString("vehicleName", null as String?),
                        registration = maintenanceJson.optString("registration", null as String?)
                    )
 
                    maintenanceList.add(maintenance)
                }
 
                Log.d(TAG, "✅ Found ${maintenanceList.size} maintenance records")
                Result.success(maintenanceList)
            } else {
                Log.w(TAG, "⚠️ No maintenance found for vehicle: $vehicleId")
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting vehicle maintenance", e)
            Result.failure(e)
        }
    }
 
    /**
     * Calculate maintenance alert (same logic as original)
     */
    fun calculateMaintenanceAlert(maintenance: List<VehicleMaintenance>): MaintenanceAlert? {
        if (maintenance.isEmpty()) return null
 
        val latest = maintenance.first()
        val nextDate = latest.nextMaintenance
 
        if (nextDate == null) return null
 
        // Handle "YYYY-MM-DD HH:MM:SS" or "YYYY-MM-DDTHH:MM:SS" by extracting just the date
        val datePart = nextDate.substringBefore(" ").substringBefore("T")
        val nextMaintenanceDate = LocalDate.parse(datePart)
        val today = LocalDate.now()
        val daysRemaining = ChronoUnit.DAYS.between(today, nextMaintenanceDate).toInt()
 
        val warningLevel = when {
            daysRemaining <= 7 -> com.example.delivery.models.vehicle.WarningLevel.URGENT
            daysRemaining <= 30 -> com.example.delivery.models.vehicle.WarningLevel.WARNING
            else -> com.example.delivery.models.vehicle.WarningLevel.NORMAL
        }
 
        return MaintenanceAlert(
            type = latest.type,
            nextDate = nextDate,
            daysRemaining = daysRemaining,
            warningLevel = warningLevel,
            vehicleName = "", // Will need to be set by caller
            registration = "" // Will need to be set by caller
        )
    }
}