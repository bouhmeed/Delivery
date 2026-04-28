const { Pool } = require('pg');
require('dotenv').config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: true }
});

(async () => {
    try {
        // Vérifier le trip du 28 avril
        const tripRes = await pool.query(`
            SELECT id, "tripDate", "driverId", "vehicleId", "depotId", status, "tripId"
            FROM "Trip"
            WHERE id = 102
        `);
        console.log('TRIP 102 (28 avril):');
        console.table(tripRes.rows);

        // Vérifier les shipments liés
        const shipmentRes = await pool.query(`
            SELECT id, "shipmentNo", "customerId", status
            FROM "Shipment"
            WHERE id IN (108, 109, 110, 111)
        `);
        console.log('\nSHIPMENTS 108-111:');
        console.table(shipmentRes.rows);

        // Vérifier les TripShipmentLinks
        const linkRes = await pool.query(`
            SELECT id, "tripId", "shipmentId", status
            FROM "TripShipmentLink"
            WHERE "tripId" = 102
        `);
        console.log('\nTRIP SHIPMENT LINKS pour trip 102:');
        console.table(linkRes.rows);

    } catch (err) {
        console.error('Error:', err);
    } finally {
        await pool.end();
    }
})();
