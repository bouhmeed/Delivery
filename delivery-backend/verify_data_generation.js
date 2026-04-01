const { Pool } = require('pg');

const pool = new Pool({
  connectionString: "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require"
});

async function verifyDataGeneration() {
  try {
    console.log('=== DATA GENERATION VERIFICATION ===\n');
    
    // Verify Trip records
    console.log('📋 TRIP RECORDS FOR DRIVER 5 (1-3 April 2026):');
    const tripQuery = `
      SELECT id, "tripId", "tripDate", status 
      FROM "Trip" 
      WHERE "driverId" = 5 
      AND DATE("tripDate") BETWEEN '2026-04-01' AND '2026-04-03'
      ORDER BY "tripDate"
    `;
    const tripResult = await pool.query(tripQuery);
    tripResult.rows.forEach(trip => {
      console.log(`  ✅ ${trip.tripId} - ${trip.tripDate.toISOString().split('T')[0]} - ${trip.status}`);
    });
    
    // Verify Shipment records per trip
    console.log('\n📦 SHIPMENTS PER TRIP:');
    for (const trip of tripResult.rows) {
      const shipmentQuery = `
        SELECT s.id, s."shipmentNo", s.status, COUNT(sl.id) as line_count
        FROM "Shipment" s
        LEFT JOIN "ShipmentLine" sl ON s.id = sl."shipmentId"
        JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId"
        WHERE tsl."tripId" = $1
        GROUP BY s.id, s."shipmentNo", s.status
        ORDER BY s.id
      `;
      const shipmentResult = await pool.query(shipmentQuery, [trip.id]);
      console.log(`  🚚 ${trip.tripId} (${trip.status}): ${shipmentResult.rows.length} shipments`);
      shipmentResult.rows.forEach(shipment => {
        console.log(`    📦 ${shipment.shipmentNo} - ${shipment.status} (${shipment.line_count} lines)`);
      });
    }
    
    // Verify status consistency
    console.log('\n🔍 STATUS CONSISTENCY CHECK:');
    const statusQuery = `
      SELECT 
        t."tripId" as trip_id,
        t.status as trip_status,
        s."shipmentNo" as shipment_no,
        s.status as shipment_status,
        tsl.status as link_status,
        CASE 
          WHEN tsl.status = 'NON_DEMARRE' AND s.status = 'TO_PLAN' THEN '✅'
          WHEN tsl.status = 'EN_COURS' AND s.status = 'EXPEDITION' THEN '✅'
          WHEN tsl.status = 'TERMINE' AND s.status = 'DELIVERED' THEN '✅'
          ELSE '❌'
        END as consistency
      FROM "Trip" t
      JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
      JOIN "Shipment" s ON tsl."shipmentId" = s.id
      WHERE t."driverId" = 5
      AND DATE(t."tripDate") BETWEEN '2026-04-01' AND '2026-04-03'
      ORDER BY t."tripDate", s.id
    `;
    const statusResult = await pool.query(statusQuery);
    
    let allConsistent = true;
    statusResult.rows.forEach(row => {
      const icon = row.consistency === '✅' ? '✅' : '❌';
      console.log(`  ${icon} ${row.trip_id}: ${row.link_status} → ${row.shipment_status}`);
      if (row.consistency === '❌') allConsistent = false;
    });
    
    // Summary
    console.log('\n📊 SUMMARY:');
    console.log(`  ✅ Trips created: ${tripResult.rows.length}`);
    console.log(`  ✅ Shipments created: ${statusResult.rows.length}`);
    
    const lineCountQuery = `
      SELECT COUNT(*) as total_lines
      FROM "ShipmentLine" sl
      JOIN "Shipment" s ON sl."shipmentId" = s.id
      JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId"
      JOIN "Trip" t ON tsl."tripId" = t.id
      WHERE t."driverId" = 5
      AND DATE(t."tripDate") BETWEEN '2026-04-01' AND '2026-04-03'
    `;
    const lineResult = await pool.query(lineCountQuery);
    console.log(`  ✅ Shipment lines created: ${lineResult.rows[0].total_lines}`);
    console.log(`  ✅ Status consistency: ${allConsistent ? 'PERFECT' : 'NEEDS FIX'}`);
    
    console.log('\n🎉 DATA GENERATION COMPLETE!');
    console.log('📱 Ready for testing in Android app');
    
  } catch (err) {
    console.error('❌ Verification error:', err.message);
  } finally {
    await pool.end();
  }
}

verifyDataGeneration();
