const express = require('express');
const router = express.Router();
const { Pool } = require('pg');

// Database connection
const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
});

// POST /api/delivery-validation/proof - Save delivery proof with signature
router.post('/proof', async (req, res) => {
    const client = await pool.connect();
    try {
        const { shipmentId, imageUrl, signatureUrl } = req.body;

        if (!shipmentId || !imageUrl) {
            return res.status(400).json({
                success: false,
                message: 'shipmentId and imageUrl are required'
            });
        }

        // Check if shipment exists
        const shipmentQuery = 'SELECT id FROM "Shipment" WHERE id = $1';
        const shipmentResult = await client.query(shipmentQuery, [shipmentId]);

        if (shipmentResult.rows.length === 0) {
            return res.status(404).json({
                success: false,
                message: 'Shipment not found'
            });
        }

        // Insert or update delivery proof
        const upsertQuery = `
            INSERT INTO "ShipmentProof" ("shipmentId", "imageUrl", "signatureUrl", "createdAt")
            VALUES ($1, $2, $3, CURRENT_TIMESTAMP)
            ON CONFLICT ("shipmentId") 
            DO UPDATE SET 
                "imageUrl" = EXCLUDED."imageUrl",
                "signatureUrl" = EXCLUDED."signatureUrl",
                "createdAt" = CURRENT_TIMESTAMP
            RETURNING id, "shipmentId", "imageUrl", "signatureUrl", "createdAt"
        `;

        const result = await client.query(upsertQuery, [shipmentId, imageUrl, signatureUrl]);

        // Update shipment status to DELIVERED if signature is provided
        if (signatureUrl) {
            const updateShipmentQuery = `
                UPDATE "Shipment" 
                SET status = 'DELIVERED', "updatedAt" = CURRENT_TIMESTAMP
                WHERE id = $1
            `;
            await client.query(updateShipmentQuery, [shipmentId]);

            // Update TripShipmentLink status
            const updateTripLinkQuery = `
                UPDATE "TripShipmentLink" 
                SET status = 'LIVRE', "podDone" = true, "updatedAt" = CURRENT_TIMESTAMP
                WHERE "shipmentId" = $1
            `;
            await client.query(updateTripLinkQuery, [shipmentId]);
        }

        res.json({
            success: true,
            message: 'Delivery proof saved successfully',
            data: result.rows[0]
        });

    } catch (error) {
        console.error('Error saving delivery proof:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error',
            error: error.message
        });
    } finally {
        client.release();
    }
});

// GET /api/delivery-validation/proof/:shipmentId - Get delivery proof for a shipment
router.get('/proof/:shipmentId', async (req, res) => {
    const client = await pool.connect();
    try {
        const { shipmentId } = req.params;

        const query = `
            SELECT sp.id, sp."shipmentId", sp."imageUrl", sp."signatureUrl", sp."createdAt",
                   s.shipmentNo, s.status as shipmentStatus
            FROM "ShipmentProof" sp
            LEFT JOIN "Shipment" s ON sp."shipmentId" = s.id
            WHERE sp."shipmentId" = $1
        `;

        const result = await client.query(query, [shipmentId]);

        if (result.rows.length === 0) {
            return res.status(404).json({
                success: false,
                message: 'Delivery proof not found'
            });
        }

        res.json({
            success: true,
            data: result.rows[0]
        });

    } catch (error) {
        console.error('Error fetching delivery proof:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error',
            error: error.message
        });
    } finally {
        client.release();
    }
});

// POST /api/delivery-validation/validate - Complete delivery validation
router.post('/validate', async (req, res) => {
    const client = await pool.connect();
    console.log('📱 DEBUG: Requête de validation reçue:', {
        body: req.body,
        headers: req.headers,
        timestamp: new Date().toISOString()
    });
    
    try {
        const { shipmentId, signatureData, imageData, notes } = req.body;

        if (!shipmentId || !signatureData) {
            return res.status(400).json({
                success: false,
                message: 'shipmentId and signatureData are required'
            });
        }

        await client.query('BEGIN');

        // Save signature and image data (assuming base64 encoded)
        const imageUrl = imageData || `data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwA/8A8A`;

        // Insert delivery proof
        const insertProofQuery = `
            INSERT INTO "ShipmentProof" ("shipmentId", "imageUrl", "signatureUrl", "createdAt")
            VALUES ($1, $2, $3, CURRENT_TIMESTAMP)
            RETURNING id
        `;

        const proofResult = await client.query(insertProofQuery, [
            shipmentId,
            imageUrl,
            signatureData
        ]);

        // Update shipment status
        const updateShipmentQuery = `
            UPDATE "Shipment" 
            SET status = 'DELIVERED', "updatedAt" = CURRENT_TIMESTAMP
            WHERE id = $1
        `;
        await client.query(updateShipmentQuery, [shipmentId]);

        // Update TripShipmentLink
        const updateTripLinkQuery = `
            UPDATE "TripShipmentLink" 
            SET status = 'LIVRE', "podDone" = true, "updatedAt" = CURRENT_TIMESTAMP
            WHERE "shipmentId" = $1
        `;
        await client.query(updateTripLinkQuery, [shipmentId]);

        await client.query('COMMIT');

        res.json({
            success: true,
            message: 'Delivery validated successfully',
            proofId: proofResult.rows[0].id
        });

    } catch (error) {
        await client.query('ROLLBACK');
        console.error('Error validating delivery:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error',
            error: error.message
        });
    } finally {
        client.release();
    }
});

module.exports = router;
