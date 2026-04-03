const express = require('express');
const router = express.Router();
const pool = require('../config/database');

/**
 * Valid status values for TripShipmentLink
 */
const VALID_TSL_STATUSES = ['ASSIGNED', 'NON_DEMARRE', 'EN_COURS', 'LIVRE', 'TERMINE'];

/**
 * Valid status values for Shipment
 */
const VALID_SHIPMENT_STATUSES = ['TO_PLAN', 'EXPEDITION', 'DELIVERED'];

/**
 * Map TripShipmentLink status to Shipment status
 */
function mapTslToShipmentStatus(tslStatus) {
    switch (tslStatus) {
        case 'EN_COURS':
            return 'EXPEDITION';
        case 'LIVRE':
        case 'TERMINE':
            return 'DELIVERED';
        default:
            return null; // No update needed for other statuses
    }
}

/**
 * PUT /api/delivery-tracking/trip-shipment/:tripShipmentLinkId/status
 * Update TripShipmentLink status and optionally Shipment status
 * Also checks and auto-completes trip if all deliveries are TERMINE
 */
router.put('/trip-shipment/:tripShipmentLinkId/status', async (req, res) => {
    const client = await pool.connect();
    
    try {
        const { tripShipmentLinkId } = req.params;
        const { status, updateShipment = true, driverId } = req.body;
        
        // Validate required fields
        if (!status) {
            return res.status(400).json({
                success: false,
                message: 'status is required'
            });
        }
        
        // Validate status value
        if (!VALID_TSL_STATUSES.includes(status)) {
            return res.status(400).json({
                success: false,
                message: `Invalid status. Allowed values: ${VALID_TSL_STATUSES.join(', ')}`
            });
        }
        
        await client.query('BEGIN');
        
        console.log(`🔄 Backend: Mise à jour statut TripShipmentLink ${tripShipmentLinkId} -> ${status}`);
        
        // Get current TripShipmentLink info (including tripId and shipmentId)
        const tslQuery = `
            SELECT id, "tripId", "shipmentId", status, "podDone"
            FROM "TripShipmentLink"
            WHERE id = $1
        `;
        const tslResult = await client.query(tslQuery, [tripShipmentLinkId]);
        
        if (tslResult.rows.length === 0) {
            await client.query('ROLLBACK');
            return res.status(404).json({
                success: false,
                message: 'TripShipmentLink not found'
            });
        }
        
        const tripShipmentLink = tslResult.rows[0];
        const tripId = tripShipmentLink.tripId;
        const shipmentId = tripShipmentLink.shipmentId;
        
        // Update TripShipmentLink status
        const updateTslQuery = `
            UPDATE "TripShipmentLink"
            SET status = $1, "updatedAt" = CURRENT_TIMESTAMP
            WHERE id = $2
            RETURNING id, status, "podDone", "updatedAt"
        `;
        const updatedTsl = await client.query(updateTslQuery, [status, tripShipmentLinkId]);
        
        let updatedShipment = null;
        
        // Update Shipment status if applicable and requested
        if (updateShipment) {
            const shipmentStatus = mapTslToShipmentStatus(status);
            if (shipmentStatus && VALID_SHIPMENT_STATUSES.includes(shipmentStatus)) {
                console.log(`🔄 Backend: Mise à jour statut Shipment ${shipmentId} -> ${shipmentStatus}`);
                
                const updateShipmentQuery = `
                    UPDATE "Shipment"
                    SET status = $1, "updatedAt" = CURRENT_TIMESTAMP
                    WHERE id = $2
                    RETURNING id, status, "updatedAt"
                `;
                const shipmentResult = await client.query(updateShipmentQuery, [shipmentStatus, shipmentId]);
                
                if (shipmentResult.rows.length > 0) {
                    updatedShipment = shipmentResult.rows[0];
                    console.log(`✅ Backend: Shipment ${shipmentId} mis à jour -> ${shipmentStatus}`);
                }
            }
        }
        
        // Check for trip auto-completion
        let tripAutoCompleted = false;
        if (status === 'TERMINE') {
            console.log(`🔄 Backend: Vérification auto-complétion trip ${tripId}`);
            
            // Count non-TERMINE deliveries in trip
            const countQuery = `
                SELECT COUNT(*) as count
                FROM "TripShipmentLink"
                WHERE "tripId" = $1 AND status != 'TERMINE'
            `;
            const countResult = await client.query(countQuery, [tripId]);
            const incompleteCount = parseInt(countResult.rows[0].count);
            
            console.log(`ℹ️ Backend: ${incompleteCount} livraison(s) non terminée(s) dans trip ${tripId}`);
            
            if (incompleteCount === 0) {
                // All deliveries are TERMINE, auto-complete the trip
                console.log(`✅ Backend: Auto-complétion du trip ${tripId}`);
                
                const completeTripQuery = `
                    UPDATE "Trip"
                    SET status = 'COMPLETED', "actualEnd" = CURRENT_TIMESTAMP, "updatedAt" = CURRENT_TIMESTAMP
                    WHERE id = $1 AND status != 'COMPLETED'
                    RETURNING id, status, "actualEnd"
                `;
                const tripResult = await client.query(completeTripQuery, [tripId]);
                
                if (tripResult.rows.length > 0) {
                    tripAutoCompleted = true;
                    console.log(`✅ Backend: Trip ${tripId} auto-complété`);
                }
            }
        }
        
        await client.query('COMMIT');
        
        res.json({
            success: true,
            message: 'Status updated successfully',
            data: {
                tripShipmentLink: updatedTsl.rows[0],
                shipment: updatedShipment,
                tripAutoCompleted: tripAutoCompleted,
                tripId: tripId,
                shipmentId: shipmentId
            }
        });
        
    } catch (error) {
        await client.query('ROLLBACK');
        console.error('❌ Backend: Error updating status:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error',
            error: error.message
        });
    } finally {
        client.release();
    }
});

/**
 * GET /api/delivery-tracking/trip/:tripId/completion-status
 * Check if all deliveries in a trip are TERMINE
 */
router.get('/trip/:tripId/completion-status', async (req, res) => {
    try {
        const { tripId } = req.params;
        
        const query = `
            SELECT 
                COUNT(*) as total,
                COUNT(CASE WHEN status = 'TERMINE' THEN 1 END) as completed,
                COUNT(CASE WHEN status != 'TERMINE' THEN 1 END) as incomplete
            FROM "TripShipmentLink"
            WHERE "tripId" = $1
        `;
        
        const result = await pool.query(query, [tripId]);
        const stats = result.rows[0];
        
        const total = parseInt(stats.total);
        const completed = parseInt(stats.completed);
        const incomplete = parseInt(stats.incomplete);
        const allCompleted = total > 0 && incomplete === 0;
        
        res.json({
            success: true,
            data: {
                tripId: parseInt(tripId),
                total: total,
                completed: completed,
                incomplete: incomplete,
                allCompleted: allCompleted,
                completionPercentage: total > 0 ? Math.round((completed * 100) / total) : 0
            }
        });
        
    } catch (error) {
        console.error('❌ Backend: Error checking completion status:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error',
            error: error.message
        });
    }
});

/**
 * POST /api/delivery-tracking/trip/:tripId/complete
 * Manually complete a trip (if all deliveries are TERMINE)
 */
router.post('/trip/:tripId/complete', async (req, res) => {
    const client = await pool.connect();
    
    try {
        const { tripId } = req.params;
        const { driverId } = req.body;
        
        await client.query('BEGIN');
        
        // Verify all deliveries are TERMINE
        const checkQuery = `
            SELECT COUNT(*) as count
            FROM "TripShipmentLink"
            WHERE "tripId" = $1 AND status != 'TERMINE'
        `;
        const checkResult = await client.query(checkQuery, [tripId]);
        const incompleteCount = parseInt(checkResult.rows[0].count);
        
        if (incompleteCount > 0) {
            await client.query('ROLLBACK');
            return res.status(400).json({
                success: false,
                message: `Cannot complete trip. ${incompleteCount} delivery(s) not finished.`,
                incompleteCount: incompleteCount
            });
        }
        
        // Complete the trip
        const completeQuery = `
            UPDATE "Trip"
            SET status = 'COMPLETED', "actualEnd" = CURRENT_TIMESTAMP, "updatedAt" = CURRENT_TIMESTAMP
            WHERE id = $1
            RETURNING id, status, "actualEnd"
        `;
        const result = await client.query(completeQuery, [tripId]);
        
        if (result.rows.length === 0) {
            await client.query('ROLLBACK');
            return res.status(404).json({
                success: false,
                message: 'Trip not found'
            });
        }
        
        await client.query('COMMIT');
        
        res.json({
            success: true,
            message: 'Trip completed successfully',
            data: {
                trip: result.rows[0]
            }
        });
        
    } catch (error) {
        await client.query('ROLLBACK');
        console.error('❌ Backend: Error completing trip:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error',
            error: error.message
        });
    } finally {
        client.release();
    }
});

/**
 * PUT /api/delivery-tracking/trip-shipment/:tripShipmentLinkId/note
 * Save driver note for a delivery (gate codes, instructions, etc.)
 */
router.put('/trip-shipment/:tripShipmentLinkId/note', async (req, res) => {
    try {
        const { tripShipmentLinkId } = req.params;
        const { note, driverId } = req.body;
        
        if (!note && note !== '') {
            return res.status(400).json({
                success: false,
                message: 'note is required'
            });
        }
        
        console.log(`📝 Backend: Saving driver note for TripShipmentLink ${tripShipmentLinkId}`);
        
        // Update the driverNote in TripShipmentLink
        const updateQuery = `
            UPDATE "TripShipmentLink"
            SET "driverNote" = $1, "updatedAt" = CURRENT_TIMESTAMP
            WHERE id = $2
            RETURNING id, "driverNote", "updatedAt"
        `;
        
        const result = await pool.query(updateQuery, [note, tripShipmentLinkId]);
        
        if (result.rows.length === 0) {
            return res.status(404).json({
                success: false,
                message: 'TripShipmentLink not found'
            });
        }
        
        console.log(`✅ Backend: Driver note saved for TripShipmentLink ${tripShipmentLinkId}`);
        
        res.json({
            success: true,
            message: 'Note saved successfully',
            data: result.rows[0]
        });
        
    } catch (error) {
        console.error('❌ Backend: Error saving driver note:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error',
            error: error.message
        });
    }
});

module.exports = router;
