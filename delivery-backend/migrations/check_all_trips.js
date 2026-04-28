const { Pool } = require('pg');
require('dotenv').config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: true }
});

(async () => {
    try {
        // Vérifier tous les trips créés (100, 101, 102, 103)
        const tripRes = await pool.query(`
            SELECT id, "tripDate", "driverId", "vehicleId", "depotId", status, "tripId"
            FROM "Trip"
            WHERE id IN (100, 101, 102, 103)
            ORDER BY id
        `);
        console.log('TOUS LES TRIPS (100-103):');
        console.table(tripRes.rows);

    } catch (err) {
        console.error('Error:', err);
    } finally {
        await pool.end();
    }
})();
