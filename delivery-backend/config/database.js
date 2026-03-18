const { Pool } = require('pg');

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: {
    rejectUnauthorized: true
  }
});

// Test database connection
pool.query('SELECT NOW()')
  .then(res => {
    console.log('✅ Database connected successfully');
    console.log('Current time:', res.rows[0].now);
  })
  .catch(err => {
    console.error('❌ Database connection error:', err.message);
  });

module.exports = pool;
