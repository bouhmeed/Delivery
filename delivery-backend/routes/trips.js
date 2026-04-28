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

// Get trip by ID (basic - for backward compatibility)
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

// Get detailed trip by ID (with shipments, stops, driver, vehicle)
router.get('/:id/details', async (req, res) => {
  try {
    const { id } = req.params;
    
    // Get basic trip info
    const tripResult = await pool.query(`
      SELECT "id", "tripDate", "depotId", "driverId", "vehicleId", "status", 
             "tenantId", "createdAt", "tripId"
      FROM "Trip"
      WHERE "id" = $1
    `, [id]);

    if (tripResult.rows.length === 0) {
      return res.status(404).json({ 
        success: false, 
        error: 'Trip not found' 
      });
    }

    const trip = tripResult.rows[0];

    // Calculate trip statistics
    const statsResult = await pool.query(`
      SELECT 
        COUNT(*) as "totalShipments",
        COUNT(CASE WHEN s.status = 'DELIVERED' THEN 1 END) as "completedShipments",
        COALESCE(SUM(s.weight), 0) as "totalWeight",
        COALESCE(SUM(s."distanceKm"), 0) as "totalDistance",
        COALESCE(AVG(s."estimatedDuration"), 0) as "estimatedDuration"
      FROM "TripShipmentLink" tsl
      JOIN "Shipment" s ON tsl."shipmentId" = s."id"
      WHERE tsl."tripId" = $1
    `, [id]);

    const stats = statsResult.rows[0];

    // Get shipments for this trip
    const shipmentsResult = await pool.query(`
      SELECT 
        s.id,
        s."shipmentNo",
        s."trackingNumber",
        s.status,
        SUBSTRING(s.description, 1, 200) as description,
        s.quantity,
        s.uom,
        s.packaging,
        s.weight,
        s."deliveryAddress",
        s."deliveryCity",
        s."deliveryZipCode",
        s."deliveryCountry",
        s."customerId",
        s."originId",
        s."destinationId",
        s."vehicleId",
        s.priority,
        s."requestedPickup",
        s."requestedDelivery",
        s."plannedStart",
        s."plannedEnd",
        s."distanceKm",
        s."estimatedDuration",
        s."createdAt",
        s."updatedAt",
        c.name as "customerName",
        l1.address as "originAddress",
        l1.city as "originCity",
        l2.address as "destinationAddress", 
        l2.city as "destinationCity"
      FROM "TripShipmentLink" tsl
      JOIN "Shipment" s ON tsl."shipmentId" = s."id"
      LEFT JOIN "Client" c ON s."customerId" = c."id"
      LEFT JOIN "Location" l1 ON s."originId" = l1."id"
      LEFT JOIN "Location" l2 ON s."destinationId" = l2."id"
      WHERE tsl."tripId" = $1
      ORDER BY tsl.sequence
    `, [id]);

    // Get trip stops
    const stopsResult = await pool.query(`
      SELECT ts.*, l.name as "locationName", l.address as "locationAddress"
      FROM "TripStop" ts
      JOIN "Location" l ON ts."locationId" = l."id"
      WHERE ts."tripId" = $1
      ORDER BY ts.sequence
    `, [id]);

    // Get driver info
    let driver = null;
    if (trip.driverId) {
      const driverResult = await pool.query(`
        SELECT "id", "name", "licenseNumber", "employmentType", "status", "phone", "email"
        FROM "Driver"
        WHERE "id" = $1
      `, [trip.driverId]);
      
      if (driverResult.rows.length > 0) {
        driver = driverResult.rows[0];
      }
    }

    // Get vehicle info
    let vehicle = null;
    if (trip.vehicleId) {
      const vehicleResult = await pool.query(`
        SELECT "id", "name", "registration", "capacityWeight", "capacityVolume", "type", "status"
        FROM "Vehicle"
        WHERE "id" = $1
      `, [trip.vehicleId]);
      
      if (vehicleResult.rows.length > 0) {
        vehicle = vehicleResult.rows[0];
      }
    }

    // Get depot info
    let depot = null;
    if (trip.depotId) {
      const depotResult = await pool.query(`
        SELECT "id", "name", "address", "city", "postalCode"
        FROM "Location"
        WHERE "id" = $1
      `, [trip.depotId]);
      
      if (depotResult.rows.length > 0) {
        depot = depotResult.rows[0];
      }
    }

    // Build detailed trip object
    const detailedTrip = {
      ...trip,
      totalShipments: parseInt(stats.totalShipments) || 0,
      completedShipments: parseInt(stats.completedShipments) || 0,
      totalDistance: parseFloat(stats.totalDistance) || 0,
      estimatedDuration: parseInt(stats.estimatedDuration) || 0,
      actualDuration: null // Would need to be calculated from actual start/end times
    };

    const response = {
      success: true,
      data: {
        trip: detailedTrip,
        shipments: shipmentsResult.rows,
        stops: stopsResult.rows,
        driver: driver,
        vehicle: vehicle,
        depot: depot
      }
    };

    res.json(response);
  } catch (error) {
    console.error('Error fetching trip details:', error);
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
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
