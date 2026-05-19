package com.example.delivery.repository.delivery

import com.example.delivery.repository.Result

import com.example.delivery.database.DatabaseManager
import com.example.delivery.models.delivery.DeliveryItem
import com.example.delivery.models.driver.Trip
import com.example.delivery.models.delivery.TripWithDeliveries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DirectDeliveryTrackingRepository {
    
    fun getTripForDate(driverId: Int, date: String): Flow<Result<TripWithDeliveries>> = flow {
        emit(Result.loading())
        try {
            val tripQuery = """
                SELECT * FROM "Trip" 
                WHERE "driverId" = $driverId AND "tripDate"::text LIKE '$date%'
                LIMIT 1
            """.trimIndent()
            val tripJsonArray = JSONObject(DatabaseManager.executeQuery(tripQuery)).optJSONArray("rows")
            
            if (tripJsonArray == null || tripJsonArray.length() == 0) {
                emit(Result.success(TripWithDeliveries(trip = null, deliveries = emptyList(), date = date)))
                return@flow
            }
            
            val tripRow = tripJsonArray.getJSONObject(0)
            val tripIdInt = tripRow.optInt("id")
            val trip = Trip(
                id = tripRow.optString("id"),
                tripDate = tripRow.optString("tripDate"),
                depotId = tripRow.optString("depotId"),
                driverId = tripRow.optString("driverId"),
                vehicleId = tripRow.optString("vehicleId"),
                status = tripRow.optString("status"),
                tenantId = tripRow.optString("tenantId"),
                createdAt = tripRow.optString("createdAt"),
                tripId = tripRow.optString("tripId")
            )
            
            val deliveriesQuery = """
                SELECT s.*, tsl.sequence as "tripSequence", tsl.status as "linkStatus", tsl.id as "tripShipmentLinkId", tsl."podDone", c.name as "clientName", c.phone as "clientPhone", l.address as "fullAddress", l.city as "locationCity", l."postalCode" as "locationPostalCode", ol.name as "originName", ol.address as "originAddress", ol.city as "originCity", ol."postalCode" as "originPostalCode"
                FROM "Shipment" s 
                JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId"
                LEFT JOIN "Client" c ON s."customerId" = c.id
                LEFT JOIN "Location" l ON s."destinationId" = l.id
                LEFT JOIN "Location" ol ON s."originId" = ol.id
                WHERE tsl."tripId" = $tripIdInt
                ORDER BY tsl.sequence ASC
            """.trimIndent()
            
            val deliveriesJsonArray = JSONObject(DatabaseManager.executeQuery(deliveriesQuery)).optJSONArray("rows")
            val deliveries = mutableListOf<DeliveryItem>()
            
            if (deliveriesJsonArray != null) {
                for (i in 0 until deliveriesJsonArray.length()) {
                    val sRow = deliveriesJsonArray.getJSONObject(i)
                    val baseLat = 48.8566
                    val baseLon = 2.3522
                    
                    val lat = baseLat + (i * 0.01)
                    val lon = baseLon + (i * 0.01)
                    
                    deliveries.add(
                        DeliveryItem(
                            sequence = sRow.optInt("tripSequence", i),
                            shipmentId = sRow.optInt("id"),
                            status = getNullableString(sRow, "status") ?: "TO_PLAN",
                            podDone = sRow.optBoolean("podDone", false),
                            shipmentNo = getNullableString(sRow, "shipmentNo") ?: "",
                            type = getNullableString(sRow, "type"),
                            originId = getNullableInt(sRow, "originId"),
                            destinationId = sRow.optInt("destinationId"),
                            deliveryAddress = getNullableString(sRow, "deliveryAddress"),
                            deliveryCity = getNullableString(sRow, "deliveryCity"),
                            deliveryZipCode = getNullableString(sRow, "deliveryZipCode"),
                            deliveryCountry = getNullableString(sRow, "deliveryCountry"),
                            clientName = getNullableString(sRow, "clientName"),
                            clientPhone = getNullableString(sRow, "clientPhone"),
                            fullAddress = getNullableString(sRow, "fullAddress"),
                            locationCity = getNullableString(sRow, "locationCity"),
                            locationPostalCode = getNullableString(sRow, "locationPostalCode"),
                            distanceKm = getNullableDouble(sRow, "distanceKm"),
                            estimatedDuration = getNullableInt(sRow, "estimatedDuration"),
                            quantity = sRow.optInt("quantity", 1),
                            uom = getNullableString(sRow, "uom") ?: "PCS",
                            tripIdentifier = trip.tripId,
                            tripShipmentLinkId = getNullableInt(sRow, "tripShipmentLinkId"),
                            latitude = lat,
                            longitude = lon,
                            originName = getNullableString(sRow, "originName"),
                            originAddress = getNullableString(sRow, "originAddress"),
                            originCity = getNullableString(sRow, "originCity"),
                            originPostalCode = getNullableString(sRow, "originPostalCode")
                        )
                    )
                }
            }
            
            emit(Result.success(TripWithDeliveries(trip = trip, deliveries = deliveries, date = date)))
        } catch (e: Exception) {
            emit(Result.error("Exception: ${e.message}"))
        }
    }
    
    suspend fun updateShipmentStatus(shipmentId: Int, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val query = """
                UPDATE "Shipment" SET status = '$status', "updatedAt" = NOW() WHERE id = $shipmentId RETURNING id
            """.trimIndent()
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            if (json != null && json.length() > 0) {
                Result.success(Unit)
            } else {
                Result.error("Shipment not found")
            }
        } catch (e: Exception) {
            Result.error("Exception: ${e.message}")
        }
    }
    
    suspend fun completeDelivery(shipmentId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val query = """
                UPDATE "Shipment" SET status = 'DELIVERED', "updatedAt" = NOW() WHERE id = $shipmentId RETURNING id
            """.trimIndent()
            val query2 = """
                UPDATE "TripShipmentLink" SET "podDone" = true, status = 'DELIVERED' WHERE "shipmentId" = $shipmentId RETURNING id
            """.trimIndent()
            DatabaseManager.executeQuery(query)
            DatabaseManager.executeQuery(query2)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Exception: ${e.message}")
        }
    }

    private fun getNullableString(json: JSONObject, key: String): String? {
        if (json.isNull(key)) return null
        val value = json.optString(key, null as String?)
        return if (value == "null") null else value
    }

    private fun getNullableInt(json: JSONObject, key: String): Int? {
        if (json.isNull(key)) return null
        val value = json.optInt(key, -999)
        return if (value == -999) null else value
    }

    private fun getNullableDouble(json: JSONObject, key: String): Double? {
        if (json.isNull(key)) return null
        val value = json.optDouble(key, Double.NaN)
        return if (value.isNaN()) null else value
    }
}