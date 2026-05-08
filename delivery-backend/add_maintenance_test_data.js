const { Pool } = require('pg');

const pool = new Pool({
  user: 'postgres',
  host: 'localhost',
  database: 'delivery',
  password: 'postgres',
  port: 5432,
});

async function addMaintenanceData() {
  try {
    console.log('Adding test maintenance data for vehicle 2...');
    
    const maintenanceRecords = [
      {
        vehicleId: 2,
        type: 'Oil Change',
        date: '2026-05-03T10:00:00Z',
        nextMaintenance: '2026-05-13T10:00:00Z', // 5 days from now (URGENT)
        estimatedCost: 85.50,
        notes: 'Regular oil change with filter replacement',
        technician: 'Jean Dupont',
        status: 'completed'
      },
      {
        vehicleId: 2,
        type: 'Brake Inspection',
        date: '2026-04-15T14:00:00Z',
        nextMaintenance: '2026-05-28T14:00:00Z', // 20 days from now (WARNING)
        estimatedCost: 120.00,
        notes: 'Front brake pads check and replacement if needed',
        technician: 'Marie Martin',
        status: 'open'
      },
      {
        vehicleId: 2,
        type: 'Tire Rotation',
        date: '2026-04-01T09:00:00Z',
        nextMaintenance: '2026-06-22T09:00:00Z', // 45 days from now (NORMAL)
        estimatedCost: 45.00,
        notes: 'Rotate all four tires and check pressure',
        technician: 'Pierre Durand',
        status: 'completed'
      },
      {
        vehicleId: 2,
        type: 'Battery Check',
        date: '2026-05-05T11:00:00Z',
        nextMaintenance: '2026-05-12T11:00:00Z', // 4 days from now (URGENT)
        estimatedCost: 65.00,
        notes: 'Battery voltage test and terminal cleaning',
        technician: 'Sophie Bernard',
        status: 'open'
      },
      {
        vehicleId: 2,
        type: 'Air Filter Replacement',
        date: '2026-03-20T08:00:00Z',
        nextMaintenance: '2026-06-20T08:00:00Z', // 43 days from now (NORMAL)
        estimatedCost: 35.00,
        notes: 'Replace engine air filter',
        technician: 'Luc Petit',
        status: 'completed'
      }
    ];

    for (const record of maintenanceRecords) {
      const result = await pool.query(
        `INSERT INTO "VehicleMaintenance" 
         ("vehicleId", "type", "date", "nextMaintenance", "estimatedCost", "notes", "technician", "status")
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
         RETURNING *`,
        [
          record.vehicleId,
          record.type,
          record.date,
          record.nextMaintenance,
          record.estimatedCost,
          record.notes,
          record.technician,
          record.status
        ]
      );
      console.log('✅ Added:', result.rows[0]);
    }

    // Verify the data
    const verifyResult = await pool.query(
      'SELECT * FROM "VehicleMaintenance" WHERE "vehicleId" = 2 ORDER BY "date" DESC'
    );
    
    console.log('\n📊 All maintenance records for vehicle 2:');
    console.log(`Total records: ${verifyResult.rows.length}`);
    verifyResult.rows.forEach((record, index) => {
      console.log(`${index + 1}. ${record.type} - Next: ${record.nextMaintenance?.toISOString().split('T')[0] || 'N/A'} - Status: ${record.status}`);
    });

  } catch (error) {
    console.error('❌ Error adding maintenance data:', error);
  } finally {
    await pool.end();
  }
}

addMaintenanceData();
