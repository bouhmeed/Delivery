const { Pool } = require('pg');
require('dotenv').config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: true }
});

(async () => {
    try {
        // Vérifier les shipments liés au trip 102 (28 avril)
        const shipmentRes = await pool.query(`
            SELECT s.id, s."shipmentNo", s."customerId", s."originId", s."destinationId", s.status, s."deliveryAddress", s."deliveryCity", s."createdAt"
            FROM "Shipment" s
            JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId"
            WHERE tsl."tripId" = 102
            ORDER BY s.id
        `);
        console.log('SHIPMENTS pour Trip 102 (28 avril):');
        console.table(shipmentRes.rows);

    } catch (err) {
        console.error('Error:', err);
    } finally {
        await pool.end();
    }
})();
