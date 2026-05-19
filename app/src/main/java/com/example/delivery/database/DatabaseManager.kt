package com.example.delivery.database

import com.example.delivery.repository.Result

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * 🚀 STABLE NEON SQL HTTP API MANAGER
 * 
 * Target: POST /sql
 * Connection: SQL over HTTP (Direct)
 */
object DatabaseManager {
    
    private const val TAG = "NEON_DEBUG"
    
    // 🔗 CONFIGURATION
    private const val NEON_SQL_ENDPOINT = "https://ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/sql"
    private const val NEON_CONNECTION_STRING = "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require"

    // STEP 4: FIX TIMEOUTS
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * STEP 3: MINIMAL WORKING METHOD
     * Executes a SQL query and returns the raw JSON response string.
     */
    suspend fun executeQuery(query: String): String {
        return withContext(Dispatchers.IO) {
            var lastException: IOException? = null
            
            for (retryCount in 0..1) {
                try {
                    return@withContext performHttpRequest(query)
                } catch (e: IOException) {
                    lastException = e
                    if (retryCount == 0) { // First attempt failed, try retry
                        Log.w(TAG, "⚠️ IOException occurred. Retrying in 2 seconds... (1/1)")
                        kotlinx.coroutines.delay(2000)
                    }
                }
            }
            
            Log.e(TAG, "❌ Max retries reached or fatal error: ${lastException?.message}")
            throw lastException ?: IOException("Unknown database error")
        }
    }

    private fun performHttpRequest(query: String): String {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "🚀 Starting Neon SQL Call")
        Log.d(TAG, "🌐 POST $NEON_SQL_ENDPOINT")
        
        // Prepare JSON body
        val jsonBody = JSONObject().apply {
            put("query", query)
        }
        
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        
        // Build Request with mandatory Neon header
        val request = Request.Builder()
            .url(NEON_SQL_ENDPOINT)
            .addHeader("Content-Type", "application/json")
            .addHeader("neon-connection-string", NEON_CONNECTION_STRING)
            .post(requestBody)
            .build()

        // Execute Call
        return client.newCall(request).execute().use { response ->
            val code = response.code
            val body = response.body?.string() ?: ""
            val duration = System.currentTimeMillis() - startTime
            
            Log.d(TAG, "📥 HTTP Code = $code")
            Log.d(TAG, "⏱️ Execution time = ${duration}ms")
            
            if (response.isSuccessful) {
                Log.d(TAG, "✅ SUCCESS")
                body
            } else {
                Log.e(TAG, "❌ FAILED: Error Body = $body")
                throw IOException("HTTP Error $code: $body")
            }
        }
    }

    // --- STEP 5: TESTS ---

    suspend fun testConnection() {
        Log.d(TAG, "🧪 TEST 1: SELECT 1")
        try {
            executeQuery("SELECT 1 as ok")
        } catch (e: Exception) {
            Log.e(TAG, "Test 1 failed")
        }
    }

    suspend fun testCountDrivers() {
        Log.d(TAG, "🧪 TEST 2: SELECT COUNT(*) FROM \"Driver\"")
        try {
            executeQuery("SELECT COUNT(*) as total FROM \"Driver\"")
        } catch (e: Exception) {
            Log.e(TAG, "Test 2 failed")
        }
    }

    suspend fun testListDrivers() {
        Log.d(TAG, "🧪 TEST 3: SELECT id, name FROM \"Driver\" LIMIT 5")
        try {
            executeQuery("SELECT id, name FROM \"Driver\" LIMIT 5")
        } catch (e: Exception) {
            Log.e(TAG, "Test 3 failed")
        }
    }

    suspend fun testFullDriverTable() {
        Log.d(TAG, "🧪 TEST 4: SELECT * FROM \"Driver\"")
        try {
            val result = executeQuery("SELECT * FROM \"Driver\"")
            Log.d(TAG, "📦 FULL DRIVER TABLE RESPONSE:")
            Log.d(TAG, result)
        } catch (e: Exception) {
            Log.e(TAG, "Test 4 failed")
        }
    }

    suspend fun testListShipments() {
        Log.d(TAG, "🧪 TEST 5: SELECT FIRST 5 SHIPMENTS")
        try {
            val query = """
                SELECT 
                    id, 
                    "shipmentNo", 
                    type, 
                    priority, 
                    status, 
                    quantity, 
                    weight, 
                    "deliveryCity", 
                    "deliveryCountry", 
                    "driverId", 
                    "vehicleId" 
                FROM "Shipment" 
                ORDER BY id 
                LIMIT 5
            """.trimIndent()
            
            val result = executeQuery(query)
            Log.d(TAG, "📦 SHIPMENT RESPONSE:")
            Log.d(TAG, result)
        } catch (e: Exception) {
            Log.e(TAG, "Test 5 failed")
        }
    }

    // --- HOME SCREEN DATA METHODS ---

    /**
     * Get user by email
     */
    suspend fun getUserByEmail(email: String): String {
        Log.d(TAG, "👤 getUserByEmail: $email")
        val query = """
            SELECT id, "tenantId", email, "firstName", "lastName", "driverId", role, "isActive", "createdAt", "updatedAt"
            FROM "User" 
            WHERE email = '$email'
        """.trimIndent()
        return executeQuery(query)
    }

    /**
     * Get driver by ID
     */
    suspend fun getDriverById(driverId: Int): String {
        Log.d(TAG, "🚗 getDriverById: $driverId")
        val query = """
            SELECT id, name, "licenseNumber", "licenseExpiry", "employmentType", 
                   "contractHoursWeek", "homeDepotId", "tenantId", status, address,
                   "assignedVehicle", city, country, "createdAt", "dateOfBirth",
                   email, "hireDate", "licenseIssueDate", phone, "postalCode", 
                   salary, "updatedAt"
            FROM "Driver" 
            WHERE id = $driverId
        """.trimIndent()
        return executeQuery(query)
    }

    /**
     * Get vehicle by driver ID (using Vehicle.driverId column)
     */
    suspend fun getVehicleByDriverId(driverId: Int): String {
        Log.d(TAG, "🚚 getVehicleByDriverId: $driverId")
        val query = """
            SELECT v.*
            FROM "Vehicle" v
            WHERE v."driverId" = $driverId
            LIMIT 1
        """.trimIndent()
        return executeQuery(query)
    }

    /**
     * Get vehicle maintenance by vehicle ID
     */
    suspend fun getVehicleMaintenance(vehicleId: Int): String {
        Log.d(TAG, "🔧 getVehicleMaintenance: $vehicleId")
        val query = """
            SELECT *
            FROM "VehicleMaintenance" 
            WHERE "vehicleId" = $vehicleId
            ORDER BY date DESC
        """.trimIndent()
        return executeQuery(query)
    }

    // --- HOME SCREEN TESTS ---

    suspend fun testGetUserByEmail() {
        Log.d(TAG, "🧪 TEST HOME 1: getUserByEmail")
        try {
            val result = getUserByEmail("admin@tms.com")
            Log.d(TAG, "📦 USER RESPONSE:")
            Log.d(TAG, result)
        } catch (e: Exception) {
            Log.e(TAG, "Test Home 1 failed")
        }
    }

    suspend fun testGetDriverById() {
        Log.d(TAG, "🧪 TEST HOME 2: getDriverById")
        try {
            val result = getDriverById(3)
            Log.d(TAG, "📦 DRIVER RESPONSE:")
            Log.d(TAG, result)
        } catch (e: Exception) {
            Log.e(TAG, "Test Home 2 failed")
        }
    }

    suspend fun testGetVehicleByDriverId() {
        Log.d(TAG, "🧪 TEST HOME 3: getVehicleByDriverId")
        try {
            val result = getVehicleByDriverId(3)
            Log.d(TAG, "📦 VEHICLE RESPONSE:")
            Log.d(TAG, result)
        } catch (e: Exception) {
            Log.e(TAG, "Test Home 3 failed")
        }
    }

    suspend fun testGetVehicleMaintenance() {
        Log.d(TAG, "🧪 TEST HOME 4: getVehicleMaintenance")
        try {
            val result = getVehicleMaintenance(3)
            Log.d(TAG, "📦 MAINTENANCE RESPONSE:")
            Log.d(TAG, result)
        } catch (e: Exception) {
            Log.e(TAG, "Test Home 4 failed")
        }
    }
}
