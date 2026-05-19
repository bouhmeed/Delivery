package com.example.delivery.repository

import com.example.delivery.database.DatabaseManager
import com.example.delivery.network.ShipmentDatesResponse
import com.example.delivery.network.StatusUpdateResponseV2
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.Result

class DirectShipmentRepository {

    suspend fun updateTripShipmentStatusV2(tripShipmentLinkId: Int, status: String, driverId: Int? = null): Result<StatusUpdateResponseV2> = withContext(Dispatchers.IO) {
        try {
            // Update TripShipmentLink status
            val updateLinkQuery = """
                UPDATE "TripShipmentLink" SET status = '$status' WHERE id = $tripShipmentLinkId RETURNING "tripId", "shipmentId"
            """.trimIndent()
            val linkResult = JSONObject(DatabaseManager.executeQuery(updateLinkQuery)).optJSONArray("rows")
            
            if (linkResult != null && linkResult.length() > 0) {
                val shipmentId = linkResult.getJSONObject(0).optInt("shipmentId")
                
                // Also update Shipment status
                val updateShipmentQuery = """
                    UPDATE "Shipment" SET status = '$status', "updatedAt" = NOW() WHERE id = $shipmentId
                """.trimIndent()
                DatabaseManager.executeQuery(updateShipmentQuery)
                
                val response = StatusUpdateResponseV2(
                    success = true,
                    message = "Status updated successfully",
                    data = com.example.delivery.network.StatusUpdateDataV2(
                        tripShipmentLink = com.example.delivery.network.TripShipmentLinkUpdate(id = tripShipmentLinkId, status = status, podDone = false, updatedAt = ""),
                        shipment = null,
                        tripAutoCompleted = false,
                        tripId = linkResult.getJSONObject(0).optInt("tripId"),
                        shipmentId = shipmentId
                    )
                )
                Result.success(response)
            } else {
                Result.failure(Exception("TripShipmentLink not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getShipmentDates(driverId: Int): Result<ShipmentDatesResponse> = withContext(Dispatchers.IO) {
        try {
            val query = """
                SELECT DISTINCT "tripDate"::date as "tripDate"
                FROM "Trip"
                WHERE "driverId" = $driverId
            """.trimIndent()
            val json = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            val dates = mutableListOf<String>()
            if (json != null) {
                for (i in 0 until json.length()) {
                    val row = json.getJSONObject(i)
                    dates.add(row.optString("tripDate"))
                }
            }
            val response = ShipmentDatesResponse(
                success = true,
                data = dates,
                message = "Dates fetched"
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchShipment(barcode: String, driverId: Int): Result<com.example.delivery.models.ShipmentSearchResponse> = performSearch(barcode, driverId)

    suspend fun searchByTrackingNumber(trackingNumber: String, driverId: Int): Result<com.example.delivery.models.ShipmentSearchResponse> = performSearch(trackingNumber, driverId)

    suspend fun markAsDelivered(shipmentId: Int, driverId: Int): Result<com.example.delivery.models.ShipmentSearchResponse> = withContext(Dispatchers.IO) {
        try {
            // Update Shipment table status
            val updateQuery = """
                UPDATE "Shipment" SET status = 'DELIVERED', "updatedAt" = NOW() WHERE id = $shipmentId
            """.trimIndent()
            DatabaseManager.executeQuery(updateQuery)
            
            // Also update any TripShipmentLink matching this shipment
            val updateLinkQuery = """
                UPDATE "TripShipmentLink" SET status = 'DELIVERED' WHERE "shipmentId" = $shipmentId
            """.trimIndent()
            DatabaseManager.executeQuery(updateLinkQuery)
            
            performSearch(shipmentId.toString(), driverId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun performSearch(queryInput: String, driverId: Int): Result<com.example.delivery.models.ShipmentSearchResponse> = withContext(Dispatchers.IO) {
        try {
            val escapedInput = queryInput.replace("'", "''")
            val query = """
                SELECT s.id, s."shipmentNo", s."trackingNumber", s.status, s.description, s.quantity, 
                       s."customerId", s.priority, s."plannedStart", s."plannedEnd", s."driverId",
                       l.address as dest_addr, l.city as dest_city, l."postalCode" as dest_zip,
                       c.name as client_name, c.address as client_addr, c.city as client_city, c."postalCode" as client_zip, c.phone as client_phone
                FROM "Shipment" s
                LEFT JOIN "Location" l ON s."destinationId" = l.id
                LEFT JOIN "Client" c ON s."customerId" = c.id
                WHERE (s.id::text = '$escapedInput') OR (s."shipmentNo" = '$escapedInput') OR (s."trackingNumber" = '$escapedInput')
                LIMIT 1
            """.trimIndent()
            
            val rows = JSONObject(DatabaseManager.executeQuery(query)).optJSONArray("rows")
            if (rows == null || rows.length() == 0) {
                return@withContext Result.success(com.example.delivery.models.ShipmentSearchResponse(
                    success = false,
                    data = null,
                    message = "Colis introuvable"
                ))
            }
            
            val row = rows.getJSONObject(0)
            val shipmentId = row.getInt("id")
            val customerId = row.getInt("customerId")
            
            val shipmentDetail = com.example.delivery.models.ShipmentSearchDetail(
                id = shipmentId,
                shipmentNo = row.optString("shipmentNo", ""),
                trackingNumber = if (row.isNull("trackingNumber")) null else row.optString("trackingNumber", null),
                status = row.optString("status", "TO_PLAN"),
                description = row.optString("description", ""),
                quantity = row.optInt("quantity", 1),
                deliveryAddress = if (row.isNull("dest_addr")) null else row.optString("dest_addr", null),
                deliveryCity = if (row.isNull("dest_city")) null else row.optString("dest_city", null),
                deliveryZipCode = if (row.isNull("dest_zip")) null else row.optString("dest_zip", null),
                customerId = customerId,
                priority = row.optString("priority", "MEDIUM"),
                plannedStart = if (row.isNull("plannedStart")) null else row.optString("plannedStart", null),
                plannedEnd = if (row.isNull("plannedEnd")) null else row.optString("plannedEnd", null)
            )
            
            val clientInfo = com.example.delivery.models.ClientInfo(
                id = customerId,
                name = row.optString("client_name", ""),
                address = row.optString("client_addr", ""),
                city = row.optString("client_city", ""),
                postalCode = row.optString("client_zip", ""),
                phone = if (row.isNull("client_phone")) null else row.optString("client_phone", null)
            )
            
            var belongsToCurrentTour = false
            var tourSequence: Int? = null
            
            val linkQuery = """
                SELECT tsl.sequence 
                FROM "TripShipmentLink" tsl
                JOIN "Trip" t ON tsl."tripId" = t.id
                WHERE tsl."shipmentId" = $shipmentId AND t."driverId" = $driverId AND t.status != 'COMPLETED'
                LIMIT 1
            """.trimIndent()
            
            val linkRows = JSONObject(DatabaseManager.executeQuery(linkQuery)).optJSONArray("rows")
            if (linkRows != null && linkRows.length() > 0) {
                belongsToCurrentTour = true
                tourSequence = linkRows.getJSONObject(0).optInt("sequence", 1)
            } else {
                val shipmentDriverId = row.optInt("driverId", 0)
                belongsToCurrentTour = (shipmentDriverId == driverId)
            }
            
            val searchData = com.example.delivery.models.ShipmentSearchData(
                shipment = shipmentDetail,
                belongsToCurrentTour = belongsToCurrentTour,
                tourSequence = tourSequence,
                client = clientInfo
            )
            
            Result.success(com.example.delivery.models.ShipmentSearchResponse(
                success = true,
                data = searchData,
                message = "Recherche réussie"
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
