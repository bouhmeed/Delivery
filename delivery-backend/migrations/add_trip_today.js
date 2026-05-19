require('dotenv').config();
const { Pool } = require('pg');

// Parser la chaîne de connexion pour éviter les problèmes d'authentification
const dbUrl = process.env.DATABASE_URL;
const url = new URL(dbUrl);

const pool = new Pool({
  host: url.hostname,
  port: url.port || 5432,
  database: url.pathname.substring(1),
  user: url.username,
  password: url.password,
  ssl: {
    rejectUnauthorized: false
  }
});

async function addTripToday() {
  const client = await pool.connect();
  
  try {
    console.log('🚀 Ajout d\'un trip pour aujourd\'hui...');
    
    // Insérer le trip pour aujourd'hui
    const insertQuery = `
      INSERT INTO "Trip" (
        "tripDate",
        "depotId",
        "driverId",
        "vehicleId",
        "status",
        "tenantId",
        "tripId"
      ) VALUES (
        '2026-05-18 08:00:00',
        5,
        5,
        2,
        'PLANNING',
        1,
        'TRIP-2026-0518-001'
      ) RETURNING *;
    `;
    
    const result = await client.query(insertQuery);
    
    console.log('✅ Trip ajouté avec succès:');
    console.log(JSON.stringify(result.rows[0], null, 2));
    
    // Vérifier
    const verifyQuery = `SELECT * FROM "Trip" WHERE "tripId" = 'TRIP-2026-0518-001'`;
    const verifyResult = await client.query(verifyQuery);
    
    console.log('\n📋 Vérification:');
    console.log(JSON.stringify(verifyResult.rows[0], null, 2));
    
  } catch (error) {
    console.error('❌ Erreur:', error.message);
    throw error;
  } finally {
    client.release();
    await pool.end();
  }
}

addTripToday();
