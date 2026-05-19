package com.example.delivery.repository

import com.example.delivery.database.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DirectDeliveryValidationRepository {

    suspend fun validateDeliveryDirect(
        shipmentId: Int,
        signatureBase64: String,
        imageData: String?
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val escapedSignature = signatureBase64.replace("'", "''")
            val defaultPlaceholder = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwA/8A8A"
            val escapedImage = (imageData ?: defaultPlaceholder).replace("'", "''")

            // 1. Check if ShipmentProof already exists
            val checkQuery = """
                SELECT id FROM "ShipmentProof" WHERE "shipmentId" = $shipmentId LIMIT 1
            """.trimIndent()
            val checkResult = DatabaseManager.executeQuery(checkQuery)
            val rows = JSONObject(checkResult).optJSONArray("rows")
            val hasProof = rows != null && rows.length() > 0

            if (hasProof) {
                // 2a. Update existing proof
                val proofId = rows.getJSONObject(0).getInt("id")
                val updateProofQuery = """
                    UPDATE "ShipmentProof"
                    SET "signatureUrl" = '$escapedSignature', "imageUrl" = '$escapedImage', "createdAt" = CURRENT_TIMESTAMP
                    WHERE id = $proofId
                """.trimIndent()
                DatabaseManager.executeQuery(updateProofQuery)
            } else {
                // 2b. Insert new proof
                val insertProofQuery = """
                    INSERT INTO "ShipmentProof" ("shipmentId", "imageUrl", "signatureUrl", "createdAt")
                    VALUES ($shipmentId, '$escapedImage', '$escapedSignature', CURRENT_TIMESTAMP)
                """.trimIndent()
                DatabaseManager.executeQuery(insertProofQuery)
            }

            // 3. Update Shipment status to DELIVERED
            val updateShipmentQuery = """
                UPDATE "Shipment"
                SET status = 'DELIVERED', "updatedAt" = CURRENT_TIMESTAMP
                WHERE id = $shipmentId
            """.trimIndent()
            DatabaseManager.executeQuery(updateShipmentQuery)

            // 4. Update TripShipmentLink status
            val updateTripLinkQuery = """
                UPDATE "TripShipmentLink"
                SET status = 'LIVRE', "podDone" = true, "updatedAt" = CURRENT_TIMESTAMP
                WHERE "shipmentId" = $shipmentId
            """.trimIndent()
            DatabaseManager.executeQuery(updateTripLinkQuery)

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.error(e.message ?: "Unknown error")
        }
    }
}
