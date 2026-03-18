const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get user information by email
router.get('/:email', async (req, res) => {
  try {
    const { email } = req.params;

    if (!email) {
      return res.status(400).json({ message: 'Email parameter is required' });
    }

    // Query the User table with the specified columns
    const result = await pool.query(`
      SELECT "id", "tenantId", "email", "role", "firstName", "lastName", "driverId", "isActive", "createdAt", "updatedAt"
      FROM "User"
      WHERE "email" = $1
    `, [email]);

    if (result.rows.length === 0) {
      return res.status(404).json({ message: 'User not found' });
    }

    res.json(result.rows[0]);
  } catch (error) {
    console.error('Error fetching user:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get current user profile (by email query parameter)
router.get('/profile', async (req, res) => {
  try {
    const { email } = req.query;
    
    if (!email) {
      return res.status(400).json({ message: 'Email parameter is required' });
    }
    
    // Query the User table with the specified columns
    const result = await pool.query(`
      SELECT "id", "tenantId", "email", "role", "firstName", "lastName", "driverId", "isActive", "createdAt", "updatedAt"
      FROM "User" 
      WHERE "email" = $1
    `, [email]);
    
    if (result.rows.length === 0) {
      return res.status(404).json({ message: 'User not found' });
    }
    
    res.json(result.rows[0]);
  } catch (error) {
    console.error('Error fetching user profile:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
