package com.example.delivery.repository

import android.util.Log
import com.example.delivery.database.DatabaseManager
import com.example.delivery.models.*
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.Result
import com.example.delivery.network.DeliverShipmentRequest

class DirectTripDetailRepository {
    private val TAG = "DirectTripDetailRepo"

    suspend fun getTripDetails(tripId: Int): Result<TripDetailData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Getting trip details for trip: $tripId via Neon SQL")

            // 1. Fetch Trip
            val tripQuery = "SELECT * FROM \"Trip\" WHERE id = $tripId"
            val tripJson = JSONObject(DatabaseManager.executeQuery(tripQuery)).optJSONArray("rows")
            if (tripJson == null || tripJson.length() == 0) {
                return@withContext Result.failure(Exception("Trip not found"))
            }
            val tripRow = tripJson.getJSONObject(0)

            val depotId = tripRow.optInt("depotId")
            val driverId = tripRow.optInt("driverId")
            val vehicleId = tripRow.optInt("vehicleId")

            // 2. Fetch Driver
            val driverQuery = "SELECT * FROM \"Driver\" WHERE id = $driverId"
            val driverJson = JSONObject(DatabaseManager.executeQuery(driverQuery)).optJSONArray("rows")?.optJSONObject(0)
            
            // 3. Fetch Vehicle
            val vehicleQuery = "SELECT * FROM \"Vehicle\" WHERE id = $vehicleId"
            val vehicleJson = JSONObject(DatabaseManager.executeQuery(vehicleQuery)).optJSONArray("rows")?.optJSONObject(0)

            // 4. Fetch Depot
            val depotQuery = "SELECT * FROM \"Location\" WHERE id = $depotId"
            val depotJson = JSONObject(DatabaseManager.executeQuery(depotQuery)).optJSONArray("rows")?.optJSONObject(0)

            // 5. Fetch Stops
            val stopsQuery = "SELECT * FROM \"TripStop\" WHERE \"tripId\" = $tripId"
            val stopsJson = JSONObject(DatabaseManager.executeQuery(stopsQuery)).optJSONArray("rows")

            // 6. Fetch Shipments
            val shipmentsQuery = """
                SELECT s.*, tsl.sequence as "tripSequence", tsl.role as "shipmentRole"
                FROM "Shipment" s 
                JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId" 
                WHERE tsl."tripId" = $tripId
            """.trimIndent()
            val shipmentsJson = JSONObject(DatabaseManager.executeQuery(shipmentsQuery)).optJSONArray("rows")

            // Parse objects
            val tripDetail = TripDetail(
                id = tripRow.getInt("id"),
                tripId = tripRow.optString("tripId", ""),
                tripDate = tripRow.optString("tripDate", ""),
                depotId = depotId,
                driverId = driverId,
                vehicleId = vehicleId,
                status = tripRow.optString("status", ""),
                tenantId = tripRow.optInt("tenantId", 0),
                createdAt = tripRow.optString("createdAt", ""),
                totalShipments = tripRow.optInt("totalShipments", 0),
                completedShipments = tripRow.optInt("completedShipments", 0),
                totalDistance = tripRow.optDouble("totalDistance", 0.0),
                estimatedDuration = tripRow.optInt("estimatedDuration", 0),
                actualDuration = if (!tripRow.isNull("actualDuration")) tripRow.optInt("actualDuration") else null
            )

            val driverDetail = DriverDetail(
                id = driverJson?.optInt("id") ?: 0,
                name = driverJson?.optString("name", "Unknown") ?: "Unknown",
                licenseNumber = driverJson?.optString("licenseNumber"),
                employmentType = driverJson?.optString("employmentType"),
                status = driverJson?.optString("status", "") ?: "",
                phoneNumber = driverJson?.optString("phone"),
                email = driverJson?.optString("email"),
                address = driverJson?.optString("address"),
                city = driverJson?.optString("city"),
                postalCode = driverJson?.optString("postalCode")
            )

            val vehicleDetail = VehicleDetail(
                id = vehicleJson?.optInt("id") ?: 0,
                name = vehicleJson?.optString("name", "Unknown") ?: "Unknown",
                registration = vehicleJson?.optString("registration", "") ?: "",
                capacityWeight = vehicleJson?.optDouble("capacityWeight", 0.0) ?: 0.0,
                capacityVolume = vehicleJson?.optDouble("capacityVolume", 0.0) ?: 0.0,
                type = vehicleJson?.optString("type"),
                status = vehicleJson?.optString("status"),
                year = vehicleJson?.optInt("year", 0) ?: 0
            )

            val depotDetail = LocationDetail(
                id = depotJson?.optInt("id") ?: 0,
                name = depotJson?.optString("name", "Unknown") ?: "Unknown",
                address = depotJson?.optString("address"),
                city = depotJson?.optString("city"),
                postalCode = depotJson?.optString("postalCode"),
                country = depotJson?.optString("country"),
                latitude = if (depotJson != null && !depotJson.isNull("latitude")) depotJson.optDouble("latitude") else null,
                longitude = if (depotJson != null && !depotJson.isNull("longitude")) depotJson.optDouble("longitude") else null,
                locationType = depotJson?.optString("locationType")
            )

            val stops = mutableListOf<TripStopDetail>()
            if (stopsJson != null) {
                for (i in 0 until stopsJson.length()) {
                    val row = stopsJson.getJSONObject(i)
                    stops.add(TripStopDetail(
                        id = row.optInt("id"),
                        tripId = row.optInt("tripId"),
                        sequence = row.optInt("sequence"),
                        locationId = row.optInt("locationId"),
                        stopType = row.optString("stopType"),
                        locationName = row.optString("locationName"),
                        locationAddress = row.optString("locationAddress"),
                        locationCity = row.optString("locationCity"),
                        locationPostalCode = row.optString("locationPostalCode"),
                        estimatedArrival = row.optString("estimatedArrival"),
                        actualArrival = row.optString("actualArrival"),
                        estimatedDeparture = row.optString("estimatedDeparture"),
                        actualDeparture = row.optString("actualDeparture"),
                        shipments = emptyList()
                    ))
                }
            }

            val shipments = mutableListOf<ShipmentDetail>()
            if (shipmentsJson != null) {
                for (i in 0 until shipmentsJson.length()) {
                    val sRow = shipmentsJson.getJSONObject(i)
                    shipments.add(ShipmentDetail(
                        id = sRow.optInt("id"),
                        shipmentNo = sRow.optString("shipmentNo"),
                        customerId = if (!sRow.isNull("customerId")) sRow.optInt("customerId") else null,
                        type = sRow.optString("type", ""),
                        originId = sRow.optInt("originId", 0),
                        destinationId = sRow.optInt("destinationId", 0),
                        priority = sRow.optString("priority", ""),
                        requestedPickup = sRow.optString("requestedPickup"),
                        requestedDelivery = sRow.optString("requestedDelivery"),
                        status = sRow.optString("status", ""),
                        description = sRow.optString("description", ""),
                        quantity = sRow.optInt("quantity", 0),
                        uom = sRow.optString("uom", ""),
                        packaging = sRow.optString("packaging"),
                        weight = if (!sRow.isNull("weight")) sRow.optDouble("weight") else null,
                        volume = if (!sRow.isNull("volume")) sRow.optDouble("volume") else null,
                        stackable = if (!sRow.isNull("stackable")) sRow.optBoolean("stackable") else null,
                        carrier = sRow.optString("carrier"),
                        trackingNumber = sRow.optString("trackingNumber"),
                        deliveryAddress = sRow.optString("deliveryAddress"),
                        deliveryCity = sRow.optString("deliveryCity"),
                        deliveryZipCode = sRow.optString("deliveryZipCode"),
                        deliveryCountry = sRow.optString("deliveryCountry", ""),
                        driverId = if (!sRow.isNull("driverId")) sRow.optInt("driverId") else null,
                        vehicleId = if (!sRow.isNull("vehicleId")) sRow.optInt("vehicleId") else null,
                        estimatedDuration = if (!sRow.isNull("estimatedDuration")) sRow.optInt("estimatedDuration") else null,
                        plannedEnd = sRow.optString("plannedEnd"),
                        plannedStart = sRow.optString("plannedStart"),
                        distanceKm = if (!sRow.isNull("distanceKm")) sRow.optDouble("distanceKm") else null,
                        tripSequence = if (!sRow.isNull("tripSequence")) sRow.optInt("tripSequence") else null,
                        shipmentRole = sRow.optString("shipmentRole"),
                        linkStatus = null,
                        podDone = null,
                        returnsDone = null,
                        customerName = null,
                        customerAddress = null,
                        customerCity = null,
                        customerPhone = null,
                        originName = null,
                        originAddress = null,
                        originCity = null,
                        destinationName = null,
                        destinationAddress = null,
                        destinationCity = null,
                        vehicleName = null,
                        vehicleRegistration = null,
                        origin = LocationDetail(id = sRow.optInt("originId", 0), name = "Origin", address = null, city = null, postalCode = null, country = null, latitude = null, longitude = null, locationType = null),
                        destination = LocationDetail(id = sRow.optInt("destinationId", 0), name = "Destination", address = null, city = null, postalCode = null, country = null, latitude = null, longitude = null, locationType = null),
                        customer = null,
                        executionStatus = null
                    ))
                }
            }

            Result.success(TripDetailData(
                trip = tripDetail,
                shipments = shipments,
                stops = stops,
                driver = driverDetail,
                vehicle = vehicleDetail,
                depot = depotDetail
            ))

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting trip details", e)
            Result.failure(e)
        }
    }

    suspend fun deliverShipment(tripId: Int, shipmentId: Int, request: DeliverShipmentRequest): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val query = """
                UPDATE "Shipment" 
                SET status = 'DELIVERED', "updatedAt" = NOW() 
                WHERE id = $shipmentId 
                RETURNING id
            """.trimIndent()
            
            val jsonResponse = JSONObject(DatabaseManager.executeQuery(query))
            val rows = jsonResponse.optJSONArray("rows")
            
            if (rows != null && rows.length() > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("Shipment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
