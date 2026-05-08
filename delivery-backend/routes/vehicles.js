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

// Get vehicle maintenance records
router.get('/:id/maintenance', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await pool.query(`
      SELECT vm.*, v.name as vehicleName, v.registration
      FROM "VehicleMaintenance" vm
      JOIN "Vehicle" v ON vm."vehicleId" = v.id
      WHERE vm."vehicleId" = $1
      ORDER BY vm.date DESC
      LIMIT 5
    `, [id]);

    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching vehicle maintenance:', error);
    res.status(500).json({ error: error.message });
  }
});

// Add test maintenance data for vehicle 2
router.post('/add-test-maintenance', async (req, res) => {
  try {
    console.log('🔧 Adding test maintenance data for vehicle 2...');
    
    // First, check if vehicle 2 exists
    const vehicleCheck = await pool.query('SELECT * FROM "Vehicle" WHERE id = 2');
    
    if (vehicleCheck.rows.length === 0) {
      return res.status(404).json({ error: 'Vehicle 2 not found in database' });
    }
    
    console.log('✅ Vehicle 2 found:', vehicleCheck.rows[0].name);
    
    // Check if maintenance data already exists
    const existingMaintenance = await pool.query('SELECT COUNT(*) as count FROM "VehicleMaintenance" WHERE "vehicleId" = 2');
    console.log('📊 Existing maintenance records for vehicle 2:', existingMaintenance.rows[0].count);
    
    // Only add data if none exists
    if (existingMaintenance.rows[0].count > 0) {
      return res.json({
        message: 'Vehicle 2 already has maintenance data',
        existingRecords: existingMaintenance.rows[0].count
      });
    }
    
    const maintenanceRecords = [
      {
        vehicleId: 2,
        type: 'Oil Change',
        date: '2026-05-03T10:00:00Z',
        nextMaintenance: '2026-05-13T10:00:00Z', // 5 days from now (URGENT)
        estimatedCost: 85.50,
        notes: 'Regular oil change with filter replacement',
        technician: 'Jean Dupont',
        status: 'completed'
      },
      {
        vehicleId: 2,
        type: 'Brake Inspection',
        date: '2026-04-15T14:00:00Z',
        nextMaintenance: '2026-05-28T14:00:00Z', // 20 days from now (WARNING)
        estimatedCost: 120.00,
        notes: 'Front brake pads check and replacement if needed',
        technician: 'Marie Martin',
        status: 'open'
      },
      {
        vehicleId: 2,
        type: 'Tire Rotation',
        date: '2026-04-01T09:00:00Z',
        nextMaintenance: '2026-06-22T09:00:00Z', // 45 days from now (NORMAL)
        estimatedCost: 45.00,
        notes: 'Rotate all four tires and check pressure',
        technician: 'Pierre Durand',
        status: 'completed'
      },
      {
        vehicleId: 2,
        type: 'Battery Check',
        date: '2026-05-05T11:00:00Z',
        nextMaintenance: '2026-05-12T11:00:00Z', // 4 days from now (URGENT)
        estimatedCost: 65.00,
        notes: 'Battery voltage test and terminal cleaning',
        technician: 'Sophie Bernard',
        status: 'open'
      },
      {
        vehicleId: 2,
        type: 'Air Filter Replacement',
        date: '2026-03-20T08:00:00Z',
        nextMaintenance: '2026-06-20T08:00:00Z', // 43 days from now (NORMAL)
        estimatedCost: 35.00,
        notes: 'Replace engine air filter',
        technician: 'Luc Petit',
        status: 'completed'
      }
    ];

    const results = [];
    
    for (const record of maintenanceRecords) {
      const result = await pool.query(
        `INSERT INTO "VehicleMaintenance" 
         ("vehicleId", "type", "date", "nextMaintenance", "estimatedCost", "notes", "technician", "status")
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
         RETURNING *`,
        [
          record.vehicleId,
          record.type,
          record.date,
          record.nextMaintenance,
          record.estimatedCost,
          record.notes,
          record.technician,
          record.status
        ]
      );
      results.push(result.rows[0]);
    }

    // Verify the data
    const verifyResult = await pool.query(
      'SELECT * FROM "VehicleMaintenance" WHERE "vehicleId" = 2 ORDER BY "date" DESC'
    );
    
    console.log('📊 Added test maintenance data for vehicle 2');
    console.log(`Total records: ${verifyResult.rows.length}`);
    
    res.json({
      message: 'Test maintenance data added successfully',
      records: results,
      totalRecords: verifyResult.rows.length
    });
  } catch (error) {
    console.error('Error adding test maintenance data:', error);
    res.status(500).json({ error: error.message });
  }
});

// Assign driver 5 to vehicle 2 for testing
router.post('/assign-driver', async (req, res) => {
  try {
    console.log('🔧 Assigning driver 5 to vehicle 2...');
    
    const result = await pool.query(
      'UPDATE "Vehicle" SET "driverId" = 5 WHERE id = 2 RETURNING *'
    );
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Vehicle 2 not found' });
    }
    
    console.log('✅ Driver 5 assigned to vehicle 2:', result.rows[0].name);
    
    res.json({
      message: 'Driver 5 successfully assigned to vehicle 2',
      vehicle: result.rows[0]
    });
  } catch (error) {
    console.error('Error assigning driver:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
