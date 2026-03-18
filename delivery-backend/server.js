const express = require('express');
const cors = require('cors');
require('dotenv').config();

// Import routes
const databaseRoutes = require('./routes/database');
const deliveryRoutes = require('./routes/deliveries');
const userRoutes = require('./routes/users');

const app = express();
app.use(cors());
app.use(express.json());

// API Routes
app.use('/api', databaseRoutes);
app.use('/api/deliveries', deliveryRoutes);
app.use('/api/user', userRoutes);

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Server running on http://0.0.0.0:${PORT}`);
  console.log(`🌐 Accessible from Android emulator at: http://10.0.2.2:${PORT}`);
  console.log('📊 Available endpoints:');
  console.log('  GET /api/tables - List all tables');
  console.log('  GET /api/tables/:tableName/structure - Get table structure');
  console.log('  GET /api/:tableName - Get data from any table');
  console.log('  GET /api/deliveries - Get deliveries');
  console.log('  GET /api/deliveries/drivers - Get drivers');
  console.log('  GET /api/user/:email - Get user by email');
  console.log('  GET /api/user/profile?email=... - Get current user profile');
  console.log('📁 Modular structure:');
  console.log('  ├── config/database.js - Database connection');
  console.log('  ├── routes/database.js - Database exploration routes');
  console.log('  ├── routes/deliveries.js - Delivery & driver routes');
  console.log('  └── routes/users.js - User management routes');
});
