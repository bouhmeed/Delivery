const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get all items
router.get('/', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT * FROM "Item"
      WHERE "isActive" = true
      ORDER BY "createdAt" DESC
      LIMIT 100
    `);
    res.json({
      success: true,
      data: result.rows,
      message: null
    });
  } catch (error) {
    console.error('Error fetching items:', error);
    res.status(500).json({
      success: false,
      data: [],
      message: error.message
    });
  }
});

// Get item by ID
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await pool.query(`
      SELECT * FROM "Item"
      WHERE "id" = $1
    `, [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({ message: 'Item not found' });
    }

    res.json(result.rows[0]);
  } catch (error) {
    console.error('Error fetching item:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
