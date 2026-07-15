package com.example.delivery.repository.delivery

import com.example.delivery.repository.Result

import com.example.delivery.database.DatabaseManager
import com.example.delivery.models.*
import com.example.delivery.models.delivery.*
import com.example.delivery.models.driver.*
import com.example.delivery.models.user.*
import com.example.delivery.models.vehicle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DirectShipmentDetailRepository {

    fun getShipmentDetails(shipmentId: Int): Flow<Result<ShipmentDetailFull>> = flow {
        emit(Result.loading())
        try {
            // 1. Get Shipment
            val shipmentQuery = """
                SELECT * FROM "Shipment" WHERE id = $shipmentId LIMIT 1
            """.trimIndent()
            val shipmentJsonArray = JSONObject(DatabaseManager.executeQuery(shipmentQuery)).optJSONArray("rows")
            
            if (shipmentJsonArray == null || shipmentJsonArray.length() == 0) {
                emit(Result.error("Livraison non trouvée"))
                return@flow
            }
            
            val sRow = shipmentJsonArray.getJSONObject(0)
            
            // 2. Get Client/Customer
            val clientId = getNullableInt(sRow, "clientId")
            var customer: CustomerDetail? = null
            if (clientId != null) {
                val customerQuery = """
                    SELECT id, name, "firstName", "companyName", phone, email 
                    FROM "Client" 
                    WHERE id = $clientId LIMIT 1
                """.trimIndent()
                val customerJson = JSONObject(DatabaseManager.executeQuery(customerQuery)).optJSONArray("rows")?.optJSONObject(0)
                if (customerJson != null) {
                    val name = getNullableString(customerJson, "name") ?: ""
                    val firstName = getNullableString(customerJson, "firstName") ?: ""
                    val companyName = getNullableString(customerJson, "companyName") ?: ""
                    val displayName = if (name.isNotEmpty()) {
                        name
                    } else if (companyName.isNotEmpty()) {
                        companyName
                    } else if (firstName.isNotEmpty()) {
                        firstName
                    } else {
                        ""
                    }
                    customer = CustomerDetail(
                        id = customerJson.optInt("id"),
                        name = displayName,
                        address = null,
                        city = null,
                        postalCode = null,
                        phone = getNullableString(customerJson, "phone"),
                        email = getNullableString(customerJson, "email"),
                        contact = null
                    )
                }
            }
            
            // 3. Get Origin Address via ShipmentAddress
            var origin: ShipmentLocationDetail? = null
            val originQuery = """
                SELECT a.id, a.label as name, a."address1" as address, a.city, a."postalCode"
                FROM "ShipmentAddress" sa
                JOIN "Address" a ON sa."addressId" = a.id
                WHERE sa."shipmentId" = $shipmentId AND sa.type = 'PICKUP'
                LIMIT 1
            """.trimIndent()
            val originJson = JSONObject(DatabaseManager.executeQuery(originQuery)).optJSONArray("rows")?.optJSONObject(0)
            if (originJson != null) {
                origin = ShipmentLocationDetail(
                    id = originJson.optInt("id"),
                    name = getNullableString(originJson, "name"),
                    address = getNullableString(originJson, "address"),
                    city = getNullableString(originJson, "city"),
                    postalCode = getNullableString(originJson, "postalCode")
                )
            }
            
            // 4. Get Destination Address via ShipmentAddress
            var destination: ShipmentLocationDetail? = null
            val destQuery = """
                SELECT a.id, a.label as name, a."address1" as address, a.city, a."postalCode"
                FROM "ShipmentAddress" sa
                JOIN "Address" a ON sa."addressId" = a.id
                WHERE sa."shipmentId" = $shipmentId AND sa.type = 'DELIVERY'
                LIMIT 1
            """.trimIndent()
            val destJson = JSONObject(DatabaseManager.executeQuery(destQuery)).optJSONArray("rows")?.optJSONObject(0)
            if (destJson != null) {
                destination = ShipmentLocationDetail(
                    id = destJson.optInt("id"),
                    name = getNullableString(destJson, "name"),
                    address = getNullableString(destJson, "address"),
                    city = getNullableString(destJson, "city"),
                    postalCode = getNullableString(destJson, "postalCode")
                )
            }
            
            // 5. Get Driver Simple
            val driverId = getNullableInt(sRow, "driverId")
            var driver: DriverSimple? = null
            if (driverId != null) {
                val driverQuery = """
                    SELECT id, "firstName", "lastName", phone FROM "Driver" WHERE id = $driverId LIMIT 1
                """.trimIndent()
                val driverJson = JSONObject(DatabaseManager.executeQuery(driverQuery)).optJSONArray("rows")?.optJSONObject(0)
                if (driverJson != null) {
                    val firstName = getNullableString(driverJson, "firstName") ?: ""
                    val lastName = getNullableString(driverJson, "lastName") ?: ""
                    val driverName = if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                        "$firstName $lastName"
                    } else {
                        firstName.ifEmpty { lastName }.ifEmpty { "" }
                    }
                    driver = DriverSimple(
                        id = driverJson.optInt("id"),
                        name = driverName,
                        phone = getNullableString(driverJson, "phone")
                    )
                }
            }
            
            // 6. Get Vehicle Simple
            val vehicleId = getNullableInt(sRow, "vehicleId")
            var vehicle: VehicleSimple? = null
            if (vehicleId != null) {
                val vehicleQuery = """
                    SELECT id, brand, model, "registrationNumber" 
                    FROM "Vehicle" 
                    WHERE id = $vehicleId LIMIT 1
                """.trimIndent()
                val vehicleJson = JSONObject(DatabaseManager.executeQuery(vehicleQuery)).optJSONArray("rows")?.optJSONObject(0)
                if (vehicleJson != null) {
                    val brand = getNullableString(vehicleJson, "brand") ?: ""
                    val model = getNullableString(vehicleJson, "model") ?: ""
                    val vehicleName = if (brand.isNotEmpty() && model.isNotEmpty()) {
                        "$brand $model"
                    } else {
                        brand.ifEmpty { model }.ifEmpty { "" }
                    }
                    vehicle = VehicleSimple(
                        id = vehicleJson.optInt("id"),
                        name = vehicleName,
                        registration = getNullableString(vehicleJson, "registrationNumber")
                    )
                }
            }
            
            // 7. Get TripShipmentInfo
            var trip: TripShipmentInfo? = null
            val tripQuery = """
                SELECT tsl.sequence, tsl.role, tsl.status as "linkStatus", tsl."podDone", tsl."returnsDone", 
                       t.id as "tripIdInt", t."tripNumber", t."tripDate", t.status as "tripStatus"
                FROM "TripShipmentLink" tsl
                JOIN "Trip" t ON tsl."tripId" = t.id
                WHERE tsl."shipmentId" = $shipmentId
                LIMIT 1
            """.trimIndent()
            val tripJson = JSONObject(DatabaseManager.executeQuery(tripQuery)).optJSONArray("rows")?.optJSONObject(0)
            if (tripJson != null) {
                trip = TripShipmentInfo(
                    id = tripJson.optInt("tripIdInt"),
                    tripId = getNullableString(tripJson, "tripNumber"),
                    tripDate = getNullableString(tripJson, "tripDate"),
                    status = getNullableString(tripJson, "tripStatus"),
                    sequence = getNullableInt(tripJson, "sequence"),
                    role = getNullableString(tripJson, "role"),
                    linkStatus = getNullableString(tripJson, "linkStatus"),
                    podDone = tripJson.optBoolean("podDone", false),
                    returnsDone = tripJson.optBoolean("returnsDone", false)
                )
            }
            
            // 8. Get DeliveryProof
            var deliveryProof: DeliveryProof? = null
            var deliveryImages: List<DeliveryImage> = emptyList()
            val proofQuery = """
                SELECT id, "imageUrl", "signatureUrl", "createdAt" 
                FROM "ShipmentProof" 
                WHERE "shipmentId" = $shipmentId 
                LIMIT 1
            """.trimIndent()
            val proofJson = JSONObject(DatabaseManager.executeQuery(proofQuery)).optJSONArray("rows")?.optJSONObject(0)
            if (proofJson != null) {
                val proofId = proofJson.optInt("id")
                val imageUrl = getNullableString(proofJson, "imageUrl")
                val signatureUrl = getNullableString(proofJson, "signatureUrl")
                val createdAt = getNullableString(proofJson, "createdAt")

                deliveryProof = DeliveryProof(
                    id = proofId,
                    imageUrl = imageUrl,
                    signatureUrl = signatureUrl,
                    createdAt = createdAt
                )

                // Add the main proof image as a delivery image
                if (!imageUrl.isNullOrEmpty()) {
                    val proofImage = DeliveryImage(
                        id = "proof_$proofId",
                        gedDocId = "",
                        url = imageUrl,
                        documentType = "PROOF",
                        proofId = proofId.toString(),
                        createdAt = createdAt ?: ""
                    )
                    deliveryImages = listOf(proofImage)
                }
            }
            
            // 9. Get ShipmentReturns (for return/empty photos)
            var returnProofUrl: String? = null
            val returnsQuery = """
                SELECT "proofimageurl" 
                FROM "ShipmentReturns" 
                WHERE "shipmentId" = $shipmentId 
                LIMIT 1
            """.trimIndent()
            val returnsJson = JSONObject(DatabaseManager.executeQuery(returnsQuery)).optJSONArray("rows")?.optJSONObject(0)
            if (returnsJson != null) {
                returnProofUrl = getNullableString(returnsJson, "proofimageurl")
                println("🔍 DEBUG: Found return proof URL with length: ${returnProofUrl?.length}")
                // Add return photo as a delivery image if it exists
                if (!returnProofUrl.isNullOrEmpty()) {
                    val returnImage = DeliveryImage(
                        id = "return_${shipmentId}",
                        gedDocId = "",
                        url = returnProofUrl,
                        documentType = "RETURN_PROOF",
                        proofId = "return",
                        createdAt = ""
                    )
                    deliveryImages = deliveryImages + returnImage
                    println("🔍 DEBUG: Added return photo to deliveryImages. Total images: ${deliveryImages.size}")
                }
            }
            
            val shipmentDetail = ShipmentDetailFull(
                id = sRow.optInt("id"),
                shipmentNo = getNullableString(sRow, "shipmentNo"),
                customerId = clientId,
                type = sRow.optString("type", "OUTBOUND"),
                originId = origin?.id ?: 0,
                destinationId = destination?.id ?: 0,
                priority = sRow.optString("priority", "NORMAL"),
                requestedPickup = getNullableString(sRow, "requestedPickup"),
                requestedDelivery = getNullableString(sRow, "requestedDelivery"),
                status = sRow.optString("status", "TO_PLAN"),
                description = sRow.optString("description", ""),
                quantity = sRow.optInt("quantity", 1),
                uom = sRow.optString("uom", "PCS"),
                packaging = getNullableString(sRow, "packaging"),
                weight = getNullableDouble(sRow, "weight"),
                volume = getNullableDouble(sRow, "volume"),
                stackable = if (sRow.isNull("stackable")) null else sRow.optBoolean("stackable"),
                carrier = getNullableString(sRow, "carrier"),
                trackingNumber = getNullableString(sRow, "trackingNumber"),
                deliveryAddress = destination?.address,
                deliveryCity = destination?.city,
                deliveryZipCode = destination?.postalCode,
                deliveryCountry = "France",
                driverId = driverId,
                vehicleId = vehicleId,
                estimatedDuration = getNullableInt(sRow, "estimatedDuration"),
                plannedEnd = getNullableString(sRow, "plannedEnd"),
                plannedStart = getNullableString(sRow, "plannedStart"),
                distanceKm = getNullableDouble(sRow, "distanceKm"),
                createdAt = sRow.optString("createdAt", ""),
                updatedAt = sRow.optString("updatedAt", ""),
                customer = customer,
                origin = origin,
                destination = destination,
                driver = driver,
                vehicle = vehicle,
                trip = trip,
                deliveryImages = deliveryImages,
                deliveryDocuments = emptyList(),
                deliveryProof = deliveryProof
            )
            
            emit(Result.success(shipmentDetail))
        } catch (e: Exception) {
            emit(Result.error("Erreur direct db: ${e.message}"))
        }
    }

    suspend fun updateShipmentStatus(shipmentId: Int, status: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val query = """
                UPDATE "Shipment" SET status = '$status', "updatedAt" = NOW() WHERE id = $shipmentId RETURNING id
            """.trimIndent()
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            if (json != null && json.length() > 0) {
                Result.success("Statut mis à jour avec succès")
            } else {
                Result.error("Livraison non trouvée")
            }
        } catch (e: Exception) {
            Result.error("Erreur direct db: ${e.message}")
        }
    }

    suspend fun completeShipment(shipmentId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val query = """
                UPDATE "Shipment" SET status = 'LIVRE', "updatedAt" = NOW() WHERE id = $shipmentId RETURNING id
            """.trimIndent()
            val query2 = """
                UPDATE "TripShipmentLink" SET "podDone" = true, status = 'TERMINE' WHERE "shipmentId" = $shipmentId RETURNING id
            """.trimIndent()
            DatabaseManager.executeQuery(query)
            DatabaseManager.executeQuery(query2)
            Result.success("Livraison complétée avec succès")
        } catch (e: Exception) {
            Result.error("Erreur direct db: ${e.message}")
        }
    }

    fun getDisplayStatus(shipment: ShipmentDetailFull): ShipmentDisplayStatus {
        return when {
            shipment.status == "LIVRE" || shipment.status == "DELIVERED" -> ShipmentDisplayStatus.COMPLETED
            shipment.status == "EXPEDITION" -> ShipmentDisplayStatus.IN_PROGRESS
            shipment.status == "TO_PLAN" -> ShipmentDisplayStatus.NOT_STARTED
            else -> ShipmentDisplayStatus.NOT_STARTED
        }
    }
    
    fun belongsToCurrentTrip(shipment: ShipmentDetailFull, driverId: Int): Boolean {
        return shipment.trip != null && shipment.driver?.id == driverId
    }
    
    fun getTripSequence(shipment: ShipmentDetailFull): Int? {
        return shipment.trip?.sequence
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
