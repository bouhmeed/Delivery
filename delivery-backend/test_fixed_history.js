const { Pool } = require('pg');

async function testFixedHistory() {
  const pool = new Pool({
    connectionString: process.env.DATABASE_URL || "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require",
    ssl: { rejectUnauthorized: false }
  });

  try {
    const driverId = 5;
    
    console.log('=== TEST API HISTORIQUE CORRIGÉE ===\n');
    
    // Utiliser la requête corrigée (sans le filtre restrictif)
    const fixedHistoryQuery = `
      SELECT 
        t."tripId" as trip_number,
        t."tripDate" as trip_date,
        t.status as trip_status,
        s."shipmentNo" as shipment_number,
        s.status as shipment_status,
        s.description as shipment_description,
        s.quantity,
        s.uom,
        c.name as client_name,
        origin_loc.name as origin_name,
        dest_loc.name as destination_name,
        v.name as vehicle_name,
        v.type as vehicle_type,
        tsl.sequence,
        tsl."podDone" as pod_done
      FROM "Trip" t
      JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
      JOIN "Shipment" s ON tsl."shipmentId" = s.id
      LEFT JOIN "Client" c ON s."customerId" = c.id
      LEFT JOIN "Location" origin_loc ON s."originId" = origin_loc.id
      LEFT JOIN "Location" dest_loc ON s."destinationId" = dest_loc.id
      LEFT JOIN "Vehicle" v ON t."vehicleId" = v.id
      WHERE t."driverId" = $1
      ORDER BY t."tripDate" DESC, tsl.sequence ASC
      LIMIT 15
    `;
    
    const fixedResult = await pool.query(fixedHistoryQuery, [driverId]);
    
    console.log('HISTORIQUE COMPLET AVEC TOUS LES STATUTS:');
    console.log(`Nombre d'entrées: ${fixedResult.rows.length}\n`);
    
    // Grouper par statut pour voir la distribution
    const statusGroups = {};
    fixedResult.rows.forEach(item => {
      const status = item.shipment_status || 'UNKNOWN';
      if (!statusGroups[status]) {
        statusGroups[status] = [];
      }
      statusGroups[status].push(item);
    });
    
    Object.entries(statusGroups).forEach(([status, items]) => {
      console.log(`=== ${status} (${items.length} livraisons) ===`);
      items.forEach((item, index) => {
        console.log(`${index + 1}. ${item.shipment_number}`);
        console.log(`   Client: ${item.client_name || 'N/A'}`);
        console.log(`   Trajet: ${item.trip_number || 'N/A'} (${item.trip_date ? item.trip_date.toISOString().split('T')[0] : 'N/A'})`);
        console.log(`   Statut trajet: ${item.trip_status}`);
        console.log(`   Quantité: ${item.quantity} ${item.uom}`);
        console.log(`   Véhicule: ${item.vehicle_name} (${item.vehicle_type})`);
        console.log(`   Route: ${item.origin_name} → ${item.destination_name}`);
        console.log(`   POD: ${item.pod_done ? 'Oui' : 'Non'}`);
        console.log('');
      });
    });
    
    // Compter par trajet
    const tripGroups = {};
    fixedResult.rows.forEach(item => {
      const tripKey = item.trip_number || 'UNKNOWN';
      if (!tripGroups[tripKey]) {
        tripGroups[tripKey] = {
          trip_date: item.trip_date,
          trip_status: item.trip_status,
          deliveries: []
        };
      }
      tripGroups[tripKey].deliveries.push(item);
    });
    
    console.log('=== RÉSUMÉ PAR TRAJET ===');
    Object.entries(tripGroups).forEach(([tripKey, trip]) => {
      const statusCounts = {};
      trip.deliveries.forEach(delivery => {
        const status = delivery.shipment_status;
        statusCounts[status] = (statusCounts[status] || 0) + 1;
      });
      
      console.log(`${tripKey} (${trip.trip_date ? trip.trip_date.toISOString().split('T')[0] : 'N/A'}) - ${trip.trip_status}`);
      console.log(`  Total: ${trip.deliveries.length} livraisons`);
      Object.entries(statusCounts).forEach(([status, count]) => {
        console.log(`  ${status}: ${count}`);
      });
      console.log('');
    });
    
  } catch (error) {
    console.error('Erreur:', error.message);
  } finally {
    await pool.end();
  }
}

testFixedHistory();
