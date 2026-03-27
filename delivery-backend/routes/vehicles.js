const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get vehicles (with optional driverId filter)
router.get('/', async (req, res) => {
  try {
    const { driverId } = req.query;
    let query = `
      SELECT * FROM "Vehicle"
    `;
    let params = [];
    
    if (driverId) {
      query += ` WHERE "driverId" = $1`;
      params.push(driverId);
    }
    
    query += ` ORDER BY "id" DESC LIMIT 100`;
    
    const result = await pool.query(query, params);
    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching vehicles:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get vehicle by ID
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await pool.query(`
      SELECT * FROM "Vehicle"
      WHERE "id" = $1
    `, [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({ message: 'Vehicle not found' });
    }

    res.json(result.rows[0]);
  } catch (error) {
    console.error('Error fetching vehicle:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
