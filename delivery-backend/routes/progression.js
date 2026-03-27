const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// GET /api/trips/today?driverId={id} - MUST COME FIRST!
router.get('/today', async (req, res) => {
    const { driverId } = req.query;
    
    if (!driverId) {
        return res.status(400).json({ error: 'driverId is required' });
    }
    
    try {
        const today = new Date().toISOString().split('T')[0];
        
        const query = `
            SELECT * FROM "Trip"
            WHERE "driverId" = $1 
            AND DATE("tripDate") = $2
            AND "status" IN ('PLANNING', 'IN_PROGRESS', 'COMPLETED', 'VALIDATED')
            ORDER BY "tripDate" DESC
            LIMIT 1
        `;
        
        const result = await pool.query(query, [parseInt(driverId), today]);
        
        if (result.rows.length === 0) {
            return res.json(null);
        }
        
        // Convert numeric IDs to strings for consistency
        const trip = {
            ...result.rows[0],
            id: result.rows[0].id?.toString(),
            driverId: result.rows[0].driverId?.toString(),
            depotId: result.rows[0].depotId?.toString(),
            vehicleId: result.rows[0].vehicleId?.toString(),
            tenantId: result.rows[0].tenantId?.toString()
        };
        
        res.json(trip);
        
    } catch (error) {
        console.error('Error fetching today\'s trip:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

// GET /api/trips/{tripId}/shipments
router.get('/:tripId/shipments', async (req, res) => {
    const { tripId } = req.params;
    
    try {
        const query = `
            SELECT * FROM "TripShipmentLink"
            WHERE "tripId" = $1 
            ORDER BY "sequence" ASC
        `;
        
        const result = await pool.query(query, [parseInt(tripId)]);
        
        // Convert numeric IDs to strings for consistency
        const shipments = result.rows.map(row => ({
            ...row,
            id: row.id?.toString(),
            tripId: row.tripId?.toString(),
            shipmentId: row.shipmentId?.toString()
        }));
        
        res.json(shipments);
        
    } catch (error) {
        console.error('Error fetching trip shipments:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

// Get all trips
router.get('/', async (req, res) => {
    try {
        const result = await pool.query(`
            SELECT "id", "tripDate", "depotId", "driverId", "vehicleId", "status", 
                   "tenantId", "createdAt", "tripId"
            FROM "Trip"
            ORDER BY "tripDate" DESC
            LIMIT 100
        `);
        res.json(result.rows);
    } catch (error) {
        console.error('Error fetching trips:', error);
        res.status(500).json({ error: error.message });
    }
});

// Get trip by ID - MUST COME AFTER /today!
router.get('/:id', async (req, res) => {
    try {
        const { id } = req.params;
        
        // Skip if id is "today" - that should be handled by the /today route above
        if (id === 'today') {
            return res.status(400).json({ error: 'Use /api/trips/today?driverId=X instead' });
        }
        
        const result = await pool.query(`
            SELECT "id", "tripDate", "depotId", "driverId", "vehicleId", "status", 
                   "tenantId", "createdAt", "tripId"
            FROM "Trip"
            WHERE "id" = $1
        `, [parseInt(id)]);
        
        if (result.rows.length === 0) {
            return res.status(404).json({ message: 'Trip not found' });
        }
        
        res.json(result.rows[0]);
    } catch (error) {
        console.error('Error fetching trip:', error);
        res.status(500).json({ error: error.message });
    }
});

// Get trips by driver
router.get('/driver/:driverId', async (req, res) => {
    try {
        const { driverId } = req.params;
        const result = await pool.query(`
            SELECT "id", "tripDate", "depotId", "driverId", "vehicleId", "status", 
                   "tenantId", "createdAt", "tripId"
            FROM "Trip"
            WHERE "driverId" = $1
            ORDER BY "tripDate" DESC
        `, [parseInt(driverId)]);

        res.json(result.rows);
    } catch (error) {
        console.error('Error fetching driver trips:', error);
        res.status(500).json({ error: error.message });
    }
});

// Get trips by status
router.get('/status/:status', async (req, res) => {
    try {
        const { status } = req.params;
        const result = await pool.query(`
            SELECT "id", "tripDate", "depotId", "driverId", "vehicleId", "status", 
                   "tenantId", "createdAt", "tripId"
            FROM "Trip"
            WHERE "status" = $1
            ORDER BY "tripDate" ASC
        `, [status]);

        res.json(result.rows);
    } catch (error) {
        console.error('Error fetching trips by status:', error);
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;
