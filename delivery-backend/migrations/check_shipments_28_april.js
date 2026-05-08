const { Pool } = require('pg');
require('dotenv').config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: true }
});

(async () => {
    try {
        // Vérifier les shipments pour driver 5 le 28 avril 2026
        const shipmentRes = await pool.query(`
            SELECT id, "shipmentNo", "customerId", "originId", "destinationId", status, "deliveryAddress", "deliveryCity"
            FROM "Shipment"
            WHERE "driverId" = 5 AND "createdAt" >= '2026-04-28' AND "createdAt" < '2026-04-29'
            ORDER BY id
        `);
        console.log('SHIPMENTS pour driver 5 le 28 avril 2026:');
        console.table(shipmentRes.rows);

        // Vérifier la location ID 8
        const locationRes = await pool.query(`
            SELECT id, name, address, city, "postalCode"
            FROM "Location"
            WHERE id = 8
        `);
        console.log('\nLOCATION ID 8:');
        console.table(locationRes.rows);

    } catch (err) {
        console.error('Error:', err);
    } finally {
        await pool.end();
    }
})();
