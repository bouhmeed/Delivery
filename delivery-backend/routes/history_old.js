const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get driver history (trips and shipments)
router.get('/driver/:driverId', async (req, res) => {
  try {
    const { driverId } = req.params;
    const { limit = 50, offset = 0 } = req.query;

    // Get driver trips with related data
    const tripsQuery = `
      SELECT 
        t.id,
        t.tripDate,
        t.status as tripStatus,
        t.vehicleId,
        v.name as vehicleName,
        v.registration as licensePlate,
        COUNT(tsl.shipmentId) as shipmentCount,
        COALESCE(SUM(s.quantity), 0) as totalQuantity,
        t.createdAt
      FROM "Trip" t
      LEFT JOIN "Vehicle" v ON t.vehicleId = v.id
      LEFT JOIN "TripShipmentLink" tsl ON t.id = tsl.tripId
      LEFT JOIN "Shipment" s ON tsl.shipmentId = s.id
      WHERE t.driverId = $1
      GROUP BY t.id, v.name, v.registration
      ORDER BY t.tripDate DESC
      LIMIT $2 OFFSET $3
    `;

    const tripsResult = await pool.query(tripsQuery, [driverId, limit, offset]);

    // Get shipments for each trip
    const history = [];
    for (const trip of tripsResult.rows) {
      const shipmentsQuery = `
        SELECT 
          s.id,
          s.shipmentNo,
          s.type,
          s.status as shipmentStatus,
          s.originId,
          o.name as originName,
          o.address as originAddress,
          s.destinationId,
          d.name as destinationName,
          d.address as destinationAddress,
          s.priority,
          s.quantity,
          s.uom,
          s.weight,
          s.requestedPickup,
          s.requestedDelivery,
          s.deliveryAddress,
          s.deliveryCity,
          s.trackingNumber,
          s.description,
          tsl.pickupOrder,
          tsl.deliveryOrder
        FROM "TripShipmentLink" tsl
        JOIN "Shipment" s ON tsl.shipmentId = s.id
        LEFT JOIN "Location" o ON s.originId = o.id
        LEFT JOIN "Location" d ON s.destinationId = d.id
        WHERE tsl.tripId = $1
        ORDER BY tsl.pickupOrder ASC
      `;

      const shipmentsResult = await pool.query(shipmentsQuery, [trip.id]);
      
      history.push({
        ...trip,
        shipments: shipmentsResult.rows
      });
    }

    // Get total count for pagination
    const countQuery = `
      SELECT COUNT(*) as total
      FROM "Trip"
      WHERE driverId = $1
    `;
    const countResult = await pool.query(countQuery, [driverId]);

    res.json({
      history,
      pagination: {
        total: parseInt(countResult.rows[0].total),
        limit: parseInt(limit),
        offset: parseInt(offset),
        hasMore: parseInt(offset) + parseInt(limit) < parseInt(countResult.rows[0].total)
      }
    });
  } catch (error) {
    console.error('Error fetching driver history:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get trip details with all shipments
router.get('/trip/:tripId', async (req, res) => {
  try {
    const { tripId } = req.params;

    // Get trip details
    const tripQuery = `
      SELECT 
        t.id,
        t.tripDate,
        t.status as tripStatus,
        t.driverId,
        d.firstName || ' ' || d.lastName as driverName,
        t.vehicleId,
        v.name as vehicleName,
        v.registration as licensePlate,
        t.createdAt
      FROM "Trip" t
      LEFT JOIN "Driver" d ON t.driverId = d.id
      LEFT JOIN "Vehicle" v ON t.vehicleId = v.id
      WHERE t.id = $1
    `;

    const tripResult = await pool.query(tripQuery, [tripId]);

    if (tripResult.rows.length === 0) {
      return res.status(404).json({ message: 'Trip not found' });
    }

    // Get shipments for this trip
    const shipmentsQuery = `
      SELECT 
        s.id,
        s.shipmentNo,
        s.type,
        s.status as shipmentStatus,
        s.originId,
        o.name as originName,
        o.address as originAddress,
        o.city as originCity,
        s.destinationId,
        d.name as destinationName,
        d.address as destinationAddress,
        d.city as destinationCity,
        s.priority,
        s.quantity,
        s.uom,
        s.weight,
        s.requestedPickup,
        s.requestedDelivery,
        s.deliveryAddress,
        s.deliveryCity,
        s.deliveryZipCode,
        s.trackingNumber,
        s.description,
        s.createdAt as shipmentCreatedAt,
        tsl.pickupOrder,
        tsl.deliveryOrder
      FROM "TripShipmentLink" tsl
      JOIN "Shipment" s ON tsl.shipmentId = s.id
      LEFT JOIN "Location" o ON s.originId = o.id
      LEFT JOIN "Location" d ON s.destinationId = d.id
      WHERE tsl.tripId = $1
      ORDER BY tsl.pickupOrder ASC
    `;

    const shipmentsResult = await pool.query(shipmentsQuery, [tripId]);

    // Get trip stops
    const stopsQuery = `
      SELECT 
        ts.id,
        ts.locationId,
        l.name as locationName,
        l.address as locationAddress,
        l.city as locationCity,
        ts.type,
        ts.plannedArrival,
        ts.actualArrival,
        ts.plannedDeparture,
        ts.actualDeparture,
        ts.status
      FROM "TripStop" ts
      LEFT JOIN "Location" l ON ts.locationId = l.id
      WHERE ts.tripId = $1
      ORDER BY ts.plannedArrival ASC
    `;

    const stopsResult = await pool.query(stopsQuery, [tripId]);

    res.json({
      trip: tripResult.rows[0],
      shipments: shipmentsResult.rows,
      stops: stopsResult.rows
    });
  } catch (error) {
    console.error('Error fetching trip details:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get shipment history by status
router.get('/shipments/status/:status', async (req, res) => {
  try {
    const { status } = req.params;
    const { limit = 50, offset = 0, driverId } = req.query;

    let whereClause = `WHERE s.status = $1`;
    let queryParams = [status, limit, offset];
    let paramIndex = 3;

    if (driverId) {
      whereClause += ` AND s.driverId = $${paramIndex}`;
      queryParams.splice(2, 0, driverId);
      paramIndex++;
    }

    const shipmentsQuery = `
      SELECT 
        s.id,
        s.shipmentNo,
        s.type,
        s.status as shipmentStatus,
        s.originId,
        o.name as originName,
        o.address as originAddress,
        o.city as originCity,
        s.destinationId,
        d.name as destinationName,
        d.address as destinationAddress,
        d.city as destinationCity,
        s.priority,
        s.quantity,
        s.uom,
        s.weight,
        s.requestedPickup,
        s.requestedDelivery,
        s.deliveryAddress,
        s.deliveryCity,
        s.deliveryZipCode,
        s.trackingNumber,
        s.description,
        s.createdAt,
        s.updatedAt,
        s.driverId,
        dr.firstName || ' ' || dr.lastName as driverName,
        v.name as vehicleName,
        v.registration as licensePlate
      FROM "Shipment" s
      LEFT JOIN "Location" o ON s.originId = o.id
      LEFT JOIN "Location" d ON s.destinationId = d.id
      LEFT JOIN "Driver" dr ON s.driverId = dr.id
      LEFT JOIN "Vehicle" v ON s.vehicleId = v.id
      ${whereClause}
      ORDER BY s.createdAt DESC
      LIMIT $${paramIndex - 1} OFFSET $${paramIndex}
    `;

    const shipmentsResult = await pool.query(shipmentsQuery, queryParams);

    // Get total count
    let countWhereClause = `WHERE status = $1`;
    let countParams = [status];
    if (driverId) {
      countWhereClause += ` AND driverId = $2`;
      countParams.push(driverId);
    }

    const countQuery = `
      SELECT COUNT(*) as total
      FROM "Shipment"
      ${countWhereClause}
    `;

    const countResult = await pool.query(countQuery, countParams);

    res.json({
      shipments: shipmentsResult.rows,
      pagination: {
        total: parseInt(countResult.rows[0].total),
        limit: parseInt(limit),
        offset: parseInt(offset),
        hasMore: parseInt(offset) + parseInt(limit) < parseInt(countResult.rows[0].total)
      }
    });
  } catch (error) {
    console.error('Error fetching shipments by status:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get driver statistics
router.get('/stats/driver/:driverId', async (req, res) => {
  try {
    const { driverId } = req.params;
    const { period = '30' } = req.query; // days

    const statsQuery = `
      SELECT 
        COUNT(DISTINCT t.id) as totalTrips,
        COUNT(DISTINCT s.id) as totalShipments,
        COALESCE(SUM(s.quantity), 0) as totalQuantity,
        COALESCE(SUM(s.weight), 0) as totalWeight,
        COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) as completedTrips,
        COUNT(DISTINCT CASE WHEN s.status = 'DELIVERED' THEN s.id END) as deliveredShipments,
        COUNT(DISTINCT CASE WHEN t.status = 'IN_PROGRESS' THEN t.id END) as activeTrips,
        COUNT(DISTINCT CASE WHEN s.status = 'PENDING' THEN s.id END) as pendingShipments,
        MAX(t.tripDate) as lastTripDate
      FROM "Trip" t
      LEFT JOIN "TripShipmentLink" tsl ON t.id = tsl.tripId
      LEFT JOIN "Shipment" s ON tsl.shipmentId = s.id
      WHERE t.driverId = $1 
        AND t.tripDate >= CURRENT_DATE - INTERVAL '${period} days'
    `;

    const statsResult = await pool.query(statsQuery, [driverId]);

    // Get monthly trends
    const trendsQuery = `
      SELECT 
        DATE_TRUNC('month', t.tripDate) as month,
        COUNT(DISTINCT t.id) as trips,
        COUNT(DISTINCT s.id) as shipments,
        COALESCE(SUM(s.quantity), 0) as quantity
      FROM "Trip" t
      LEFT JOIN "TripShipmentLink" tsl ON t.id = tsl.tripId
      LEFT JOIN "Shipment" s ON tsl.shipmentId = s.id
      WHERE t.driverId = $1 
        AND t.tripDate >= CURRENT_DATE - INTERVAL '12 months'
      GROUP BY DATE_TRUNC('month', t.tripDate)
      ORDER BY month DESC
      LIMIT 6
    `;

    const trendsResult = await pool.query(trendsQuery, [driverId]);

    res.json({
      stats: statsResult.rows[0] || {},
      trends: trendsResult.rows
    });
  } catch (error) {
    console.error('Error fetching driver stats:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
