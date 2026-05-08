const { Pool } = require('pg');
const fs = require('fs');
require('dotenv').config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: true }
});

(async () => {
    try {
        const sql = fs.readFileSync('./migrations/update_shipments_origin_28_april.sql', 'utf8');
        await pool.query(sql);
        console.log('✅ Origin ID mis à jour pour les shipments du 28 avril 2026!');
    } catch (err) {
        console.error('❌ Erreur:', err);
    } finally {
        await pool.end();
    }
})();
