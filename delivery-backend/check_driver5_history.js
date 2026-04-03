const { Pool } = require('pg');

async function checkDriver5History() {
  const pool = new Pool({
    connectionString: process.env.DATABASE_URL || "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require",
    ssl: { rejectUnauthorized: false }
  });

  try {
    const driverId = 5;
    
    // Vérifier les trajets du chauffeur 5
    const tripsQuery = `
      SELECT t.id, t."tripId", t."tripDate", t.status, COUNT(s.id) as shipment_count
      FROM "Trip" t
      LEFT JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
      LEFT JOIN "Shipment" s ON tsl."shipmentId" = s.id
      WHERE t."driverId" = $1
      GROUP BY t.id, t."tripId", t."tripDate", t.status
      ORDER BY t."tripDate" DESC
    `;
    
    const tripsResult = await pool.query(tripsQuery, [driverId]);
    
    console.log('=== TRAJETS DU CHAUFFEUR 5 ===');
    console.log(`Nombre total de trajets: ${tripsResult.rows.length}`);
    
    tripsResult.rows.forEach((trip, index) => {
      console.log(`${index + 1}. Trajet: ${trip.tripId || 'TRIP-' + trip.id}`);
      console.log(`   Date: ${trip.trip_date}`);
      console.log(`   Statut: ${trip.status}`);
      console.log(`   Livraisons: ${trip.shipment_count}`);
      console.log('');
    });
    
    // Vérifier l'historique selon les critères de l'API
    const historyQuery = `
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
        AND (t.status = 'COMPLETED' OR t."tripDate" < CURRENT_DATE)
      ORDER BY t."tripDate" DESC, tsl.sequence ASC
      LIMIT 10
    `;
    
    const historyResult = await pool.query(historyQuery, [driverId]);
    
    console.log('=== HISTORIQUE VISIBLE DANS L\'APP ===');
    console.log(`Nombre d'entrées dans l'historique: ${historyResult.rows.length}`);
    
    historyResult.rows.forEach((item, index) => {
      console.log(`${index + 1}. ${item.shipment_number || 'SHIP-' + item.id}`);
      console.log(`   Client: ${item.client_name || 'N/A'}`);
      console.log(`   Trajet: ${item.trip_number || 'N/A'} (${item.trip_date ? item.trip_date.toISOString().split('T')[0] : 'N/A'})`);
      console.log(`   Statut livraison: ${item.shipment_status}`);
      console.log(`   Quantité: ${item.quantity} ${item.uom}`);
      console.log(`   Véhicule: ${item.vehicle_name} (${item.vehicle_type})`);
      console.log(`   Route: ${item.origin_name} → ${item.destination_name}`);
      console.log(`   POD: ${item.pod_done ? 'Oui' : 'Non'}`);
      console.log('');
    });
    
    // Vérifier les statistiques
    const statsQuery = `
      SELECT 
        COUNT(DISTINCT t.id) as total_trips,
        COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) as completed_trips,
        COUNT(DISTINCT s.id) as total_shipments,
        COUNT(DISTINCT CASE WHEN s.status = 'DELIVERED' THEN s.id END) as delivered_shipments,
        COUNT(DISTINCT CASE WHEN s.status = 'TO_PLAN' THEN s.id END) as pending_shipments,
        COUNT(DISTINCT CASE WHEN s.status = 'EXPEDITION' THEN s.id END) as expedition_shipments,
        COALESCE(SUM(s.quantity), 0) as total_quantity,
        MAX(t."tripDate") as last_trip_date
      FROM "Trip" t
      LEFT JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
      LEFT JOIN "Shipment" s ON tsl."shipmentId" = s.id
      WHERE t."driverId" = $1 
        AND t."tripDate" >= CURRENT_DATE - INTERVAL '30 days'
    `;
    
    const statsResult = await pool.query(statsQuery, [driverId]);
    const stats = statsResult.rows[0];
    
    console.log('=== STATISTIQUES (30 derniers jours) ===');
    console.log(`Total trajets: ${stats.total_trips}`);
    console.log(`Trajets complétés: ${stats.completed_trips}`);
    console.log(`Total livraisons: ${stats.total_shipments}`);
    console.log(`Livraisons livrées: ${stats.delivered_shipments}`);
    console.log(`Livraisons à planifier: ${stats.pending_shipments}`);
    console.log(`Livraisons en expédition: ${stats.expedition_shipments}`);
    console.log(`Quantité totale: ${stats.total_quantity}`);
    console.log(`Dernier trajet: ${stats.last_trip_date ? stats.last_trip_date.toISOString().split('T')[0] : 'N/A'}`);
    
  } catch (error) {
    console.error('Erreur:', error.message);
  } finally {
    await pool.end();
  }
}

checkDriver5History();
