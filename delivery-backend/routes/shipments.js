const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get all shipments
router.get('/', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT "id", "shipmentNo", "customerId", "type", "originId", "destinationId",
             "priority", "requestedPickup", "requestedDelivery", "status", "description",
             "quantity", "uom", "packaging", "weight", "volume", "stackable",
             "carrier", "trackingNumber", "deliveryAddress", "deliveryCity", 
             "deliveryZipCode", "deliveryCountry", "tenantId", "createdAt", 
             "updatedAt", "createdById", "updatedById", "driverId", "vehicleId",
             "estimatedDuration", "outlookEventId", "plannedEnd", "plannedStart",
             "distanceKm"
      FROM "Shipment"
      ORDER BY "createdAt" DESC
      LIMIT 100
    `);
    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching shipments:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get shipment by ID
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await pool.query(`
      SELECT "id", "shipmentNo", "customerId", "type", "originId", "destinationId",
             "priority", "requestedPickup", "requestedDelivery", "status", "description",
             "quantity", "uom", "packaging", "weight", "volume", "stackable",
             "carrier", "trackingNumber", "deliveryAddress", "deliveryCity", 
             "deliveryZipCode", "deliveryCountry", "tenantId", "createdAt", 
             "updatedAt", "createdById", "updatedById", "driverId", "vehicleId",
             "estimatedDuration", "outlookEventId", "plannedEnd", "plannedStart",
             "distanceKm"
      FROM "Shipment"
      WHERE "id" = $1
    `, [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({ message: 'Shipment not found' });
    }

    res.json(result.rows[0]);
  } catch (error) {
    console.error('Error fetching shipment:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get shipments by status
router.get('/status/:status', async (req, res) => {
  try {
    const { status } = req.params;
    const result = await pool.query(`
      SELECT "id", "shipmentNo", "customerId", "type", "originId", "destinationId",
             "priority", "requestedPickup", "requestedDelivery", "status", "description",
             "quantity", "uom", "packaging", "weight", "volume", "stackable",
             "carrier", "trackingNumber", "deliveryAddress", "deliveryCity", 
             "deliveryZipCode", "deliveryCountry", "tenantId", "createdAt", 
             "updatedAt", "createdById", "updatedById", "driverId", "vehicleId",
             "estimatedDuration", "outlookEventId", "plannedEnd", "plannedStart",
             "distanceKm"
      FROM "Shipment"
      WHERE "status" = $1
      ORDER BY "requestedDelivery" ASC
    `, [status]);

    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching shipments by status:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get shipments by driver
router.get('/driver/:driverId', async (req, res) => {
  try {
    const { driverId } = req.params;
    const result = await pool.query(`
      SELECT "id", "shipmentNo", "customerId", "type", "originId", "destinationId",
             "priority", "requestedPickup", "requestedDelivery", "status", "description",
             "quantity", "uom", "packaging", "weight", "volume", "stackable",
             "carrier", "trackingNumber", "deliveryAddress", "deliveryCity", 
             "deliveryZipCode", "deliveryCountry", "tenantId", "createdAt", 
             "updatedAt", "createdById", "updatedById", "driverId", "vehicleId",
             "estimatedDuration", "outlookEventId", "plannedEnd", "plannedStart",
             "distanceKm"
      FROM "Shipment"
      WHERE "driverId" = $1
      ORDER BY "requestedDelivery" ASC
    `, [driverId]);

    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching driver shipments:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
