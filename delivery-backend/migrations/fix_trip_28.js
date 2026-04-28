const { Pool } = require('pg');
const fs = require('fs');
require('dotenv').config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: true }
});

(async () => {
    try {
        const sql = fs.readFileSync('./migrations/fix_trip_28.sql', 'utf8');
        await pool.query(sql);
        console.log('✅ Trip 28 avril corrigé avec succès!');
    } catch (err) {
        console.error('❌ Erreur:', err);
    } finally {
        await pool.end();
    }
})();
