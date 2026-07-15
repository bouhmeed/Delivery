package com.example.delivery.repository.delivery

import com.example.delivery.models.delivery.TodayTourResponse
import com.example.delivery.models.delivery.TodayTourData
import com.example.delivery.models.delivery.TourInfo
import com.example.delivery.models.delivery.TourStatistics
import com.example.delivery.models.ApiResponse
import com.example.delivery.models.delivery.Shipment
import com.example.delivery.database.DatabaseManager
import org.json.JSONObject
import android.util.Log
import kotlin.Result

class TodayTourRepository {
    
    private val TAG = "TodayTourRepository"
    
    suspend fun getTodayTour(driverId: Int): Result<TodayTourResponse> {
        Log.d(TAG, "🔍 Getting today's tour for driver: $driverId via Neon SQL")
        
        return try {
            // First, check if there are any trips for this driver today
            val checkQuery = """
                SELECT id, "tripNumber", status, "tripDate"
                FROM "Trip" 
                WHERE "driverId" = $driverId 
                AND DATE("tripDate") = CURRENT_DATE
            """.trimIndent()
            
            val checkJson = JSONObject(DatabaseManager.executeQuery(checkQuery))
            val checkRows = checkJson.optJSONArray("rows")
            Log.d(TAG, "🔍 DEBUG: Found ${checkRows?.length() ?: 0} trips for driver $driverId today")
            
            if (checkRows != null && checkRows.length() > 0) {
                for (i in 0 until checkRows.length()) {
                    val row = checkRows.getJSONObject(i)
                    Log.d(TAG, "🔍 DEBUG: Trip $i - id=${row.getInt("id")}, tripNumber=${row.getString("tripNumber")}, status=${row.getString("status")}")
                }
            }
            
            // Single optimized query to get trip and shipments in one go
            // Changed status filter to include more statuses
            val query = """
                SELECT 
                    t.id as "tripIdInt", t."tripNumber", t.status, t."tripDate" as date,
                    t."estimatedDuration", t."estimatedDistance", t.priority,
                    s.id, s."shipmentNo", s.status as "shipmentStatus", s.description, s.quantity, tsl.sequence
                FROM "Trip" t
                LEFT JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
                LEFT JOIN "Shipment" s ON tsl."shipmentId" = s.id
                WHERE t."driverId" = $driverId 
                AND DATE(t."tripDate") = CURRENT_DATE 
                AND t.status IN ('PLANNING', 'IN_PROGRESS', 'ASSIGNED')
                ORDER BY tsl.sequence
            """.trimIndent()
            
            Log.d(TAG, "🔍 DEBUG: Main query: $query")
            
            val jsonResponse = JSONObject(DatabaseManager.executeQuery(query))
            val rows = jsonResponse.optJSONArray("rows")
            Log.d(TAG, "🔍 DEBUG: Main query result count: ${rows?.length() ?: 0}")
            
            if (rows == null || rows.length() == 0) {
                Log.d(TAG, "⚠️ No active tour found for driver $driverId today")
                return Result.success(TodayTourResponse(
                    success = true,
                    data = TodayTourData(hasTour = false, message = "Aucune tournée active trouvée")
                ))
            }
            
            // Parse first row for trip info
            val firstRow = rows.getJSONObject(0)
            val tripIdInt = firstRow.getInt("tripIdInt")
            val tourInfo = TourInfo(
                id = tripIdInt,
                tripId = firstRow.getString("tripNumber"),
                status = firstRow.getString("status"),
                date = firstRow.getString("date")
            )
            
            Log.d(TAG, "✅ Tour found: ${tourInfo.tripId} - ${tourInfo.status}")
            
            val shipments = mutableListOf<Shipment>()
            var total = 0
            var completed = 0
            
            // Parse all rows for shipments
            for (i in 0 until rows.length()) {
                val row = rows.getJSONObject(i)
                val shipmentId = row.optInt("id", 0)
                
                // Skip rows without shipment (LEFT JOIN can produce nulls)
                if (shipmentId > 0) {
                    val status = row.optString("shipmentStatus", "")
                    shipments.add(Shipment(
                        id = shipmentId,
                        shipmentNo = row.optString("shipmentNo", ""),
                        status = status,
                        description = row.optString("description", ""),
                        quantity = row.optInt("quantity", 0),
                        sequence = row.optInt("sequence", i)
                    ))
                    total++
                    if (status == "COMPLETED" || status == "LIVRE") {
                        completed++
                    }
                }
            }
            
            val percentage = if (total > 0) (completed * 100) / total else 0
            val stats = TourStatistics(
                totalShipments = total,
                completedShipments = completed,
                remainingShipments = total - completed,
                completionPercentage = percentage,
                progressBar = "$percentage%"
            )
            
            Log.d(TAG, "✅ Today's tour loaded: $total shipments, $completed completed")
            
            Result.success(TodayTourResponse(
                success = true,
                data = TodayTourData(
                    hasTour = true,
                    tour = tourInfo,
                    statistics = stats,
                    shipments = shipments
                )
            ))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting today tour", e)
            Result.failure(e)
        }
    }
    
    suspend fun completeShipment(shipmentId: Int): Result<ApiResponse<Shipment>> {
        Log.d(TAG, "✅ Completing shipment: $shipmentId via Neon SQL")
        
        return try {
            val query = """
                UPDATE "Shipment" 
                SET status = 'LIVRE', "updatedAt" = NOW() 
                WHERE id = $shipmentId 
                RETURNING id, "shipmentNo", status, description, quantity
            """.trimIndent()
            
            val jsonResponse = JSONObject(DatabaseManager.executeQuery(query))
            val rows = jsonResponse.optJSONArray("rows")
            
            if (rows != null && rows.length() > 0) {
                val sRow = rows.getJSONObject(0)
                val shipment = Shipment(
                    id = sRow.getInt("id"),
                    shipmentNo = sRow.getString("shipmentNo"),
                    status = sRow.getString("status"),
                    description = sRow.optString("description", ""),
                    quantity = sRow.optInt("quantity", 0),
                    sequence = 0
                )
                Result.success(ApiResponse(success = true, data = shipment))
            } else {
                Result.failure(Exception("Shipment not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Complete shipment exception", e)
            Result.failure(e)
        }
    }
}
