const express = require('express');

const cors = require('cors');

require('dotenv').config();



// Import routes

const databaseRoutes = require('./routes/database');

const userRoutes = require('./routes/users');

const orderRoutes = require('./routes/orders');

const driverRoutes = require('./routes/drivers');

const shipmentRoutes = require('./routes/shipments');

const clientRoutes = require('./routes/clients');

const vehicleRoutes = require('./routes/vehicles');

const itemRoutes = require('./routes/items');

const locationRoutes = require('./routes/locations');

const todayTourRoutes = require('./routes/today-tour');

const shipmentSearchRoutes = require('./routes/shipment');

const tourneeDetailsRoutes = require('./routes/tourneeDetails');

const progressionRoutes = require('./routes/progression');

const deliveryTrackingRoutes = require('./routes/delivery-tracking');

const deliveryTrackingV2Routes = require('./routes/delivery-tracking-v2');

const shipmentDetailsRoutes = require('./routes/shipment-details');

const deliveryValidationRoutes = require('./routes/delivery-validation');

const historyRoutes = require('./routes/history');

const profileRoutes = require('./routes/profile');

const shipmentTripRoutes = require('./routes/shipment-trip');



const app = express();

app.use(cors());

// Increase body parser limit for large Base64 images

app.use(express.json({ limit: '50mb' }));

app.use(express.urlencoded({ limit: '50mb', extended: true }));



// API Routes

app.use('/api/orders', orderRoutes);

app.use('/api/drivers', driverRoutes);

app.use('/api/shipments', shipmentRoutes);

app.use('/api/trips', progressionRoutes);

app.use('/api/delivery-tracking', deliveryTrackingRoutes);

app.use('/api/delivery-tracking/v2', deliveryTrackingV2Routes);

app.use('/api/shipments', shipmentDetailsRoutes);

app.use('/api/clients', clientRoutes);

app.use('/api/vehicles', vehicleRoutes);

app.use('/api/items', itemRoutes);

app.use('/api/locations', locationRoutes);

app.use('/api/today-tour', todayTourRoutes);

app.use('/api/shipment', shipmentSearchRoutes);

app.use('/api/tournee', tourneeDetailsRoutes);

app.use('/api/user', userRoutes);

app.use('/api/delivery-validation', deliveryValidationRoutes);

app.use('/api/history', historyRoutes);

app.use('/api/profile', profileRoutes);

app.use('/api/shipment', shipmentTripRoutes); // Pour les expéditions par trip

app.use('/api', databaseRoutes);



const PORT = process.env.PORT || 3000;

app.listen(PORT, '0.0.0.0', () => {

  console.log(`🚀 Server running on http://0.0.0.0:${PORT}`);

  console.log(`🌐 Accessible from Android emulator at: http://10.0.2.2:${PORT}`);

  console.log('📊 Available endpoints:');

  console.log('  Database Exploration:');

  console.log('    GET /api/tables - List all tables');

  console.log('    GET /api/tables/:tableName/structure - Get table structure');

  console.log('    GET /api/:tableName - Get data from any table');

  console.log('');

  console.log('  Users:');

  console.log('    GET /api/user/:email - Get user by email');

  console.log('    GET /api/user/profile?email=... - Get current user profile');

  console.log('');

  console.log('  Orders:');

  console.log('    GET /api/orders - Get all orders');

  console.log('    GET /api/orders/:id - Get order by ID');

  console.log('    GET /api/orders/customer/:customerId - Get orders by customer');

  console.log('');

  console.log('  Drivers:');

  console.log('    GET /api/drivers - Get all drivers');

  console.log('    GET /api/drivers/:id - Get driver by ID');

  console.log('    GET /api/drivers/status/active - Get active drivers');

  console.log('');

  console.log('  Shipments:');

  console.log('    GET /api/shipments - Get all shipments');

  console.log('    GET /api/shipments/:id - Get shipment by ID');

  console.log('    GET /api/shipments/status/:status - Get shipments by status');

  console.log('    GET /api/shipments/driver/:driverId - Get shipments by driver');

  console.log('');

  console.log('  Trips:');

  console.log('    GET /api/trips - Get all trips');

  console.log('    GET /api/trips/:id - Get trip by ID');

  console.log('    GET /api/trips/driver/:driverId - Get trips by driver');

  console.log('    GET /api/trips/status/:status - Get trips by status');

  console.log('');

  console.log('  Other Entities:');

  console.log('    GET /api/clients - Get all clients');

  console.log('    GET /api/clients/:id - Get client by ID');

  console.log('    GET /api/vehicles - Get all vehicles');

  console.log('    GET /api/vehicles/:id - Get vehicle by ID');

  console.log('    GET /api/items - Get all items');

  console.log('    GET /api/items/:id - Get item by ID');

  console.log('    GET /api/locations - Get all locations');

  console.log('    GET /api/locations/:id - Get location by ID');

  console.log('📁 Modular structure:');

  console.log('  ├── config/database.js - Database connection');

  console.log('  ├── routes/database.js - Database exploration routes');

  console.log('  └── routes/users.js - User management routes');

});

