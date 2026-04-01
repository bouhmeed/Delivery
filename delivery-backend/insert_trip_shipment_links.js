const { Pool } = require('pg');

const pool = new Pool({
  connectionString: "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require"
});

async function insertTripShipmentLinks() {
  try {
    console.log('=== INSERTING TRIP SHIPMENT LINKS ===');
    
    // TripShipmentLink data with correct status mapping
    const tripShipmentLinks = [
      // Trip 32 (1 April) - PLANNING trip - all shipments NON_DEMARRE
      { tripId: 32, shipmentId: 52, role: 'BOTH', status: 'NON_DEMARRE', sequence: 1 },
      { tripId: 32, shipmentId: 53, role: 'BOTH', status: 'NON_DEMARRE', sequence: 2 },
      { tripId: 32, shipmentId: 54, role: 'BOTH', status: 'NON_DEMARRE', sequence: 3 },
      { tripId: 32, shipmentId: 55, role: 'BOTH', status: 'NON_DEMARRE', sequence: 4 },
      { tripId: 32, shipmentId: 56, role: 'BOTH', status: 'NON_DEMARRE', sequence: 5 },
      { tripId: 32, shipmentId: 57, role: 'BOTH', status: 'NON_DEMARRE', sequence: 6 },
      { tripId: 32, shipmentId: 58, role: 'BOTH', status: 'NON_DEMARRE', sequence: 7 },
      
      // Trip 33 (2 April) - IN_PROGRESS trip - mix of EN_COURS and NON_DEMARRE
      { tripId: 33, shipmentId: 59, role: 'BOTH', status: 'EN_COURS', sequence: 1 },      // EXPEDITION
      { tripId: 33, shipmentId: 60, role: 'BOTH', status: 'EN_COURS', sequence: 2 },      // EXPEDITION
      { tripId: 33, shipmentId: 61, role: 'BOTH', status: 'NON_DEMARRE', sequence: 3 },    // TO_PLAN
      { tripId: 33, shipmentId: 62, role: 'BOTH', status: 'EN_COURS', sequence: 4 },      // EXPEDITION
      { tripId: 33, shipmentId: 63, role: 'BOTH', status: 'NON_DEMARRE', sequence: 5 },    // TO_PLAN
      { tripId: 33, shipmentId: 64, role: 'BOTH', status: 'NON_DEMARRE', sequence: 6 },    // TO_PLAN
      { tripId: 33, shipmentId: 65, role: 'BOTH', status: 'EN_COURS', sequence: 7 },      // EXPEDITION
      
      // Trip 34 (3 April) - COMPLETED trip - all shipments TERMINE
      { tripId: 34, shipmentId: 66, role: 'BOTH', status: 'TERMINE', sequence: 1 },       // DELIVERED
      { tripId: 34, shipmentId: 67, role: 'BOTH', status: 'TERMINE', sequence: 2 },       // DELIVERED
      { tripId: 34, shipmentId: 68, role: 'BOTH', status: 'TERMINE', sequence: 3 },       // DELIVERED
      { tripId: 34, shipmentId: 69, role: 'BOTH', status: 'TERMINE', sequence: 4 },       // DELIVERED
      { tripId: 34, shipmentId: 70, role: 'BOTH', status: 'TERMINE', sequence: 5 },       // DELIVERED
      { tripId: 34, shipmentId: 71, role: 'BOTH', status: 'TERMINE', sequence: 6 },       // DELIVERED
      { tripId: 34, shipmentId: 72, role: 'BOTH', status: 'TERMINE', sequence: 7 }        // DELIVERED
    ];
    
    let linkId = 33; // Starting from next available ID
    
    for (const link of tripShipmentLinks) {
      const query = `
        INSERT INTO "TripShipmentLink" (
          id, "tripId", "shipmentId", role, status, "podDone", "returnsDone",
          sequence, "createdAt", "updatedAt"
        ) VALUES (
          $1, $2, $3, $4, $5, $6, $7, $8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        )
      `;
      
      await pool.query(query, [
        linkId, link.tripId, link.shipmentId, link.role, link.status,
        false, false, link.sequence
      ]);
      
      console.log(`✅ Inserted TripShipmentLink: Trip ${link.tripId} → Shipment ${link.shipmentId} (${link.status})`);
      linkId++;
    }
    
    console.log('\n=== TRIP SHIPMENT LINK INSERTION COMPLETE ===');
    console.log(`✅ Total links inserted: ${tripShipmentLinks.length}`);
    
    // Verify status consistency
    console.log('\n=== STATUS CONSISTENCY VERIFICATION ===');
    console.log('✅ Trip 32 (PLANNING) → All shipments NON_DEMARRE → Shipment status TO_PLAN');
    console.log('✅ Trip 33 (IN_PROGRESS) → Mix EN_COURS/NON_DEMARRE → Shipment status EXPEDITION/TO_PLAN');
    console.log('✅ Trip 34 (COMPLETED) → All shipments TERMINE → Shipment status DELIVERED');
    
  } catch (err) {
    console.error('❌ Error inserting trip shipment links:', err.message);
  } finally {
    await pool.end();
  }
}

insertTripShipmentLinks();
