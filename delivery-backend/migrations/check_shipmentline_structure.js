const { Pool } = require('pg');
require('dotenv').config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: true }
});

(async () => {
    try {
        console.log('🔍 VÉRIFICATION DE LA STRUCTURE DE SHIPMENTLINE\n');
        
        // Vérifier la structure de la table ShipmentLine
        const structureRes = await pool.query(`
            SELECT column_name, data_type
            FROM information_schema.columns
            WHERE table_name = 'ShipmentLine'
            ORDER BY ordinal_position
        `);
        
        console.log('📋 STRUCTURE DE LA TABLE SHIPMENTLINE:');
        console.table(structureRes.rows);
        
        // Vérifier quelques enregistrements
        const sampleRes = await pool.query(`
            SELECT * FROM "ShipmentLine" LIMIT 3
        `);
        
        console.log('\n📋 EXEMPLES D\'ENREGISTREMENTS:');
        console.table(sampleRes.rows);
        
    } catch (err) {
        console.error('❌ Erreur:', err);
    } finally {
        await pool.end();
    }
})();
