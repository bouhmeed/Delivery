package com.example.delivery.repository.delivery

import com.example.delivery.repository.Result

import com.example.delivery.database.DatabaseManager
import com.example.delivery.models.delivery.DeliveryItem
import com.example.delivery.models.driver.Trip
import com.example.delivery.models.delivery.TripWithDeliveries
import com.example.delivery.models.delivery.MultipleTripsWithDeliveries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DirectDeliveryTrackingRepository {
    
    fun getTripForDate(driverId: Int, date: String): Flow<Result<TripWithDeliveries>> = flow {
        emit(Result.loading())
        try {
            println("� CRASH_DEBUG: getTripForDate called with driverId=$driverId, date=$date")

            val tripQuery = """
                SELECT * FROM "Trip"
                WHERE "driverId" = $driverId AND "tripDate"::text LIKE '$date%'
                LIMIT 1
            """.trimIndent()
            println("� CRASH_DEBUG: Trip query: $tripQuery")

            val tripJsonArray = JSONObject(DatabaseManager.executeQuery(tripQuery)).optJSONArray("rows")
            println("� CRASH_DEBUG: Trip query result count: ${tripJsonArray?.length() ?: 0}")

            if (tripJsonArray == null || tripJsonArray.length() == 0) {
                println("� CRASH_DEBUG: No trip found for driverId=$driverId, date=$date")
                emit(Result.success(TripWithDeliveries(trip = null, deliveries = emptyList(), date = date)))
                return@flow
            }

            val tripRow = tripJsonArray.getJSONObject(0)
            val tripIdInt = tripRow.optInt("id")
            println("� CRASH_DEBUG: Trip found - id=$tripIdInt, tripNumber=${tripRow.optString("tripNumber")}, tripDate=${tripRow.optString("tripDate")}")

            val trip = Trip(
                id = tripRow.optString("id"),
                tripDate = tripRow.optString("tripDate"),
                depotId = tripRow.optString("departureAddressId"),
                driverId = tripRow.optString("driverId"),
                vehicleId = tripRow.optString("vehicleId"),
                status = tripRow.optString("status"),
                tenantId = tripRow.optString("tenantId"),
                createdAt = tripRow.optString("createdAt"),
                tripId = tripRow.optString("tripNumber")
            )

            val deliveriesQuery = """
                SELECT s.id, s."shipmentNo", s.type, s.status, s.description, s."distanceKm", s."estimatedDuration",
                       tsl.sequence as "tripSequence", tsl.status as "linkStatus", tsl.id as "tripShipmentLinkId", tsl."podDone",
                       c.name as "clientName", c.phone as "clientPhone",
                       1 as quantity, 'PCS' as uom,
                       origAddr.label as "originName", origAddr."address1" as "originAddress", origAddr.city as "originCity", origAddr."postalCode" as "originPostalCode",
                       destAddr.label as "destinationName", destAddr."address1" as "deliveryAddress", destAddr.city as "deliveryCity", destAddr."postalCode" as "deliveryZipCode", destAddr.country as "deliveryCountry"
                FROM "TripShipmentLink" tsl
                JOIN "Shipment" s ON tsl."shipmentId" = s.id
                LEFT JOIN "Client" c ON s."clientId" = c.id
                LEFT JOIN "ShipmentAddress" saOrig ON s.id = saOrig."shipmentId" AND saOrig.type = 'PICKUP'
                LEFT JOIN "Address" origAddr ON saOrig."addressId" = origAddr.id
                LEFT JOIN "ShipmentAddress" saDest ON s.id = saDest."shipmentId" AND saDest.type = 'DELIVERY'
                LEFT JOIN "Address" destAddr ON saDest."addressId" = destAddr.id
                WHERE tsl."tripId" = $tripIdInt
                ORDER BY tsl.sequence ASC
            """.trimIndent()
            println("� CRASH_DEBUG: Deliveries query: $deliveriesQuery")

            val deliveriesJsonArray = try {
                JSONObject(DatabaseManager.executeQuery(deliveriesQuery)).optJSONArray("rows")
            } catch (e: Exception) {
                println("🚨 CRASH_DEBUG_ERROR: Failed to execute deliveries query: ${e.message}")
                e.printStackTrace()
                emit(Result.error("Erreur lors de la récupération des livraisons: ${e.message}"))
                return@flow
            }
            println("� CRASH_DEBUG: Deliveries query result count: ${deliveriesJsonArray?.length() ?: 0}")

            val deliveries = mutableListOf<DeliveryItem>()

            if (deliveriesJsonArray != null) {
                for (i in 0 until deliveriesJsonArray.length()) {
                    try {
                        val sRow = deliveriesJsonArray.getJSONObject(i)
                        println("� CRASH_DEBUG: Shipment $i - id=${sRow.optInt("id")}, shipmentNo=${sRow.optString("shipmentNo")}, status=${sRow.optString("status")}")

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
                                originId = null,
                                destinationId = 0,
                                deliveryAddress = getNullableString(sRow, "deliveryAddress"),
                                deliveryCity = getNullableString(sRow, "deliveryCity"),
                                deliveryZipCode = getNullableString(sRow, "deliveryZipCode"),
                                deliveryCountry = getNullableString(sRow, "deliveryCountry"),
                                clientName = getNullableString(sRow, "clientName"),
                                clientPhone = getNullableString(sRow, "clientPhone"),
                                fullAddress = getNullableString(sRow, "deliveryAddress"),
                                locationCity = getNullableString(sRow, "deliveryCity"),
                                locationPostalCode = getNullableString(sRow, "deliveryZipCode"),
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
                    } catch (e: Exception) {
                        println("🚨 CRASH_DEBUG_ERROR: Failed to process shipment at index $i: ${e.message}")
                        e.printStackTrace()
                        // Continue with next shipment instead of crashing
                    }
                }
            }

            println("� CRASH_DEBUG: Total deliveries added: ${deliveries.size}")
            emit(Result.success(TripWithDeliveries(trip = trip, deliveries = deliveries, date = date)))
        } catch (e: Exception) {
            println("� CRASH_DEBUG_ERROR: Exception in getTripForDate: ${e.message}")
            e.printStackTrace()
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
                UPDATE "Shipment" SET status = 'EXPEDITION', "updatedAt" = NOW() WHERE id = $shipmentId RETURNING id
            """.trimIndent()
            val query2 = """
                UPDATE "TripShipmentLink" SET "podDone" = true, status = 'TERMINE' WHERE "shipmentId" = $shipmentId RETURNING id
            """.trimIndent()
            DatabaseManager.executeQuery(query)
            DatabaseManager.executeQuery(query2)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Exception: ${e.message}")
        }
    }

    fun getAllTripsForDate(driverId: Int, date: String): Flow<Result<MultipleTripsWithDeliveries>> = flow {
        emit(Result.loading())
        try {
            println("🔍 CRASH_DEBUG: getAllTripsForDate called with driverId=$driverId, date=$date")

            val tripsQuery = """
                SELECT * FROM "Trip"
                WHERE "driverId" = $driverId AND "tripDate"::text LIKE '$date%'
                ORDER BY "tripNumber" ASC
            """.trimIndent()
            println("🔍 CRASH_DEBUG: Trips query: $tripsQuery")

            val tripsJsonArray = JSONObject(DatabaseManager.executeQuery(tripsQuery)).optJSONArray("rows")
            println("🔍 CRASH_DEBUG: Trips query result count: ${tripsJsonArray?.length() ?: 0}")

            if (tripsJsonArray == null || tripsJsonArray.length() == 0) {
                println("🔍 CRASH_DEBUG: No trips found for driverId=$driverId, date=$date")
                emit(Result.success(MultipleTripsWithDeliveries(trips = emptyList(), date = date)))
                return@flow
            }

            val tripsWithDeliveries = mutableListOf<TripWithDeliveries>()

            for (i in 0 until tripsJsonArray.length()) {
                try {
                    val tripRow = tripsJsonArray.getJSONObject(i)
                    val tripIdInt = tripRow.optInt("id")
                    println("🔍 CRASH_DEBUG: Processing trip $i - id=$tripIdInt, tripNumber=${tripRow.optString("tripNumber")}")

                    val trip = Trip(
                        id = tripRow.optString("id"),
                        tripDate = tripRow.optString("tripDate"),
                        depotId = tripRow.optString("departureAddressId"),
                        driverId = tripRow.optString("driverId"),
                        vehicleId = tripRow.optString("vehicleId"),
                        status = tripRow.optString("status"),
                        tenantId = tripRow.optString("tenantId"),
                        createdAt = tripRow.optString("createdAt"),
                        tripId = tripRow.optString("tripNumber")
                    )

                    val deliveriesQuery = """
                        SELECT s.id, s."shipmentNo", s.type, s.status, s.description, s."distanceKm", s."estimatedDuration",
                               tsl.sequence as "tripSequence", tsl.status as "linkStatus", tsl.id as "tripShipmentLinkId", tsl."podDone",
                               c.name as "clientName", c.phone as "clientPhone",
                               1 as quantity, 'PCS' as uom,
                               origAddr.label as "originName", origAddr."address1" as "originAddress", origAddr.city as "originCity", origAddr."postalCode" as "originPostalCode",
                               destAddr.label as "destinationName", destAddr."address1" as "deliveryAddress", destAddr.city as "deliveryCity", destAddr."postalCode" as "deliveryZipCode", destAddr.country as "deliveryCountry"
                        FROM "TripShipmentLink" tsl
                        JOIN "Shipment" s ON tsl."shipmentId" = s.id
                        LEFT JOIN "Client" c ON s."clientId" = c.id
                        LEFT JOIN "ShipmentAddress" saOrig ON s.id = saOrig."shipmentId" AND saOrig.type = 'PICKUP'
                        LEFT JOIN "Address" origAddr ON saOrig."addressId" = origAddr.id
                        LEFT JOIN "ShipmentAddress" saDest ON s.id = saDest."shipmentId" AND saDest.type = 'DELIVERY'
                        LEFT JOIN "Address" destAddr ON saDest."addressId" = destAddr.id
                        WHERE tsl."tripId" = $tripIdInt
                        ORDER BY tsl.sequence ASC
                    """.trimIndent()

                    val deliveriesJsonArray = try {
                        JSONObject(DatabaseManager.executeQuery(deliveriesQuery)).optJSONArray("rows")
                    } catch (e: Exception) {
                        println("🚨 CRASH_DEBUG_ERROR: Failed to execute deliveries query for trip $tripIdInt: ${e.message}")
                        e.printStackTrace()
                        null
                    }
                    println("🔍 CRASH_DEBUG: Deliveries for trip $tripIdInt count: ${deliveriesJsonArray?.length() ?: 0}")

                    val deliveries = mutableListOf<DeliveryItem>()

                    if (deliveriesJsonArray != null) {
                        for (j in 0 until deliveriesJsonArray.length()) {
                            try {
                                val sRow = deliveriesJsonArray.getJSONObject(j)

                                val baseLat = 48.8566
                                val baseLon = 2.3522

                                val lat = baseLat + (j * 0.01)
                                val lon = baseLon + (j * 0.01)

                                deliveries.add(
                                    DeliveryItem(
                                        sequence = sRow.optInt("tripSequence", j),
                                        shipmentId = sRow.optInt("id"),
                                        status = getNullableString(sRow, "status") ?: "TO_PLAN",
                                        podDone = sRow.optBoolean("podDone", false),
                                        shipmentNo = getNullableString(sRow, "shipmentNo") ?: "",
                                        type = getNullableString(sRow, "type"),
                                        originId = null,
                                        destinationId = 0,
                                        deliveryAddress = getNullableString(sRow, "deliveryAddress"),
                                        deliveryCity = getNullableString(sRow, "deliveryCity"),
                                        deliveryZipCode = getNullableString(sRow, "deliveryZipCode"),
                                        deliveryCountry = getNullableString(sRow, "deliveryCountry"),
                                        clientName = getNullableString(sRow, "clientName"),
                                        clientPhone = getNullableString(sRow, "clientPhone"),
                                        fullAddress = getNullableString(sRow, "deliveryAddress"),
                                        locationCity = getNullableString(sRow, "deliveryCity"),
                                        locationPostalCode = getNullableString(sRow, "deliveryZipCode"),
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
                            } catch (e: Exception) {
                                println("🚨 CRASH_DEBUG_ERROR: Failed to process shipment at index $j: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    }

                    tripsWithDeliveries.add(TripWithDeliveries(trip = trip, deliveries = deliveries, date = date))
                    println("🔍 CRASH_DEBUG: Added trip ${trip.tripId} with ${deliveries.size} deliveries")
                } catch (e: Exception) {
                    println("🚨 CRASH_DEBUG_ERROR: Failed to process trip at index $i: ${e.message}")
                    e.printStackTrace()
                }
            }

            println("🔍 CRASH_DEBUG: Total trips processed: ${tripsWithDeliveries.size}")
            emit(Result.success(MultipleTripsWithDeliveries(trips = tripsWithDeliveries, date = date)))
        } catch (e: Exception) {
            println("🚨 CRASH_DEBUG_ERROR: Exception in getAllTripsForDate: ${e.message}")
            e.printStackTrace()
            emit(Result.error("Exception: ${e.message}"))
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