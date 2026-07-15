package com.example.delivery.repository.delivery

import com.example.delivery.repository.Result
import com.example.delivery.models.delivery.ShipmentReturn
import com.example.delivery.models.delivery.ShipmentReturnDefect

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
                val updateProofQuery = if (imageData != null) {
                    """
                        UPDATE "ShipmentProof"
                        SET "signatureUrl" = '$escapedSignature', "imageUrl" = '$escapedImage', "createdAt" = CURRENT_TIMESTAMP
                        WHERE id = $proofId
                    """.trimIndent()
                } else {
                    """
                        UPDATE "ShipmentProof"
                        SET "signatureUrl" = '$escapedSignature', "createdAt" = CURRENT_TIMESTAMP
                        WHERE id = $proofId
                    """.trimIndent()
                }
                DatabaseManager.executeQuery(updateProofQuery)
            } else {
                // 2b. Insert new proof
                val insertProofQuery = """
                    INSERT INTO "ShipmentProof" ("shipmentId", "imageUrl", "signatureUrl", "createdAt")
                    VALUES ($shipmentId, '$escapedImage', '$escapedSignature', CURRENT_TIMESTAMP)
                """.trimIndent()
                DatabaseManager.executeQuery(insertProofQuery)
            }

            // 3. Update Shipment status to LIVRE
            val updateShipmentQuery = """
                UPDATE "Shipment"
                SET status = 'LIVRE', "updatedAt" = CURRENT_TIMESTAMP
                WHERE id = $shipmentId
            """.trimIndent()
            DatabaseManager.executeQuery(updateShipmentQuery)

            // 4. Update TripShipmentLink status
            val updateTripLinkQuery = """
                UPDATE "TripShipmentLink"
                SET status = 'TERMINE', "podDone" = true, "updatedAt" = CURRENT_TIMESTAMP
                WHERE "shipmentId" = $shipmentId
            """.trimIndent()
            DatabaseManager.executeQuery(updateTripLinkQuery)

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.error(e.message ?: "Unknown error")
        }
    }

    suspend fun saveReturnsDirect(
        shipmentId: Int,
        photoBase64: String?,
        palettes: Int,
        caisses: Int,
        bouteilles: Int,
        futs: Int,
        autre: Int,
        comment: String,
        packagesRecovered: Boolean,
        packagingRecovered: Boolean
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val escapedImage = (photoBase64 ?: "").replace("'", "''")
            val escapedComment = comment.replace("'", "''")

            // 1. Fetch tripShipmentLinkId if available
            val tripLinkQuery = """
                SELECT id FROM "TripShipmentLink" WHERE "shipmentId" = $shipmentId LIMIT 1
            """.trimIndent()
            val tripLinkResult = DatabaseManager.executeQuery(tripLinkQuery)
            val tripLinkRows = JSONObject(tripLinkResult).optJSONArray("rows")
            val tripShipmentLinkId = if (tripLinkRows != null && tripLinkRows.length() > 0) {
                tripLinkRows.getJSONObject(0).optInt("id")
            } else {
                null
            }

            // 2. Check if return already exists for this shipment
            val checkReturnQuery = """
                SELECT id FROM "ShipmentReturns" WHERE "shipmentId" = $shipmentId LIMIT 1
            """.trimIndent()
            val checkReturnResult = DatabaseManager.executeQuery(checkReturnQuery)
            val checkReturnRows = JSONObject(checkReturnResult).optJSONArray("rows")
            val existingReturnId = if (checkReturnRows != null && checkReturnRows.length() > 0) {
                checkReturnRows.getJSONObject(0).optInt("id")
            } else {
                null
            }

            val shipmentReturnId = if (existingReturnId != null) {
                // Update existing return
                val updateReturnQuery = """
                    UPDATE "ShipmentReturns"
                    SET
                        "tripShipmentLinkId" = ${tripShipmentLinkId ?: "NULL"},
                        "returnStatus" = 'PENDING',
                        "returnDate" = CURRENT_TIMESTAMP,
                        "updatedAt" = CURRENT_TIMESTAMP,
                        "packagesrecovered" = ${if (packagesRecovered) 1 else 0},
                        "packagingrecovered" = ${if (packagingRecovered) 1 else 0},
                        "palettes" = $palettes,
                        "caisses" = $caisses,
                        "bouteilles" = $bouteilles,
                        "futs" = $futs,
                        "autre" = $autre,
                        "comment" = '$escapedComment',
                        "proofimageurl" = '$escapedImage'
                    WHERE id = $existingReturnId
                    RETURNING id
                """.trimIndent()
                
                val updateResult = DatabaseManager.executeQuery(updateReturnQuery)
                val updateRows = JSONObject(updateResult).optJSONArray("rows")
                
                if (updateRows == null || updateRows.length() == 0) {
                    return@withContext Result.error("Failed to update shipment return")
                }
                
                updateRows.getJSONObject(0).getInt("id").also {
                    println("✅ ShipmentReturn updated with ID: $it")
                }
            } else {
                // Insert new return
                val insertReturnQuery = """
                    INSERT INTO "ShipmentReturns" (
                        "shipmentId",
                        "tenantId",
                        "tripShipmentLinkId",
                        "returnStatus",
                        "returnReason",
                        "returnDate",
                        "createdAt",
                        "updatedAt",
                        "packagesrecovered",
                        "packagingrecovered",
                        "palettes",
                        "caisses",
                        "bouteilles",
                        "futs",
                        "autre",
                        "comment",
                        "proofimageurl"
                    ) VALUES (
                        $shipmentId,
                        1,
                        ${tripShipmentLinkId ?: "NULL"},
                        'PENDING',
                        NULL,
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP,
                        ${if (packagesRecovered) 1 else 0},
                        ${if (packagingRecovered) 1 else 0},
                        $palettes,
                        $caisses,
                        $bouteilles,
                        $futs,
                        $autre,
                        '$escapedComment',
                        '$escapedImage'
                    ) RETURNING id
                """.trimIndent()
                
                val returnResult = DatabaseManager.executeQuery(insertReturnQuery)
                val returnRows = JSONObject(returnResult).optJSONArray("rows")
                
                if (returnRows == null || returnRows.length() == 0) {
                    return@withContext Result.error("Failed to create shipment return")
                }
                
                returnRows.getJSONObject(0).getInt("id").also {
                    println("✅ ShipmentReturn created with ID: $it")
                }
            }

            // 2. Update TripShipmentLink to set returnsDone = true
            val updateTripLinkQuery = """
                UPDATE "TripShipmentLink"
                SET "returnsDone" = true, "updatedAt" = CURRENT_TIMESTAMP
                WHERE "shipmentId" = $shipmentId
            """.trimIndent()
            DatabaseManager.executeQuery(updateTripLinkQuery)

            Result.success(shipmentReturnId)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.error(e.message ?: "Unknown error")
        }
    }

    suspend fun saveReturnDefects(
        shipmentReturnId: Int,
        defects: List<com.example.delivery.models.delivery.ItemDefect>
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            for (defect in defects) {
                val escapedReason = defect.reason.replace("'", "''")
                val escapedDescription = defect.articleName.replace("'", "''")

                val insertDefectQuery = """
                    INSERT INTO "ShipmentReturnDefects" (
                        "shipmentReturnId",
                        "itemId",
                        "defectType",
                        "defectDescription",
                        "quantity",
                        "reason"
                    ) VALUES (
                        $shipmentReturnId,
                        ${defect.itemId},
                        'OTHER',
                        '$escapedDescription',
                        ${defect.quantity},
                        '$escapedReason'
                    )
                """.trimIndent()

                DatabaseManager.executeQuery(insertDefectQuery)
                println("✅ Defect inserted: Item ${defect.itemId} x${defect.quantity} - ${defect.reason}")
            }

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.error(e.message ?: "Unknown error")
        }
    }
}
