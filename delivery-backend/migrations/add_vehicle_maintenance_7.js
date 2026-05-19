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

async function addVehicleMaintenance() {
  const client = await pool.connect();
  
  try {
    console.log('🚀 Ajout d\'un enregistrement de maintenance véhicule...');
    
    // Insérer l'enregistrement de maintenance
    const insertQuery = `
      INSERT INTO "VehicleMaintenance" (
        "vehicleId",
        "type",
        "date",
        "nextMaintenance",
        "estimatedCost",
        "notes",
        "technician",
        "status"
      ) VALUES (
        2,
        'Brake Inspection',
        '2026-04-18 19:00:00.000',
        '2026-05-28 19:00:00.000',
        120.0,
        'Front brake pads check and replacement if needed',
        'Marie Martin',
        'open'
      ) RETURNING *;
    `;
    
    const result = await client.query(insertQuery);
    
    console.log('✅ Enregistrement de maintenance ajouté avec succès:');
    console.log(JSON.stringify(result.rows[0], null, 2));
    
    // Vérifier
    const verifyQuery = `SELECT * FROM "VehicleMaintenance" WHERE id = ${result.rows[0].id}`;
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

addVehicleMaintenance();
