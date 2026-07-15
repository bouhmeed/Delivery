package com.example.delivery.repository.delivery

import com.example.delivery.database.DatabaseManager
import org.json.JSONObject
import android.util.Log
import kotlin.Result

data class SimpleTripInfo(
    val hasTrip: Boolean,
    val tripId: String? = null,
    val tripNumber: String? = null,
    val shipmentCount: Int = 0,
    val clientNames: List<String> = emptyList(),
    val destinationCities: List<String> = emptyList()
)

class SimpleTodayTripRepository {
    
    private val TAG = "SimpleTodayTripRepository"
    
    suspend fun getTodayTripInfo(driverId: Int): Result<SimpleTripInfo> {
        return try {
            Log.d(TAG, "🔍 Getting today's trip info for driver: $driverId")
            
            // Check if there's a trip today - simplified query
            val tripQuery = """
                SELECT t.id, t."tripNumber"
                FROM "Trip" t
                WHERE t."driverId" = $driverId 
                AND DATE(t."tripDate") = CURRENT_DATE
                LIMIT 1
            """.trimIndent()
            
            val tripJson = JSONObject(DatabaseManager.executeQuery(tripQuery))
            val tripRows = tripJson.optJSONArray("rows")
            
            if (tripRows == null || tripRows.length() == 0) {
                Log.d(TAG, "✅ No trip found for today")
                return Result.success(SimpleTripInfo(hasTrip = false))
            }
            
            val tripRow = tripRows.getJSONObject(0)
            val tripId = tripRow.getInt("id")
            val tripNumber = tripRow.getString("tripNumber")
            
            Log.d(TAG, "✅ Trip found: $tripNumber (id: $tripId)")
            
            // Get shipment count only - much simpler query
            val countQuery = """
                SELECT COUNT(DISTINCT s.id) as "shipmentCount"
                FROM "TripShipmentLink" tsl
                JOIN "Shipment" s ON tsl."shipmentId" = s.id
                WHERE tsl."tripId" = $tripId
            """.trimIndent()
            
            val countJson = JSONObject(DatabaseManager.executeQuery(countQuery))
            val countRow = countJson.optJSONArray("rows")?.optJSONObject(0)
            val totalCount = countRow?.optInt("shipmentCount", 0) ?: 0
            
            Log.d(TAG, "✅ Shipment count: $totalCount")
            
            // Get client names - simplified approach
            val clientQuery = """
                SELECT DISTINCT c.name
                FROM "TripShipmentLink" tsl
                JOIN "Shipment" s ON tsl."shipmentId" = s.id
                LEFT JOIN "Client" c ON s."clientId" = c.id
                WHERE tsl."tripId" = $tripId
                LIMIT 5
            """.trimIndent()
            
            val clientJson = JSONObject(DatabaseManager.executeQuery(clientQuery))
            val clientRows = clientJson.optJSONArray("rows")
            val clientNames = mutableListOf<String>()
            
            Log.d(TAG, "🔍 Client query result rows: ${clientRows?.length() ?: 0}")
            if (clientRows != null) {
                for (i in 0 until clientRows.length()) {
                    val row = clientRows.getJSONObject(i)
                    val name = row.optString("name", "")
                    Log.d(TAG, "🔍 Client $i: name='$name'")
                    if (name.isNotEmpty() && name != "null") {
                        clientNames.add(name)
                    }
                }
            }
            
            // If still no clients, add a default
            if (clientNames.isEmpty()) {
                clientNames.add("Client inconnu")
            }
            
            Log.d(TAG, "✅ Clients: ${clientNames.size} - names: $clientNames")
            
            // Get destination cities - simplified approach
            val cityQuery = """
                SELECT DISTINCT a.city
                FROM "TripShipmentLink" tsl
                JOIN "Shipment" s ON tsl."shipmentId" = s.id
                LEFT JOIN "ShipmentAddress" sa ON s.id = sa."shipmentId" AND sa.type = 'DELIVERY'
                LEFT JOIN "Address" a ON sa."addressId" = a.id
                WHERE tsl."tripId" = $tripId
                LIMIT 5
            """.trimIndent()
            
            val cityJson = JSONObject(DatabaseManager.executeQuery(cityQuery))
            val cityRows = cityJson.optJSONArray("rows")
            val destinationCities = mutableListOf<String>()
            
            Log.d(TAG, "🔍 City query result rows: ${cityRows?.length() ?: 0}")
            if (cityRows != null) {
                for (i in 0 until cityRows.length()) {
                    val row = cityRows.getJSONObject(i)
                    val city = row.optString("city", "")
                    Log.d(TAG, "🔍 City $i: city='$city'")
                    if (city.isNotEmpty() && city != "null") {
                        destinationCities.add(city)
                    }
                }
            }
            
            // If still no cities, add a default
            if (destinationCities.isEmpty()) {
                destinationCities.add("Ville inconnue")
            }
            
            Log.d(TAG, "✅ Cities: ${destinationCities.size} - cities: $destinationCities")
            
            val result = SimpleTripInfo(
                hasTrip = true,
                tripId = tripId.toString(),
                tripNumber = tripNumber,
                shipmentCount = totalCount,
                clientNames = clientNames,
                destinationCities = destinationCities
            )
            
            Log.d(TAG, "✅ Created SimpleTripInfo: hasTrip=${result.hasTrip}, tripNumber=${result.tripNumber}, shipments=${result.shipmentCount}")
            Log.d(TAG, "✅ Returning success")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting today's trip info", e)
            Result.failure(e)
        }
    }
}
