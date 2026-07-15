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
import com.example.delivery.network.api.driver.DeliverShipmentRequest

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

            val departureAddressId = tripRow.optInt("departureAddressId")
            val driverId = tripRow.optInt("driverId")
            val vehicleId = tripRow.optInt("vehicleId")

            // 2. Fetch Driver
            val driverQuery = "SELECT * FROM \"Driver\" WHERE id = $driverId"
            val driverJson = JSONObject(DatabaseManager.executeQuery(driverQuery)).optJSONArray("rows")?.optJSONObject(0)
            
            // 3. Fetch Vehicle
            val vehicleQuery = "SELECT * FROM \"Vehicle\" WHERE id = $vehicleId"
            val vehicleJson = JSONObject(DatabaseManager.executeQuery(vehicleQuery)).optJSONArray("rows")?.optJSONObject(0)

            // 4. Fetch Departure Address (was Depot, now in Address table)
            val departureAddressQuery = "SELECT * FROM \"Address\" WHERE id = $departureAddressId"
            val departureAddressJson = JSONObject(DatabaseManager.executeQuery(departureAddressQuery)).optJSONArray("rows")?.optJSONObject(0)

            // 5. Fetch Shipments with TripShipmentLink
            val shipmentsQuery = """
                SELECT s.*, tsl.sequence as "tripSequence", tsl.role as "shipmentRole", 
                       tsl.status as "linkStatus", tsl."podDone", tsl."returnsDone"
                FROM "Shipment" s 
                JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId" 
                WHERE tsl."tripId" = $tripId
                ORDER BY tsl.sequence ASC
            """.trimIndent()
            val shipmentsJson = JSONObject(DatabaseManager.executeQuery(shipmentsQuery)).optJSONArray("rows")
            Log.d(TAG, "🔍 Found ${shipmentsJson?.length() ?: 0} shipments for trip $tripId")

            val tripDetail = TripDetail(
                id = tripRow.getInt("id"),
                tripId = tripRow.optString("tripNumber", ""),
                tripDate = tripRow.optString("tripDate", ""),
                depotId = departureAddressId,
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

            val firstName = driverJson?.optString("firstName", "") ?: ""
            val lastName = driverJson?.optString("lastName", "") ?: ""
            val driverName = if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                "$firstName $lastName"
            } else {
                firstName.ifEmpty { lastName }.ifEmpty { "Unknown" }
            }
            
            val driverDetail = DriverDetail(
                id = driverJson?.optInt("id") ?: 0,
                name = driverName,
                licenseNumber = driverJson?.optString("licenseNumber"),
                employmentType = driverJson?.optString("employmentType"),
                status = driverJson?.optString("status", "") ?: "",
                phoneNumber = driverJson?.optString("phone"),
                email = driverJson?.optString("email"),
                address = driverJson?.optString("address"),
                city = driverJson?.optString("city"),
                postalCode = driverJson?.optString("postalCode")
            )

            val brand = vehicleJson?.optString("brand", "") ?: ""
            val model = vehicleJson?.optString("model", "") ?: ""
            val registrationNumber = vehicleJson?.optString("registrationNumber", "") ?: ""
            val vehicleName = if (brand.isNotEmpty() && model.isNotEmpty()) {
                "$brand $model"
            } else {
                brand.ifEmpty { model }.ifEmpty { registrationNumber }.ifEmpty { "Unknown" }
            }
            
            val vehicleDetail = VehicleDetail(
                id = vehicleJson?.optInt("id") ?: 0,
                name = vehicleName,
                registration = registrationNumber,
                capacityWeight = vehicleJson?.optDouble("capacityWeight", 0.0) ?: 0.0,
                capacityVolume = vehicleJson?.optDouble("capacityVolume", 0.0) ?: 0.0,
                type = vehicleJson?.optString("type"),
                status = vehicleJson?.optString("status"),
                year = vehicleJson?.optInt("year", 0) ?: 0
            )

            val depotDetail = LocationDetail(
                id = departureAddressJson?.optInt("id") ?: 0,
                name = departureAddressJson?.optString("name", "Unknown") ?: "Unknown",
                address = departureAddressJson?.optString("address"),
                city = departureAddressJson?.optString("city"),
                postalCode = departureAddressJson?.optString("postalCode"),
                country = departureAddressJson?.optString("country"),
                latitude = if (departureAddressJson != null && !departureAddressJson.isNull("latitude")) departureAddressJson.optDouble("latitude") else null,
                longitude = if (departureAddressJson != null && !departureAddressJson.isNull("longitude")) departureAddressJson.optDouble("longitude") else null,
                locationType = departureAddressJson?.optString("locationType")
            )

            val shipments = mutableListOf<ShipmentDetail>()
            if (shipmentsJson != null) {
                for (i in 0 until shipmentsJson.length()) {
                    val sRow = shipmentsJson.getJSONObject(i)
                    
                    val shipmentId = sRow.optInt("id")
                    val shipmentNo = sRow.optString("shipmentNo")
                    val clientId = if (!sRow.isNull("clientId")) sRow.optInt("clientId") else null
                    
                    Log.d(TAG, "🔍 Processing shipment $shipmentId ($shipmentNo), clientId=$clientId")
                    
                    // Fetch client details if clientId exists
                    val clientJson = if (clientId != null) {
                        JSONObject(DatabaseManager.executeQuery("SELECT * FROM \"Client\" WHERE id = $clientId")).optJSONArray("rows")?.optJSONObject(0)
                    } else null
                    
                    if (clientJson != null) {
                        Log.d(TAG, "✅ Client found: ${clientJson.optString("name")}")
                    } else {
                        Log.w(TAG, "⚠️ Client not found for clientId=$clientId")
                    }

                    // Fetch shipment addresses from ShipmentAddress and Address tables
                    val shipmentAddressesQuery = """
                        SELECT sa."addressId", sa.type, a.*
                        FROM "ShipmentAddress" sa
                        JOIN "Address" a ON sa."addressId" = a.id
                        WHERE sa."shipmentId" = $shipmentId
                    """.trimIndent()
                    val shipmentAddressesJson = JSONObject(DatabaseManager.executeQuery(shipmentAddressesQuery)).optJSONArray("rows")
                    
                    Log.d(TAG, "🔍 Found ${shipmentAddressesJson?.length() ?: 0} addresses for shipment $shipmentId")
                    
                    var origin: LocationDetail? = null
                    var destination: LocationDetail? = null
                    
                    if (shipmentAddressesJson != null) {
                        for (j in 0 until shipmentAddressesJson.length()) {
                            val addrRow = shipmentAddressesJson.getJSONObject(j)
                            val addressType = addrRow.optString("type")
                            val addressLabel = addrRow.optString("label", "")
                            Log.d(TAG, "🔍 Address type=$addressType, label=$addressLabel")
                            if (addressType == "PICKUP") {
                                origin = LocationDetail(
                                    id = addrRow.optInt("addressId"),
                                    name = addressLabel,
                                    address = addrRow.optString("address1"),
                                    city = addrRow.optString("city"),
                                    postalCode = addrRow.optString("postalCode"),
                                    country = addrRow.optString("country"),
                                    latitude = if (!addrRow.isNull("latitude")) addrRow.optDouble("latitude") else null,
                                    longitude = if (!addrRow.isNull("longitude")) addrRow.optDouble("longitude") else null,
                                    locationType = null
                                )
                            } else if (addressType == "DELIVERY") {
                                destination = LocationDetail(
                                    id = addrRow.optInt("addressId"),
                                    name = addressLabel,
                                    address = addrRow.optString("address1"),
                                    city = addrRow.optString("city"),
                                    postalCode = addrRow.optString("postalCode"),
                                    country = addrRow.optString("country"),
                                    latitude = if (!addrRow.isNull("latitude")) addrRow.optDouble("latitude") else null,
                                    longitude = if (!addrRow.isNull("longitude")) addrRow.optDouble("longitude") else null,
                                    locationType = null
                                )
                            }
                        }
                    }

                    // Fetch shipment line details for quantity and uom
                    val shipmentLineQuery = """
                        SELECT * FROM "ShipmentLine" WHERE "shipmentId" = $shipmentId LIMIT 1
                    """.trimIndent()
                    val shipmentLineJson = JSONObject(DatabaseManager.executeQuery(shipmentLineQuery)).optJSONArray("rows")?.optJSONObject(0)
                    
                    val quantity = shipmentLineJson?.optDouble("quantity")?.toInt() ?: 0
                    val uom = shipmentLineJson?.optString("unit") ?: "PCS"
                    
                    Log.d(TAG, "🔍 ShipmentLine: quantity=$quantity, uom=$uom")

                    val customer = if (clientJson != null) {
                        ClientDetail(
                            id = clientJson.optInt("id"),
                            name = clientJson.optString("name"),
                            address = clientJson.optString("address"),
                            city = clientJson.optString("city"),
                            postalCode = clientJson.optString("postalCode"),
                            phone = clientJson.optString("phone"),
                            email = clientJson.optString("email"),
                            contact = clientJson.optString("contact")
                        )
                    } else null

                    Log.d(TAG, "🔍 Origin: ${origin?.name ?: "null"}, Destination: ${destination?.name ?: "null"}")
                    Log.d(TAG, "🔍 Customer: ${customer?.name ?: "null"}")
                    
                    val shipmentType = sRow.optString("type", "OUTBOUND")
                    Log.d(TAG, "🔍 Shipment type: $shipmentType")
                    
                    // For OUTBOUND shipments, origin is the depot (departure address)
                    // For INBOUND/TRANSFER shipments, origin comes from ShipmentAddress with type PICKUP
                    if (shipmentType == "OUTBOUND" && origin == null && departureAddressJson != null) {
                        origin = LocationDetail(
                            id = departureAddressJson.optInt("id"),
                            name = departureAddressJson.optString("label", "Dépôt"),
                            address = departureAddressJson.optString("address1"),
                            city = departureAddressJson.optString("city"),
                            postalCode = departureAddressJson.optString("postalCode"),
                            country = departureAddressJson.optString("country"),
                            latitude = if (!departureAddressJson.isNull("latitude")) departureAddressJson.optDouble("latitude") else null,
                            longitude = if (!departureAddressJson.isNull("longitude")) departureAddressJson.optDouble("longitude") else null,
                            locationType = null
                        )
                        Log.d(TAG, "🔍 OUTBOUND shipment: Using depot as origin: ${origin.name}")
                    } else if (origin == null) {
                        Log.w(TAG, "⚠️ No origin address found for $shipmentType shipment")
                    }

                    shipments.add(
                        ShipmentDetail(
                            id = shipmentId,
                            shipmentNo = sRow.optString("shipmentNo"),
                            customerId = clientId,
                            type = sRow.optString("type", ""),
                            originId = origin?.id ?: 0,
                            destinationId = destination?.id ?: 0,
                            priority = sRow.optString("priority", ""),
                            requestedPickup = sRow.optString("requestedPickup"),
                            requestedDelivery = sRow.optString("requestedDelivery"),
                            status = sRow.optString("status", ""),
                            description = sRow.optString("description", ""),
                            quantity = quantity,
                            uom = uom,
                            packaging = shipmentLineJson?.optString("packageType"),
                            weight = shipmentLineJson?.optDouble("weight"),
                            volume = shipmentLineJson?.optDouble("volume"),
                            stackable = null,
                            carrier = null,
                            trackingNumber = sRow.optString("trackingNumber"),
                            deliveryAddress = destination?.address,
                            deliveryCity = destination?.city,
                            deliveryZipCode = destination?.postalCode,
                            deliveryCountry = destination?.country ?: "",
                            driverId = if (!sRow.isNull("driverId")) sRow.optInt("driverId") else null,
                            vehicleId = if (!sRow.isNull("vehicleId")) sRow.optInt("vehicleId") else null,
                            estimatedDuration = if (!sRow.isNull("estimatedDuration")) sRow.optInt("estimatedDuration") else null,
                            plannedEnd = sRow.optString("plannedEnd"),
                            plannedStart = sRow.optString("plannedStart"),
                            distanceKm = if (!sRow.isNull("distanceKm")) sRow.optDouble("distanceKm") else null,
                            tripSequence = if (!sRow.isNull("tripSequence")) sRow.optInt("tripSequence") else null,
                            shipmentRole = sRow.optString("shipmentRole"),
                            linkStatus = sRow.optString("linkStatus"),
                            podDone = if (!sRow.isNull("podDone")) sRow.optBoolean("podDone") else null,
                            returnsDone = if (!sRow.isNull("returnsDone")) sRow.optBoolean("returnsDone") else null,
                            customerName = customer?.name,
                            customerAddress = customer?.address,
                            customerCity = customer?.city,
                            customerPhone = customer?.phone,
                            originName = origin?.name,
                            originAddress = origin?.address,
                            originCity = origin?.city,
                            destinationName = destination?.name,
                            destinationAddress = destination?.address,
                            destinationCity = destination?.city,
                            vehicleName = null,
                            vehicleRegistration = null,
                            customer = customer,
                            executionStatus = null,
                            origin = origin ?: LocationDetail(0, "", null, null, null, null, null, null, null),
                            destination = destination ?: LocationDetail(0, "", null, null, null, null, null, null, null)
                        )
                    )
                }
            }

            Result.success(TripDetailData(
                trip = tripDetail,
                shipments = shipments,
                stops = emptyList(),
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
                SET status = 'EXPEDITION', "updatedAt" = NOW() 
                WHERE id = $shipmentId 
                RETURNING id
            """.trimIndent()
            
            val query2 = """
                UPDATE "TripShipmentLink" 
                SET "podDone" = true, status = 'DELIVERED' 
                WHERE "shipmentId" = $shipmentId AND "tripId" = $tripId
                RETURNING id
            """.trimIndent()
            
            DatabaseManager.executeQuery(query)
            DatabaseManager.executeQuery(query2)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
