const { Pool } = require('pg');
const pool = new Pool({
  connectionString: 'postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require',
  ssl: { rejectUnauthorized: false }
});

async function checkClientColumns() {
  try {
    console.log('🔍 Vérification des colonnes de la table Client...');
    const result = await pool.query('SELECT column_name FROM information_schema.columns WHERE table_name = \'Client\' AND table_schema = \'public\' ORDER BY ordinal_position');
    console.log('Colonnes Client:', result.rows.map(r => r.column_name).join(', '));
    
    await pool.end();
  } catch (error) {
    console.error('❌ Erreur:', error.message);
  }
}

checkClientColumns();
