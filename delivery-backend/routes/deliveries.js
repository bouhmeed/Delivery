const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get deliveries
router.get('/', async (req, res) => {
  try {
    // Try common table names for deliveries
    let query = '';
    const possibleTables = ['deliveries', 'orders', 'tours', 'expeditions'];
    
    for (const table of possibleTables) {
      try {
        const result = await pool.query(`SELECT * FROM ${table} LIMIT 50`);
        query = `SELECT * FROM ${table}`;
        return res.json(result.rows);
      } catch (e) {
        // Table doesn't exist, try next one
        continue;
      }
    }
    
    res.json({ message: 'No delivery table found. Available tables:', tables: [] });
  } catch (error) {
    console.error('Error fetching deliveries:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get drivers
router.get('/drivers', async (req, res) => {
  try {
    const possibleTables = ['drivers', 'chauffeurs', 'users'];
    
    for (const table of possibleTables) {
      try {
        const result = await pool.query(`SELECT * FROM ${table}`);
        return res.json(result.rows);
      } catch (e) {
        continue;
      }
    }
    
    res.json({ message: 'No driver table found' });
  } catch (error) {
    console.error('Error fetching drivers:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
