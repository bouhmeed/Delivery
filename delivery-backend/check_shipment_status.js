const { Pool } = require('pg');

async function checkShipmentStatus() {
  const pool = new Pool({
    connectionString: process.env.DATABASE_URL || "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require",
    ssl: { rejectUnauthorized: false }
  });

  try {
    const driverId = 5;
    
    console.log('=== ANALYSE DES STATUTS DES LIVRAISONS CHAUFFEUR 5 ===\n');
    
    // Vérifier tous les statuts possibles dans la base
    const statusQuery = `
      SELECT DISTINCT s.status, COUNT(*) as count
      FROM "Shipment" s
      JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId"
      JOIN "Trip" t ON tsl."tripId" = t.id
      WHERE t."driverId" = $1
      GROUP BY s.status
      ORDER BY count DESC
    `;
    
    const statusResult = await pool.query(statusQuery, [driverId]);
    
    console.log('STATUTS DES LIVRAISONS:');
    statusResult.rows.forEach(row => {
      console.log(`  ${row.status}: ${row.count} livraison(s)`);
    });
    
    // Vérifier les statuts dans TripShipmentLink
    const linkStatusQuery = `
      SELECT DISTINCT tsl.status, COUNT(*) as count
      FROM "TripShipmentLink" tsl
      JOIN "Trip" t ON tsl."tripId" = t.id
      WHERE t."driverId" = $1
      GROUP BY tsl.status
      ORDER BY count DESC
    `;
    
    const linkStatusResult = await pool.query(linkStatusQuery, [driverId]);
    
    console.log('\nSTATUTS DANS TripShipmentLink:');
    linkStatusResult.rows.forEach(row => {
      console.log(`  ${row.status}: ${row.count} livraison(s)`);
    });
    
    // Vérifier l'historique complet avec tous les statuts
    const fullHistoryQuery = `
      SELECT 
        s."shipmentNo" as shipment_number,
        s.status as shipment_status,
        tsl.status as link_status,
        t."tripId" as trip_number,
        t."tripDate" as trip_date,
        t.status as trip_status,
        c.name as client_name,
        s.quantity,
        s.uom
      FROM "Trip" t
      JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
      JOIN "Shipment" s ON tsl."shipmentId" = s.id
      LEFT JOIN "Client" c ON s."customerId" = c.id
      WHERE t."driverId" = $1
      ORDER BY t."tripDate" DESC, tsl.sequence ASC
      LIMIT 15
    `;
    
    const fullHistoryResult = await pool.query(fullHistoryQuery, [driverId]);
    
    console.log('\nHISTORIQUE COMPLET (TOUS STATUTS):');
    fullHistoryResult.rows.forEach((item, index) => {
      console.log(`${index + 1}. ${item.shipment_number}`);
      console.log(`   Client: ${item.client_name || 'N/A'}`);
      console.log(`   Statut Shipment: ${item.shipment_status}`);
      console.log(`   Statut Link: ${item.link_status}`);
      console.log(`   Trajet: ${item.trip_number} (${item.trip_date ? item.trip_date.toISOString().split('T')[0] : 'N/A'})`);
      console.log(`   Statut Trajet: ${item.trip_status}`);
      console.log(`   Quantité: ${item.quantity} ${item.uom}`);
      console.log('');
    });
    
    // Vérifier spécifiquement les livraisons non livrées
    const nonDeliveredQuery = `
      SELECT 
        s."shipmentNo" as shipment_number,
        s.status as shipment_status,
        tsl.status as link_status,
        t."tripId" as trip_number,
        c.name as client_name
      FROM "Trip" t
      JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
      JOIN "Shipment" s ON tsl."shipmentId" = s.id
      LEFT JOIN "Client" c ON s."customerId" = c.id
      WHERE t."driverId" = $1 
        AND s.status != 'DELIVERED'
      ORDER BY t."tripDate" DESC
    `;
    
    const nonDeliveredResult = await pool.query(nonDeliveredQuery, [driverId]);
    
    console.log(`\nLIVRAISONS NON LIVRÉES (${nonDeliveredResult.rows.length}):`);
    nonDeliveredResult.rows.forEach((item, index) => {
      console.log(`${index + 1}. ${item.shipment_number}`);
      console.log(`   Client: ${item.client_name || 'N/A'}`);
      console.log(`   Statut Shipment: ${item.shipment_status}`);
      console.log(`   Statut Link: ${item.link_status}`);
      console.log(`   Trajet: ${item.trip_number}`);
      console.log('');
    });
    
  } catch (error) {
    console.error('Erreur:', error.message);
  } finally {
    await pool.end();
  }
}

checkShipmentStatus();
