const { Pool } = require('pg');

const pool = new Pool({
  connectionString: "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require"
});

async function checkLocationLinks() {
  try {
    console.log('=== CHECKING LOCATION LINKS ===\n');
    
    // Check shipments and their destinationId
    const shipmentQuery = `
      SELECT s.id, s."shipmentNo", s."destinationId", s."deliveryAddress", s."deliveryCity", s."deliveryZipCode"
      FROM "Shipment" s
      WHERE s.id BETWEEN 52 AND 58
      ORDER BY s.id
    `;
    const shipmentResult = await pool.query(shipmentQuery);
    
    console.log('📦 Shipments (52-58):');
    shipmentResult.rows.forEach(row => {
      console.log(`  Shipment ${row.id}: destinationId = ${row.destinationId}, deliveryAddress = '${row.deliveryAddress}'`);
    });
    
    // Check locations table
    const locationQuery = `
      SELECT l.id, l.name, l.address, l.city, l."postalCode"
      FROM "Location" l
      ORDER BY l.id
    `;
    const locationResult = await pool.query(locationQuery);
    
    console.log('\n📍 Available Locations:');
    locationResult.rows.forEach(row => {
      console.log(`  Location ${row.id}: ${row.name} - ${row.address}, ${row.city} ${row.postalCode}`);
    });
    
    // Check the actual join
    const joinQuery = `
      SELECT
        s.id as shipment_id,
        s."shipmentNo",
        s."destinationId",
        l.id as location_id,
        l.address as "fullAddress",
        l.city as "locationCity",
        l."postalCode" as "locationPostalCode"
      FROM "Shipment" s
      LEFT JOIN "Location" l ON s."destinationId" = l.id
      WHERE s.id BETWEEN 52 AND 58
      ORDER BY s.id
    `;
    const joinResult = await pool.query(joinQuery);
    
    console.log('\n🔗 Join Results:');
    joinResult.rows.forEach(row => {
      console.log(`  Shipment ${row.shipment_id} → Location ${row.location_id}: ${row.fullAddress}, ${row.locationCity} ${row.locationPostalCode}`);
    });
    
  } catch (err) {
    console.error('❌ Error:', err.message);
  } finally {
    await pool.end();
  }
}

checkLocationLinks();
