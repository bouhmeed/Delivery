const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get all drivers
router.get('/', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT "id", "name", "licenseNumber", "licenseExpiry", "employmentType",
             "contractHoursWeek", "homeDepotId", "tenantId", "status", "address",
             "assignedVehicle", "city", "country", "createdAt", "dateOfBirth",
             "email", "hireDate", "licenseIssueDate", "phone", "postalCode",
             "salary", "updatedAt"
      FROM "Driver"
      ORDER BY "name" ASC
      LIMIT 100
    `);
    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching drivers:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get driver by ID
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await pool.query(`
      SELECT "id", "name", "licenseNumber", "licenseExpiry", "employmentType",
             "contractHoursWeek", "homeDepotId", "tenantId", "status", "address",
             "assignedVehicle", "city", "country", "createdAt", "dateOfBirth",
             "email", "hireDate", "licenseIssueDate", "phone", "postalCode",
             "salary", "updatedAt"
      FROM "Driver"
      WHERE "id" = $1
    `, [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({ message: 'Driver not found' });
    }

    res.json(result.rows[0]);
  } catch (error) {
    console.error('Error fetching driver:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get active drivers
router.get('/status/active', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT "id", "name", "licenseNumber", "licenseExpiry", "employmentType",
             "contractHoursWeek", "homeDepotId", "tenantId", "status", "address",
             "assignedVehicle", "city", "country", "createdAt", "dateOfBirth",
             "email", "hireDate", "licenseIssueDate", "phone", "postalCode",
             "salary", "updatedAt"
      FROM "Driver"
      WHERE "status" = 'ACTIF'
      ORDER BY "name" ASC
    `);
    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching active drivers:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
