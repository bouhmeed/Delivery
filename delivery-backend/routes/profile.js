const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get driver profile information
router.get('/:driverId', async (req, res) => {
  try {
    const { driverId } = req.params;

    const profileQuery = `
      SELECT 
        d.id,
        d.name,
        d."licenseNumber",
        d."licenseExpiry",
        d."employmentType",
        d."contractHoursWeek",
        d."homeDepotId",
        d."tenantId",
        d.status,
        d.address,
        d."assignedVehicle",
        d.city,
        d.country,
        d."dateOfBirth",
        d.email,
        d."hireDate",
        d.phone,
        d."postalCode",
        d.salary,
        d."createdAt",
        d."updatedAt"
      FROM "Driver" d
      WHERE d.id = $1
    `;

    const profileResult = await pool.query(profileQuery, [driverId]);

    if (profileResult.rows.length === 0) {
      return res.status(404).json({ error: 'Driver not found' });
    }

    const driverProfile = profileResult.rows[0];

    // Get vehicle information if assigned
    let vehicleInfo = null;
    if (driverProfile.assignedVehicle) {
      const vehicleQuery = `
        SELECT 
          v.id,
          v.name,
          v.registration,
          v."capacityWeight",
          v."capacityVolume",
          v.type,
          v.year,
          v.status
        FROM "Vehicle" v
        WHERE v.registration = $1
      `;

      const vehicleResult = await pool.query(vehicleQuery, [driverProfile.assignedVehicle]);
      vehicleInfo = vehicleResult.rows[0] || null;
    }

    // Get depot information
    let depotInfo = null;
    if (driverProfile.homeDepotId) {
      const depotQuery = `
        SELECT 
          l.id,
          l.name,
          l.address,
          l.city,
          l."postalCode",
          l.phone,
          l.email
        FROM "Location" l
        WHERE l.id = $1
      `;

      const depotResult = await pool.query(depotQuery, [driverProfile.homeDepotId]);
      depotInfo = depotResult.rows[0] || null;
    }

    res.json({
      profile: driverProfile,
      vehicle: vehicleInfo,
      depot: depotInfo
    });

  } catch (error) {
    console.error('Error fetching driver profile:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get driver statistics summary
router.get('/:driverId/stats', async (req, res) => {
  try {
    const { driverId } = req.params;
    const { period = '365' } = req.query; // Default 1 year

    const statsQuery = `
      SELECT 
        COUNT(DISTINCT t.id) as total_trips,
        COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) as completed_trips,
        COUNT(DISTINCT s.id) as total_shipments,
        COUNT(DISTINCT CASE WHEN s.status = 'DELIVERED' THEN s.id END) as delivered_shipments,
        COALESCE(SUM(s.quantity), 0) as total_quantity,
        COALESCE(SUM(s.weight), 0) as total_weight,
        COALESCE(AVG(s.weight), 0) as avg_weight,
        MAX(t."tripDate") as last_trip_date,
        MIN(t."tripDate") as first_trip_date
      FROM "Trip" t
      LEFT JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
      LEFT JOIN "Shipment" s ON tsl."shipmentId" = s.id
      WHERE t."driverId" = $1 
        AND t."tripDate" >= CURRENT_DATE - INTERVAL '${period} days'
    `;

    const statsResult = await pool.query(statsQuery, [driverId]);
    const stats = statsResult.rows[0] || {};

    // Calculate success rate
    const successRate = stats.total_trips > 0 
      ? Math.round((stats.completed_trips / stats.total_trips) * 100)
      : 0;

    const driverStats = {
      driverId: parseInt(driverId),
      totalTrips: parseInt(stats.total_trips) || 0,
      completedTrips: parseInt(stats.completed_trips) || 0,
      totalShipments: parseInt(stats.total_shipments) || 0,
      deliveredShipments: parseInt(stats.delivered_shipments) || 0,
      totalQuantity: parseFloat(stats.total_quantity) || 0,
      totalWeight: parseFloat(stats.total_weight) || 0,
      averageWeight: parseFloat(stats.avg_weight) || 0,
      successRate: successRate,
      lastTripDate: stats.last_trip_date,
      firstTripDate: stats.first_trip_date
    };

    res.json(driverStats);

  } catch (error) {
    console.error('Error fetching driver stats:', error);
    res.status(500).json({ error: error.message });
  }
});

// Update driver profile
router.put('/:driverId', async (req, res) => {
  try {
    const { driverId } = req.params;
    const updates = req.body;

    // Build dynamic update query
    const updateFields = [];
    const updateValues = [];
    let paramIndex = 1;

    if (updates.name !== undefined) {
      updateFields.push(`name = $${paramIndex}`);
      updateValues.push(updates.name);
      paramIndex++;
    }

    if (updates.phone !== undefined) {
      updateFields.push(`phone = $${paramIndex}`);
      updateValues.push(updates.phone);
      paramIndex++;
    }

    if (updates.email !== undefined) {
      updateFields.push(`email = $${paramIndex}`);
      updateValues.push(updates.email);
      paramIndex++;
    }

    if (updates.address !== undefined) {
      updateFields.push(`address = $${paramIndex}`);
      updateValues.push(updates.address);
      paramIndex++;
    }

    if (updateFields.length === 0) {
      return res.status(400).json({ error: 'No valid fields to update' });
    }

    updateFields.push(`"updatedAt" = CURRENT_TIMESTAMP`);
    
    const updateQuery = `
      UPDATE "Driver" 
      SET ${updateFields.join(', ')}
      WHERE id = $${paramIndex}
    `;

    updateValues.push(driverId);

    await pool.query(updateQuery, updateValues);

    // Get updated profile
    const profileQuery = `
      SELECT * FROM "Driver" WHERE id = $1
    `;
    const updatedResult = await pool.query(profileQuery, [driverId]);
    
    res.json({
      message: 'Profile updated successfully',
      profile: updatedResult.rows[0]
    });

  } catch (error) {
    console.error('Error updating driver profile:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
