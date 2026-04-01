const { Pool } = require('pg');

const pool = new Pool({
  connectionString: "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require"
});

async function analyzeTable(tableName, limit = 5) {
  console.log(`\n=== ${tableName} ===`);
  const query = `SELECT * FROM "${tableName}" LIMIT ${limit}`;
  try {
    const result = await pool.query(query);
    if (result.rows.length > 0) {
      console.log('Sample rows:');
      result.rows.forEach((row, i) => {
        console.log(`Row ${i + 1}:`, JSON.stringify(row, null, 2));
      });
    } else {
      console.log('No data found');
    }
  } catch (err) {
    console.error(`Error querying ${tableName}:`, err.message);
  }
}

async function getExistingData() {
  try {
    // Get existing data for reuse
    console.log('\n=== EXISTING DATA FOR REUSE ===');
    
    const clients = await pool.query('SELECT id, name FROM "Client" LIMIT 10');
    console.log('\nClients:', clients.rows);
    
    const locations = await pool.query('SELECT id, name, city FROM "Location" LIMIT 10');
    console.log('\nLocations:', locations.rows);
    
    const vehicles = await pool.query('SELECT id, name, registration FROM "Vehicle" LIMIT 10');
    console.log('\nVehicles:', vehicles.rows);
    
    const items = await pool.query('SELECT id, "itemNo", description FROM "Item" LIMIT 10');
    console.log('\nItems:', items.rows);
    
  } catch (err) {
    console.error('Error getting existing data:', err.message);
  }
}

async function main() {
  try {
    await analyzeTable('Trip');
    await analyzeTable('TripShipmentLink');
    await analyzeTable('Shipment');
    await analyzeTable('ShipmentLine');
    await getExistingData();
  } finally {
    await pool.end();
  }
}

main();
