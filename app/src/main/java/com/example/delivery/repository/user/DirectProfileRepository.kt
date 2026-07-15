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
                SELECT id, "firstName", "lastName", phone, email, "licenseNumber",
                       "licenseExpiry", "licenseIssueDate", "employmentType",
                       "contractHoursWeek", status, "tenantId", "addressId",
                       "dateOfBirth", "hireDate", "assignedVehicleId", "createdAt", "updatedAt"
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
                firstName = dJson.getString("firstName"),
                lastName = dJson.getString("lastName"),
                phone = getNullableString(dJson, "phone"),
                email = getNullableString(dJson, "email"),
                licenseNumber = getNullableString(dJson, "licenseNumber"),
                licenseExpiry = getNullableString(dJson, "licenseExpiry"),
                licenseIssueDate = getNullableString(dJson, "licenseIssueDate"),
                employmentType = dJson.optString("employmentType", "FULL_TIME"),
                contractHoursWeek = dJson.optInt("contractHoursWeek", 40),
                status = dJson.optString("status", "ACTIF"),
                tenantId = dJson.optInt("tenantId", 0),
                addressId = if (!dJson.isNull("addressId")) dJson.getInt("addressId") else null,
                dateOfBirth = getNullableString(dJson, "dateOfBirth"),
                hireDate = getNullableString(dJson, "hireDate"),
                assignedVehicleId = if (!dJson.isNull("assignedVehicleId")) dJson.getInt("assignedVehicleId") else null,
                createdAt = dJson.optString("createdAt", ""),
                updatedAt = dJson.optString("updatedAt", "")
            )

            // 2. Fetch Vehicle Info (if exists)
            var vehicleInfo: VehicleInfo? = null
            val assignedVehicleId = profile.assignedVehicleId
            if (assignedVehicleId != null && assignedVehicleId > 0) {
                val vehicleQuery = """
                    SELECT v.id, vc.name as category_name, v."registrationNumber", v.brand, v.model,
                           v."capacityWeight", v."capacityVolume", v.year, v.status, v."fuelType",
                           v."hasLiftGate", v."hasRefrigeration", v."hasGPS"
                    FROM "Vehicle" v
                    LEFT JOIN "VehicleCategory" vc ON v."categoryId" = vc.id
                    WHERE v.id = $assignedVehicleId LIMIT 1
                """.trimIndent()
                try {
                    val vehicleRows = JSONObject(DatabaseManager.executeQuery(vehicleQuery)).optJSONArray("rows")
                    if (vehicleRows != null && vehicleRows.length() > 0) {
                        val vJson = vehicleRows.getJSONObject(0)
                        vehicleInfo = VehicleInfo(
                            id = vJson.getInt("id"),
                            name = vJson.optString("category_name", "Non spécifié"),
                            registrationNumber = vJson.getString("registrationNumber"),
                            brand = getNullableString(vJson, "brand"),
                            model = getNullableString(vJson, "model"),
                            category = vJson.optString("category_name", "Non spécifié"),
                            year = vJson.optInt("year", 2023),
                            capacityWeight = if (!vJson.isNull("capacityWeight")) vJson.optDouble("capacityWeight") else null,
                            capacityVolume = if (!vJson.isNull("capacityVolume")) vJson.optDouble("capacityVolume") else null,
                            status = vJson.optString("status", "ACTIVE"),
                            fuelType = vJson.optString("fuelType", "DIESEL"),
                            hasLiftGate = vJson.optBoolean("hasLiftGate", false),
                            hasRefrigeration = vJson.optBoolean("hasRefrigeration", false),
                            hasGPS = vJson.optBoolean("hasGPS", false)
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching vehicle details: ${e.message}")
                }
            }

            // 3. Fetch Address Info (using Address table)
            var addressInfo: AddressInfo? = null
            val addressId = profile.addressId
            if (addressId != null && addressId > 0) {
                val addressQuery = """
                    SELECT id, label, "address1", "address2", city, "postalCode", country,
                           "contactName", "contactPhone"
                    FROM "Address"
                    WHERE id = $addressId LIMIT 1
                """.trimIndent()
                try {
                    val addressRows = JSONObject(DatabaseManager.executeQuery(addressQuery)).optJSONArray("rows")
                    if (addressRows != null && addressRows.length() > 0) {
                        val aJson = addressRows.getJSONObject(0)
                        addressInfo = AddressInfo(
                            id = aJson.getInt("id"),
                            label = aJson.getString("label"),
                            address1 = aJson.getString("address1"),
                            address2 = getNullableString(aJson, "address2"),
                            city = aJson.getString("city"),
                            postalCode = aJson.getString("postalCode"),
                            country = aJson.getString("country"),
                            contactName = getNullableString(aJson, "contactName"),
                            contactPhone = getNullableString(aJson, "contactPhone")
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching address details: ${e.message}")
                }
            }

            Result.success(ProfileResponse(profile, vehicleInfo, addressInfo))
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

            // Query shipments metrics - quantity and weight are now in ShipmentLine
            val shipmentsQuery = """
                SELECT
                    COUNT(DISTINCT s.id)::int as total_shipments,
                    COUNT(DISTINCT CASE WHEN s.status = 'EXPEDITION' THEN s.id END)::int as delivered_shipments,
                    COALESCE(SUM(sl.quantity), 0)::float as total_qty,
                    COALESCE(SUM(sl.weight), 0)::float as total_weight,
                    COALESCE(AVG(sl.weight), 0)::float as avg_weight
                FROM "Shipment" s
                LEFT JOIN "ShipmentLine" sl ON s.id = sl."shipmentId"
                WHERE s."driverId" = $driverId
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

    suspend fun updateDriverProfile(driverId: Int, profile: DriverProfile): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 updateDriverProfile: driverId=$driverId, firstName=${profile.firstName}, lastName=${profile.lastName}, phone=${profile.phone}, email=${profile.email}")

            val updateQuery = """
                UPDATE "Driver"
                SET
                    "firstName" = '${profile.firstName}',
                    "lastName" = '${profile.lastName}',
                    phone = ${if (profile.phone != null) "'${profile.phone}'" else "NULL"},
                    email = ${if (profile.email != null) "'${profile.email}'" else "NULL"},
                    "updatedAt" = CURRENT_TIMESTAMP
                WHERE id = $driverId
            """.trimIndent()

            Log.d(TAG, "📝 SQL Query: $updateQuery")

            val result = DatabaseManager.executeQuery(updateQuery)
            Log.d(TAG, "✅ Update result: $result")

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in updateDriverProfile", e)
            Result.failure(e)
        }
    }
}
