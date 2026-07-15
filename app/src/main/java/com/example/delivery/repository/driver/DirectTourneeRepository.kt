package com.example.delivery.repository.driver

import android.util.Log
import com.example.delivery.database.DatabaseManager
import com.example.delivery.models.*
import com.example.delivery.models.delivery.*
import com.example.delivery.models.driver.*
import com.example.delivery.models.user.*
import com.example.delivery.models.vehicle.*
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.Result

class DirectTourneeRepository {
    private val TAG = "DirectTourneeRepo"

    suspend fun getUserByEmail(email: String): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val query = "SELECT * FROM \"User\" WHERE email = '$email'"
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            if (json == null || json.length() == 0) {
                return@withContext Result.failure(Exception("User not found"))
            }
            val row = json.getJSONObject(0)
            val user = UserResponse(
                id = row.optString("id"),
                tenantId = row.optString("tenantId"),
                email = row.optString("email"),
                role = row.optString("role"),
                firstName = row.optString("firstName"),
                lastName = row.optString("lastName"),
                driverId = row.optString("driverId"),
                isActive = row.optBoolean("isActive"),
                createdAt = row.optString("createdAt"),
                updatedAt = row.optString("updatedAt")
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTripsByDriver(driverId: String): Result<List<Trip>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Fetching trips for driverId: $driverId")
            val query = "SELECT * FROM \"Trip\" WHERE \"driverId\" = $driverId"
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            val trips = mutableListOf<Trip>()
            if (json != null) {
                for (i in 0 until json.length()) {
                    val row = json.getJSONObject(i)
                    val tripDriverId = row.optString("driverId")
                    val tripVehicleId = row.optString("vehicleId")
                    Log.d(TAG, "🔍 Trip ${row.optString("id")}: driverId=$tripDriverId, vehicleId=$tripVehicleId")
                    trips.add(
                        Trip(
                            id = row.optString("id"),
                            tripDate = row.optString("tripDate"),
                            depotId = row.optString("departureAddressId"),
                            driverId = tripDriverId,
                            vehicleId = tripVehicleId,
                            status = row.optString("status"),
                            tenantId = row.optString("tenantId"),
                            createdAt = row.optString("createdAt"),
                            tripId = row.optString("tripNumber"),
                            depotName = null,
                            depotAddress = null,
                            depotCity = null,
                            depotPostalCode = null
                        )
                    )
                }
            }
            Log.d(TAG, "✅ Found ${trips.size} trips")
            Result.success(trips)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching trips: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getShipmentsByTrip(tripId: Int): Result<List<ShipmentSearchDetail>> = withContext(Dispatchers.IO) {
        try {
            val query = """
                SELECT s.*, tsl.sequence as "tripSequence", tsl.status as "linkStatus", tsl."podDone"
                FROM "Shipment" s 
                JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId" 
                WHERE tsl."tripId" = $tripId
                ORDER BY tsl.sequence ASC
            """.trimIndent()
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            val shipments = mutableListOf<ShipmentSearchDetail>()
            if (json != null) {
                for (i in 0 until json.length()) {
                    val sRow = json.getJSONObject(i)
                    val shipmentId = sRow.optInt("id")
                    val clientId = if (!sRow.isNull("clientId")) sRow.optInt("clientId") else null
                    
                    // Fetch client details
                    val clientJson = if (clientId != null) {
                        JSONObject(DatabaseManager.executeQuery("SELECT * FROM \"Client\" WHERE id = $clientId")).optJSONArray("rows")?.optJSONObject(0)
                    } else null

                    // Fetch shipment addresses from ShipmentAddress and Address tables
                    val shipmentAddressesQuery = """
                        SELECT sa."addressId", sa.type, a.*
                        FROM "ShipmentAddress" sa
                        JOIN "Address" a ON sa."addressId" = a.id
                        WHERE sa."shipmentId" = $shipmentId
                    """.trimIndent()
                    val shipmentAddressesJson = JSONObject(DatabaseManager.executeQuery(shipmentAddressesQuery)).optJSONArray("rows")
                    
                    var deliveryAddress: String? = null
                    var deliveryCity: String? = null
                    var deliveryZipCode: String? = null
                    
                    if (shipmentAddressesJson != null) {
                        for (j in 0 until shipmentAddressesJson.length()) {
                            val addrRow = shipmentAddressesJson.getJSONObject(j)
                            val addressType = addrRow.optString("type")
                            if (addressType == "DELIVERY") {
                                deliveryAddress = addrRow.optString("address")
                                deliveryCity = addrRow.optString("city")
                                deliveryZipCode = addrRow.optString("postalCode")
                            }
                        }
                    }

                    // Fetch shipment line details for quantity
                    val shipmentLineQuery = """
                        SELECT * FROM "ShipmentLine" WHERE "shipmentId" = $shipmentId LIMIT 1
                    """.trimIndent()
                    val shipmentLineJson = JSONObject(DatabaseManager.executeQuery(shipmentLineQuery)).optJSONArray("rows")?.optJSONObject(0)
                    
                    val quantity = shipmentLineJson?.optDouble("quantity")?.toInt() ?: 1

                    shipments.add(
                        ShipmentSearchDetail(
                            id = shipmentId,
                            shipmentNo = sRow.optString("shipmentNo", ""),
                            trackingNumber = sRow.optString("trackingNumber"),
                            status = sRow.optString("status", ""),
                            description = sRow.optString("description", ""),
                            quantity = quantity,
                            deliveryAddress = deliveryAddress,
                            deliveryCity = deliveryCity ?: "",
                            deliveryZipCode = deliveryZipCode,
                            customerId = clientId ?: 0,
                            priority = sRow.optString("priority", "NORMAL"),
                            plannedStart = sRow.optString("plannedStart"),
                            plannedEnd = sRow.optString("plannedEnd")
                        )
                    )
                }
            }
            Result.success(shipments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVehicleById(vehicleId: String): Result<Vehicle> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Fetching vehicle with ID: $vehicleId")
            val query = "SELECT * FROM \"Vehicle\" WHERE id = $vehicleId"
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            if (json == null || json.length() == 0) {
                Log.e(TAG, "❌ Vehicle not found for ID: $vehicleId")
                return@withContext Result.failure(Exception("Vehicle not found"))
            }
            val row = json.getJSONObject(0)
            val brand = row.optString("brand", "")
            val model = row.optString("model", "")
            val registrationNumber = row.optString("registrationNumber", "")
            val vehicleName = if (brand.isNotEmpty() && model.isNotEmpty()) {
                "$brand $model"
            } else {
                brand.ifEmpty { model }.ifEmpty { registrationNumber }.ifEmpty { "Unknown" }
            }
            val vehicle = Vehicle(
                id = row.optString("id"),
                name = vehicleName,
                registration = registrationNumber,
                capacityWeight = row.optInt("capacityWeight"),
                capacityVolume = row.optInt("capacityVolume"),
                tenantId = row.optString("tenantId"),
                dernierControle = row.optString("technicalControlDate"),
                driverId = row.optString("driverId"),
                prochainControle = row.optString("nextMaintenanceDate"),
                status = row.optString("status"),
                type = row.optString("type"),
                createdAt = row.optString("createdAt"),
                updatedAt = row.optString("updatedAt")
            )
            Log.d(TAG, "✅ Vehicle found: $vehicleName ($registrationNumber)")
            Result.success(vehicle)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching vehicle: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getDriverById(driverId: String): Result<Driver> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Fetching driver with ID: $driverId")
            val query = "SELECT * FROM \"Driver\" WHERE id = $driverId"
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            if (json == null || json.length() == 0) {
                Log.e(TAG, "❌ Driver not found for ID: $driverId")
                return@withContext Result.failure(Exception("Driver not found"))
            }
            val row = json.getJSONObject(0)
            val firstName = row.optString("firstName", "")
            val lastName = row.optString("lastName", "")
            val fullName = if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                "$firstName $lastName"
            } else {
                firstName.ifEmpty { lastName }.ifEmpty { "Unknown" }
            }
            val driver = Driver(
                id = row.optString("id"),
                name = fullName,
                licenseNumber = row.optString("licenseNumber"),
                licenseExpiry = row.optString("licenseExpiry"),
                employmentType = row.optString("employmentType"),
                contractHoursWeek = row.optInt("contractHoursWeek"),
                homeDepotId = row.optString("homeDepotId"),
                tenantId = row.optString("tenantId"),
                status = row.optString("status"),
                address = row.optString("address"),
                assignedVehicle = row.optString("assignedVehicle"),
                createdAt = row.optString("createdAt"),
                updatedAt = row.optString("updatedAt")
            )
            Log.d(TAG, "✅ Driver found: $fullName")
            Result.success(driver)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching driver: ${e.message}", e)
            Result.failure(e)
        }
    }
}
