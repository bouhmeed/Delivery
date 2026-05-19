package com.example.delivery.repository

import android.util.Log
import com.example.delivery.database.DatabaseManager
import com.example.delivery.models.*
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
            val query = "SELECT * FROM \"Trip\" WHERE \"driverId\" = $driverId"
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            val trips = mutableListOf<Trip>()
            if (json != null) {
                for (i in 0 until json.length()) {
                    val row = json.getJSONObject(i)
                    trips.add(
                        Trip(
                            id = row.optString("id"),
                            tripDate = row.optString("tripDate"),
                            depotId = row.optString("depotId"),
                            driverId = row.optString("driverId"),
                            vehicleId = row.optString("vehicleId"),
                            status = row.optString("status"),
                            tenantId = row.optString("tenantId"),
                            createdAt = row.optString("createdAt"),
                            tripId = row.optString("tripId"),
                            depotName = null,
                            depotAddress = null,
                            depotCity = null,
                            depotPostalCode = null
                        )
                    )
                }
            }
            Result.success(trips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getShipmentsByTrip(tripId: Int): Result<List<ShipmentSearchDetail>> = withContext(Dispatchers.IO) {
        try {
            val query = """
                SELECT s.* 
                FROM "Shipment" s 
                JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId" 
                WHERE tsl."tripId" = $tripId
            """.trimIndent()
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            val shipments = mutableListOf<ShipmentSearchDetail>()
            if (json != null) {
                for (i in 0 until json.length()) {
                    val sRow = json.getJSONObject(i)
                    shipments.add(
                        ShipmentSearchDetail(
                            id = sRow.optInt("id"),
                            shipmentNo = sRow.optString("shipmentNo", ""),
                            trackingNumber = sRow.optString("trackingNumber"),
                            status = sRow.optString("status", ""),
                            description = sRow.optString("description", ""),
                            quantity = sRow.optInt("quantity", 1),
                            deliveryAddress = sRow.optString("deliveryAddress"),
                            deliveryCity = sRow.optString("deliveryCity", ""),
                            deliveryZipCode = sRow.optString("deliveryZipCode"),
                            customerId = sRow.optInt("customerId", 0),
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
            val query = "SELECT * FROM \"Vehicle\" WHERE id = $vehicleId"
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            if (json == null || json.length() == 0) {
                return@withContext Result.failure(Exception("Vehicle not found"))
            }
            val row = json.getJSONObject(0)
            val vehicle = Vehicle(
                id = row.optString("id"),
                name = row.optString("name", "Unknown"),
                registration = row.optString("registration"),
                capacityWeight = row.optInt("capacityWeight"),
                capacityVolume = row.optInt("capacityVolume"),
                tenantId = row.optString("tenantId"),
                dernierControle = row.optString("dernierControle"),
                driverId = row.optString("driverId"),
                prochainControle = row.optString("prochainControle"),
                status = row.optString("status"),
                type = row.optString("type"),
                createdAt = row.optString("createdAt"),
                updatedAt = row.optString("updatedAt")
            )
            Result.success(vehicle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDriverById(driverId: String): Result<Driver> = withContext(Dispatchers.IO) {
        try {
            val query = "SELECT * FROM \"Driver\" WHERE id = $driverId"
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            if (json == null || json.length() == 0) {
                return@withContext Result.failure(Exception("Driver not found"))
            }
            val row = json.getJSONObject(0)
            val driver = Driver(
                id = row.optString("id"),
                name = row.optString("name", "Unknown"),
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
            Result.success(driver)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
