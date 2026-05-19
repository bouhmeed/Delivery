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
            val queryTrip = """
                SELECT id, "tripId", status, "tripDate" as date 
                FROM "Trip" 
                WHERE "driverId" = $driverId 
                AND DATE("tripDate") = CURRENT_DATE 
                AND status != 'COMPLETED' 
                ORDER BY "tripDate" LIMIT 1
            """.trimIndent()
            
            val tripJson = JSONObject(DatabaseManager.executeQuery(queryTrip))
            val tripRows = tripJson.optJSONArray("rows")
            
            if (tripRows == null || tripRows.length() == 0) {
                return Result.success(TodayTourResponse(
                    success = true,
                    data = TodayTourData(hasTour = false, message = "Aucune tournée trouvée")
                ))
            }
            
            val tripRow = tripRows.getJSONObject(0)
            val tripIdInt = tripRow.getInt("id")
            val tourInfo = TourInfo(
                id = tripIdInt,
                tripId = tripRow.getString("tripId"),
                status = tripRow.getString("status"),
                date = tripRow.getString("date")
            )
            
            // Get shipments for this trip
            val queryShipments = """
                SELECT s.id, s."shipmentNo", s.status, s.description, s.quantity, tsl.sequence 
                FROM "Shipment" s 
                JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId" 
                WHERE tsl."tripId" = $tripIdInt
                ORDER BY tsl.sequence
            """.trimIndent()
            
            val shipJson = JSONObject(DatabaseManager.executeQuery(queryShipments))
            val shipRows = shipJson.optJSONArray("rows")
            val shipments = mutableListOf<Shipment>()
            
            var total = 0
            var completed = 0
            
            if (shipRows != null) {
                for (i in 0 until shipRows.length()) {
                    val sRow = shipRows.getJSONObject(i)
                    val status = sRow.getString("status")
                    shipments.add(Shipment(
                        id = sRow.getInt("id"),
                        shipmentNo = sRow.getString("shipmentNo"),
                        status = status,
                        description = sRow.optString("description", ""),
                        quantity = sRow.optInt("quantity", 0),
                        sequence = sRow.optInt("sequence", i)
                    ))
                    total++
                    if (status == "COMPLETED" || status == "DELIVERED") {
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
                SET status = 'DELIVERED', "updatedAt" = NOW() 
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
