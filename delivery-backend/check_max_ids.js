const { Pool } = require('pg');

const pool = new Pool({
  connectionString: "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require"
});

async function getMaxIds() {
  try {
    console.log('=== MAXIMUM ID ANALYSIS ===');
    
    const tables = ['Trip', 'Shipment', 'TripShipmentLink', 'ShipmentLine'];
    
    for (const table of tables) {
      const query = `SELECT MAX(id) as max_id, COUNT(*) as total FROM "${table}"`;
      const result = await pool.query(query);
      const maxId = result.rows[0].max_id || 0;
      const total = result.rows[0].total;
      console.log(`${table}: Max ID = ${maxId}, Total Records = ${total}`);
    }
    
    // Check existing tripId pattern
    const tripQuery = `SELECT "tripId" FROM "Trip" ORDER BY "tripId" DESC LIMIT 5`;
    const tripResult = await pool.query(tripQuery);
    console.log('\nLatest Trip IDs:', tripResult.rows.map(r => r.tripId));
    
    // Check existing shipmentNo pattern
    const shipmentQuery = `SELECT "shipmentNo" FROM "Shipment" ORDER BY "shipmentNo" DESC LIMIT 5`;
    const shipmentResult = await pool.query(shipmentQuery);
    console.log('Latest Shipment Numbers:', shipmentResult.rows.map(r => r.shipmentNo));
    
  } catch (err) {
    console.error('Error:', err.message);
  } finally {
    await pool.end();
  }
}

getMaxIds();
