const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get all tables (to explore existing structure)
router.get('/tables', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT table_name 
      FROM information_schema.tables 
      WHERE table_schema = 'public'
      ORDER BY table_name
    `);
    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching tables:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get table structure
router.get('/tables/:tableName/structure', async (req, res) => {
  try {
    const { tableName } = req.params;
    const result = await pool.query(`
      SELECT column_name, data_type, is_nullable, column_default
      FROM information_schema.columns 
      WHERE table_name = $1 AND table_schema = 'public'
      ORDER BY ordinal_position
    `, [tableName]);
    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching table structure:', error);
    res.status(500).json({ error: error.message });
  }
});

// Generic endpoint to get all data from any table
router.get('/:tableName', async (req, res) => {
  try {
    const { tableName } = req.params;
    // Sanitize table name to prevent SQL injection
    const validTableName = tableName.replace(/[^a-zA-Z0-9_]/g, '');
    const result = await pool.query(`SELECT * FROM "${validTableName}" LIMIT 100`);
    res.json(result.rows);
  } catch (error) {
    console.error(`Error fetching data from ${req.params.tableName}:`, error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
