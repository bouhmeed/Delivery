const { Pool } = require('pg');

const pool = new Pool({
  connectionString: "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require"
});

async function insertTrips() {
  try {
    console.log('=== INSERTING TRIP RECORDS ===');
    
    // Trip data for driver 5, 1-3 April 2026
    const trips = [
      {
        id: 32,
        tripId: 'TRIP-2026-515',
        tripDate: '2026-04-01T08:00:00.000Z',
        depotId: 3, // Entrepôt Principal, Lyon
        driverId: 5,
        vehicleId: 3, // Renault Master, AB-123-CD
        status: 'PLANNING',
        tenantId: 1
      },
      {
        id: 33,
        tripId: 'TRIP-2026-516',
        tripDate: '2026-04-02T08:00:00.000Z',
        depotId: 3, // Entrepôt Principal, Lyon
        driverId: 5,
        vehicleId: 3, // Renault Master, AB-123-CD
        status: 'IN_PROGRESS',
        tenantId: 1
      },
      {
        id: 34,
        tripId: 'TRIP-2026-517',
        tripDate: '2026-04-03T08:00:00.000Z',
        depotId: 3, // Entrepôt Principal, Lyon
        driverId: 5,
        vehicleId: 3, // Renault Master, AB-123-CD
        status: 'COMPLETED',
        tenantId: 1
      }
    ];
    
    for (const trip of trips) {
      const query = `
        INSERT INTO "Trip" (
          id, "tripId", "tripDate", "depotId", "driverId", "vehicleId", 
          status, "tenantId", "createdAt"
        ) VALUES (
          $1, $2, $3, $4, $5, $6, $7, $8, CURRENT_TIMESTAMP
        )
      `;
      
      await pool.query(query, [
        trip.id, trip.tripId, trip.tripDate, trip.depotId, 
        trip.driverId, trip.vehicleId, trip.status, trip.tenantId
      ]);
      
      console.log(`✅ Inserted Trip: ${trip.tripId} (${trip.status})`);
    }
    
    console.log('\n=== TRIP INSERTION COMPLETE ===');
    
  } catch (err) {
    console.error('❌ Error inserting trips:', err.message);
  } finally {
    await pool.end();
  }
}

insertTrips();
