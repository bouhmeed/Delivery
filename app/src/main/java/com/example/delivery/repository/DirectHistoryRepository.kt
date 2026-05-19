package com.example.delivery.repository

import com.example.delivery.repository.Result

import com.example.delivery.database.DatabaseManager
import com.example.delivery.models.*
import com.example.delivery.models.delivery.*
import com.example.delivery.models.driver.*
import com.example.delivery.models.user.*
import com.example.delivery.models.vehicle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.ArrayList

class DirectHistoryRepository {

    suspend fun getDriverHistory(driverId: Int, limit: Int = 50, offset: Int = 0): Result<TripHistory> = withContext(Dispatchers.IO) {
        try {
            // Main query to get delivery history with all required joins
            val historyQuery = """
                SELECT 
                    t.id as trip_id,
                    t."tripId" as trip_number,
                    t."tripDate" as trip_date,
                    t.status as trip_status,
                    s.id as shipment_id,
                    s."shipmentNo" as shipment_number,
                    s.status as shipment_status,
                    SUBSTRING(s.description, 1, 200) as shipment_description,
                    s.quantity,
                    s.uom,
                    c.name as client_name,
                    c.address as client_address,
                    c.city as client_city,
                    c."postalCode" as client_postal_code,
                    origin_loc.name as origin_name,
                    origin_loc.city as origin_city,
                    origin_loc.address as origin_address,
                    dest_loc.name as destination_name,
                    dest_loc.city as destination_city,
                    dest_loc.address as destination_address,
                    v.name as vehicle_name,
                    v.registration as vehicle_registration,
                    v.type as vehicle_type,
                    tsl.status as link_status,
                    tsl."podDone" as pod_done,
                    tsl.sequence,
                    d.name as driver_name
                FROM "Trip" t
                JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
                JOIN "Shipment" s ON tsl."shipmentId" = s.id
                LEFT JOIN "Client" c ON s."customerId" = c.id
                LEFT JOIN "Location" origin_loc ON s."originId" = origin_loc.id
                LEFT JOIN "Location" dest_loc ON s."destinationId" = dest_loc.id
                LEFT JOIN "Vehicle" v ON t."vehicleId" = v.id
                LEFT JOIN "Driver" d ON t."driverId" = d.id
                WHERE t."driverId" = ${driverId}
                  AND t."tripDate" <= CURRENT_DATE
                ORDER BY t."tripDate" DESC, tsl.sequence ASC
                LIMIT ${limit} OFFSET ${offset}
            """.trimIndent()

            val rowsArray = JSONObject(DatabaseManager.executeQuery(historyQuery)).optJSONArray("rows")
            val historyList = ArrayList<DeliveryHistoryItem>()
            
            if (rowsArray != null) {
                for (i in 0 until rowsArray.length()) {
                    val row = rowsArray.getJSONObject(i)
                    historyList.add(
                        DeliveryHistoryItem(
                            id = row.optLong("trip_id").toString(),
                            tripDate = row.optString("trip_date", ""),
                            tripNumber = getNullableString(row, "trip_number"),
                            tripStatus = row.optString("trip_status", "DRAFT"),
                            shipmentId = row.optLong("shipment_id").toString(),
                            shipmentNumber = getNullableString(row, "shipment_number"),
                            shipmentStatus = row.optString("shipment_status", "TO_PLAN"),
                            shipmentDescription = row.optString("shipment_description", ""),
                            quantity = row.optInt("quantity", 1),
                            uom = row.optString("uom", "PCS"),
                            clientName = getNullableString(row, "client_name"),
                            clientAddress = getNullableString(row, "client_address"),
                            clientCity = getNullableString(row, "client_city"),
                            clientPostalCode = getNullableString(row, "client_postal_code"),
                            originName = getNullableString(row, "origin_name"),
                            originCity = getNullableString(row, "origin_city"),
                            originAddress = getNullableString(row, "origin_address"),
                            destinationName = getNullableString(row, "destination_name"),
                            destinationCity = getNullableString(row, "destination_city"),
                            destinationAddress = getNullableString(row, "destination_address"),
                            vehicleName = getNullableString(row, "vehicle_name"),
                            vehicleRegistration = getNullableString(row, "vehicle_registration"),
                            vehicleType = getNullableString(row, "vehicle_type"),
                            linkStatus = getNullableString(row, "link_status"),
                            podDone = if (row.isNull("pod_done")) false else row.optBoolean("pod_done"),
                            sequence = if (row.isNull("sequence")) null else row.optInt("sequence"),
                            driverName = getNullableString(row, "driver_name")
                        )
                    )
                }
            }

            // Get total count for pagination
            val countQuery = """
                SELECT COUNT(*) as total
                FROM "Trip" t
                WHERE t."driverId" = ${driverId}
                  AND t."tripDate" <= CURRENT_DATE
            """.trimIndent()
            
            val countArray = JSONObject(DatabaseManager.executeQuery(countQuery)).optJSONArray("rows")
            val totalCount = if (countArray != null && countArray.length() > 0) {
                countArray.getJSONObject(0).optInt("total", 0)
            } else {
                0
            }

            val pagination = PaginationInfo(
                currentPage = (offset / limit) + 1,
                totalPages = Math.max(1, Math.ceil(totalCount.toDouble() / limit).toInt()),
                totalItems = totalCount,
                itemsPerPage = limit
            )

            Result.success(TripHistory(historyList, pagination))
        } catch (e: Exception) {
            Result.error("Erreur direct db: ${e.message}")
        }
    }

    suspend fun getDriverStats(driverId: Int, periodDays: Int = 30): Result<DriverStats> = withContext(Dispatchers.IO) {
        try {
            // Get driver statistics
            val statsQuery = """
                SELECT 
                    COUNT(DISTINCT t.id) as total_trips,
                    COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) as completed_trips,
                    COUNT(DISTINCT s.id) as total_shipments,
                    COUNT(DISTINCT CASE WHEN s.status = 'DELIVERED' THEN s.id END) as delivered_shipments,
                    COUNT(DISTINCT CASE WHEN s.status = 'TO_PLAN' THEN s.id END) as pending_shipments,
                    COUNT(DISTINCT CASE WHEN s.status = 'EXPEDITION' THEN s.id END) as expedition_shipments,
                    COALESCE(SUM(s.quantity), 0) as total_quantity,
                    COALESCE(SUM(s.weight), 0) as total_weight,
                    COALESCE(AVG(s.weight), 0) as avg_weight,
                    MAX(t."tripDate") as last_trip_date,
                    MIN(t."tripDate") as first_trip_date
                FROM "Trip" t
                LEFT JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
                LEFT JOIN "Shipment" s ON tsl."shipmentId" = s.id
                WHERE t."driverId" = ${driverId} 
                  AND t."tripDate" >= CURRENT_DATE - INTERVAL '${periodDays} days'
            """ .trimIndent()

            val statsArray = JSONObject(DatabaseManager.executeQuery(statsQuery)).optJSONArray("rows")
            val statsRow = if (statsArray != null && statsArray.length() > 0) statsArray.getJSONObject(0) else null

            val driverStatsInfo = if (statsRow != null) {
                val totalTrips = statsRow.optInt("total_trips", 0)
                val completedTrips = statsRow.optInt("completed_trips", 0)
                DriverStatsInfo(
                    driverId = driverId.toString(),
                    totalTrips = totalTrips,
                    completedTrips = completedTrips,
                    deliveredShipments = statsRow.optInt("delivered_shipments", 0),
                    totalShipments = statsRow.optInt("total_shipments", 0),
                    pendingShipments = statsRow.optInt("pending_shipments", 0),
                    expeditionShipments = statsRow.optInt("expedition_shipments", 0),
                    totalQuantity = statsRow.optDouble("total_quantity", 0.0),
                    totalWeight = statsRow.optDouble("total_weight", 0.0),
                    averageWeight = statsRow.optDouble("avg_weight", 0.0),
                    lastTripDate = getNullableString(statsRow, "last_trip_date"),
                    firstTripDate = getNullableString(statsRow, "first_trip_date"),
                    successRate = if (totalTrips > 0) Math.round((completedTrips.toDouble() / totalTrips) * 100).toInt() else 0
                )
            } else {
                DriverStatsInfo(
                    driverId = driverId.toString(),
                    totalTrips = 0,
                    completedTrips = 0,
                    deliveredShipments = 0,
                    totalShipments = 0,
                    pendingShipments = 0,
                    expeditionShipments = 0,
                    totalQuantity = 0.0,
                    totalWeight = 0.0,
                    averageWeight = 0.0,
                    lastTripDate = null,
                    firstTripDate = null,
                    successRate = 0
                )
            }

            // Get monthly trends for the last 6 months
            val trendsQuery = """
                SELECT 
                    TO_CHAR(t."tripDate", 'YYYY-MM') as month,
                    COUNT(DISTINCT t.id) as trips,
                    COUNT(DISTINCT s.id) as deliveries,
                    COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) as completed_trips,
                    COUNT(DISTINCT CASE WHEN s.status = 'DELIVERED' THEN s.id END) as delivered_shipments,
                    COALESCE(SUM(s.quantity), 0) as total_quantity,
                    CASE 
                      WHEN COUNT(DISTINCT s.id) > 0 
                      THEN ROUND((COUNT(DISTINCT CASE WHEN s.status = 'DELIVERED' THEN s.id END) * 100.0 / COUNT(DISTINCT s.id)), 2)
                      ELSE 0 
                    END as success_rate
                FROM "Trip" t
                LEFT JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
                LEFT JOIN "Shipment" s ON tsl."shipmentId" = s.id
                WHERE t."driverId" = ${driverId} 
                  AND t."tripDate" >= CURRENT_DATE - INTERVAL '6 months'
                GROUP BY TO_CHAR(t."tripDate", 'YYYY-MM')
                ORDER BY month DESC
            """.trimIndent()

            val trendsArray = JSONObject(DatabaseManager.executeQuery(trendsQuery)).optJSONArray("rows")
            val monthlyTrends = ArrayList<MonthlyTrend>()

            if (trendsArray != null) {
                for (i in 0 until trendsArray.length()) {
                    val row = trendsArray.getJSONObject(i)
                    monthlyTrends.add(
                        MonthlyTrend(
                            month = formatMonth(row.optString("month", "")),
                            trips = row.optInt("trips", 0),
                            deliveries = row.optInt("deliveries", 0),
                            completedTrips = row.optInt("completed_trips", 0),
                            deliveredShipments = row.optInt("delivered_shipments", 0),
                            totalQuantity = row.optDouble("total_quantity", 0.0),
                            successRate = row.optDouble("success_rate", 0.0)
                        )
                    )
                }
            }

            Result.success(DriverStats(driverStatsInfo, monthlyTrends))
        } catch (e: Exception) {
            Result.error("Erreur direct db: ${e.message}")
        }
    }

    private fun getNullableString(json: JSONObject, key: String): String? {
        if (json.isNull(key)) return null
        val value = json.optString(key, null as String?)
        return if (value == "null") null else value
    }

    private fun formatMonth(monthString: String): String {
        if (monthString.isEmpty() || !monthString.contains("-")) return monthString
        val parts = monthString.split("-")
        if (parts.size < 2) return monthString
        val year = parts[0]
        val month = parts[1]
        val monthNames = mapOf(
            "01" to "Janvier", "02" to "Février", "03" to "Mars", "04" to "Avril",
            "05" to "Mai", "06" to "Juin", "07" to "Juillet", "08" to "Août",
            "09" to "Septembre", "10" to "Octobre", "11" to "Novembre", "12" to "Décembre"
        )
        return "${monthNames[month] ?: month} $year"
    } 
}
