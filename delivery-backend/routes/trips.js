const express = require('express');
const router = express.Router();
const pool = require('../config/database');

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

// Get trip by ID
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await pool.query(`
      SELECT "id", "tripDate", "depotId", "driverId", "vehicleId", "status", 
             "tenantId", "createdAt", "tripId"
      FROM "Trip"
      WHERE "id" = $1
    `, [id]);

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
    `, [driverId]);

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
