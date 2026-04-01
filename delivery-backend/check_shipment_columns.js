const { Pool } = require('pg');

const pool = new Pool({
  connectionString: "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require"
});

async function checkShipmentColumns() {
  try {
    console.log('=== SHIPMENT TABLE COLUMNS ===');
    
    const query = `
      SELECT column_name, data_type 
      FROM information_schema.columns 
      WHERE table_name = 'Shipment' 
      ORDER BY ordinal_position
    `;
    
    const result = await pool.query(query);
    result.rows.forEach(row => {
      console.log(`${row.column_name}: ${row.data_type}`);
    });
    
  } catch (err) {
    console.error('❌ Error:', err.message);
  } finally {
    await pool.end();
  }
}

checkShipmentColumns();
