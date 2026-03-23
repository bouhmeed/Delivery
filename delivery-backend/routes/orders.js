const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get all orders
router.get('/', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT "id", "orderNumber", "customerId", "customerName", "orderDate", 
             "deliveryAddress", "deliveryCity", "deliveryZipCode", "deliveryCountry",
             "status", "priority", "estimatedDelivery", "tenantId", 
             "createdAt", "updatedAt", "shipmentId"
      FROM "Order"
      ORDER BY "orderDate" DESC
      LIMIT 100
    `);
    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching orders:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get order by ID
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await pool.query(`
      SELECT "id", "orderNumber", "customerId", "customerName", "orderDate", 
             "deliveryAddress", "deliveryCity", "deliveryZipCode", "deliveryCountry",
             "status", "priority", "estimatedDelivery", "tenantId", 
             "createdAt", "updatedAt", "shipmentId"
      FROM "Order"
      WHERE "id" = $1
    `, [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({ message: 'Order not found' });
    }

    res.json(result.rows[0]);
  } catch (error) {
    console.error('Error fetching order:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get orders by customer ID
router.get('/customer/:customerId', async (req, res) => {
  try {
    const { customerId } = req.params;
    const result = await pool.query(`
      SELECT "id", "orderNumber", "customerId", "customerName", "orderDate", 
             "deliveryAddress", "deliveryCity", "deliveryZipCode", "deliveryCountry",
             "status", "priority", "estimatedDelivery", "tenantId", 
             "createdAt", "updatedAt", "shipmentId"
      FROM "Order"
      WHERE "customerId" = $1
      ORDER BY "orderDate" DESC
    `, [customerId]);

    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching customer orders:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
