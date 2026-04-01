const { Pool } = require('pg');

const pool = new Pool({
  connectionString: "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require"
});

async function insertShipments() {
  try {
    console.log('=== INSERTING SHIPMENT RECORDS ===');
    
    // Shipment data - 7 shipments per trip
    const shipments = [
      // Trip 32 (1 April) - PLANNING trip - shipments should be TO_PLAN
      {
        id: 52,
        shipmentNo: 'EXP-2026-430',
        customerId: 1, // Client A
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 4, // Client Default, Paris
        priority: 'HIGH',
        requestedPickup: '2026-04-01T08:00:00.000Z',
        requestedDelivery: '2026-04-01T18:00:00.000Z',
        status: 'TO_PLAN',
        description: 'Livraison Client A - Produits standards',
        quantity: 120,
        uom: 'PCS',
        weight: 280,
        volume: 1.3,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 53,
        shipmentNo: 'EXP-2026-431',
        customerId: 2, // Client B
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 4, // Client Default, Paris
        priority: 'MEDIUM',
        requestedPickup: '2026-04-01T08:30:00.000Z',
        requestedDelivery: '2026-04-01T17:30:00.000Z',
        status: 'TO_PLAN',
        description: 'Livraison Client B - Cartons et palettes',
        quantity: 75,
        uom: 'CTN',
        weight: 450,
        volume: 2.1,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 54,
        shipmentNo: 'EXP-2026-432',
        customerId: 5, // Banque Internationale Genève
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 7, // Genève - Siège Suisse
        priority: 'HIGH',
        requestedPickup: '2026-04-01T09:00:00.000Z',
        requestedDelivery: '2026-04-01T16:00:00.000Z',
        status: 'TO_PLAN',
        description: 'Livraison Banque - Documents et matériel informatique',
        quantity: 25,
        uom: 'PCS',
        weight: 85,
        volume: 0.4,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 55,
        shipmentNo: 'EXP-2026-433',
        customerId: 6, // Swiss Pharma AG
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 8, // Zurich - Dépôt Suisse
        priority: 'HIGH',
        requestedPickup: '2026-04-01T09:30:00.000Z',
        requestedDelivery: '2026-04-01T15:30:00.000Z',
        status: 'TO_PLAN',
        description: 'Livraison Swiss Pharma - Matériel médical',
        quantity: 40,
        uom: 'PCS',
        weight: 120,
        volume: 0.8,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 56,
        shipmentNo: 'EXP-2026-434',
        customerId: 7, // Lausanne Technologies
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 9, // Lausanne - Agence
        priority: 'MEDIUM',
        requestedPickup: '2026-04-01T10:00:00.000Z',
        requestedDelivery: '2026-04-01T17:00:00.000Z',
        status: 'TO_PLAN',
        description: 'Livraison Lausanne Tech - Électronique',
        quantity: 15,
        uom: 'PCS',
        weight: 95,
        volume: 0.6,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 57,
        shipmentNo: 'EXP-2026-435',
        customerId: 8, // Bâle Industries SA
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 10, // Bâle - Entrepôt
        priority: 'LOW',
        requestedPickup: '2026-04-01T10:30:00.000Z',
        requestedDelivery: '2026-04-01T18:30:00.000Z',
        status: 'TO_PLAN',
        description: 'Livraison Bâle Industries - Outillage',
        quantity: 30,
        uom: 'PCS',
        weight: 180,
        volume: 1.2,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 58,
        shipmentNo: 'EXP-2026-436',
        customerId: 9, // TechnoPlus France
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 4, // Client Default, Paris
        priority: 'MEDIUM',
        requestedPickup: '2026-04-01T11:00:00.000Z',
        requestedDelivery: '2026-04-01T19:00:00.000Z',
        status: 'TO_PLAN',
        description: 'Livraison TechnoPlus - Mobilier de bureau',
        quantity: 8,
        uom: 'PCS',
        weight: 220,
        volume: 1.8,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      
      // Trip 33 (2 April) - IN_PROGRESS trip - mix of EXPEDITION and TO_PLAN
      {
        id: 59,
        shipmentNo: 'EXP-2026-437',
        customerId: 1, // Client A
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 4, // Client Default, Paris
        priority: 'HIGH',
        requestedPickup: '2026-04-02T08:00:00.000Z',
        requestedDelivery: '2026-04-02T18:00:00.000Z',
        status: 'EXPEDITION', // Currently in progress
        description: 'Livraison Client A - Produits alimentaires',
        quantity: 95,
        uom: 'PCS',
        weight: 310,
        volume: 1.5,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 60,
        shipmentNo: 'EXP-2026-438',
        customerId: 3, // Client A
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 4, // Client Default, Paris
        priority: 'MEDIUM',
        requestedPickup: '2026-04-02T08:30:00.000Z',
        requestedDelivery: '2026-04-02T17:30:00.000Z',
        status: 'EXPEDITION',
        description: 'Livraison Client A - Textile et vêtements',
        quantity: 60,
        uom: 'PCS',
        weight: 140,
        volume: 0.9,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 61,
        shipmentNo: 'EXP-2026-439',
        customerId: 5, // Banque Internationale Genève
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 7, // Genève - Siège Suisse
        priority: 'HIGH',
        requestedPickup: '2026-04-02T09:00:00.000Z',
        requestedDelivery: '2026-04-02T16:00:00.000Z',
        status: 'TO_PLAN', // Still to be delivered
        description: 'Livraison Banque - Livres et documents',
        quantity: 50,
        uom: 'PCS',
        weight: 75,
        volume: 0.3,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 62,
        shipmentNo: 'EXP-2026-440',
        customerId: 10, // SantéPlus Médical
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 8, // Zurich - Dépôt Suisse
        priority: 'HIGH',
        requestedPickup: '2026-04-02T09:30:00.000Z',
        requestedDelivery: '2026-04-02T15:30:00.000Z',
        status: 'EXPEDITION',
        description: 'Livraison SantéPlus - Matériel médical',
        quantity: 35,
        uom: 'PCS',
        weight: 165,
        volume: 1.1,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 63,
        shipmentNo: 'EXP-2026-441',
        customerId: 7, // Lausanne Technologies
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 9, // Lausanne - Agence
        priority: 'MEDIUM',
        requestedPickup: '2026-04-02T10:00:00.000Z',
        requestedDelivery: '2026-04-02T17:00:00.000Z',
        status: 'TO_PLAN',
        description: 'Livraison Lausanne Tech - Ordinateurs',
        quantity: 12,
        uom: 'PCS',
        weight: 85,
        volume: 0.7,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 64,
        shipmentNo: 'EXP-2026-442',
        customerId: 8, // Bâle Industries SA
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 10, // Bâle - Entrepôt
        priority: 'LOW',
        requestedPickup: '2026-04-02T10:30:00.000Z',
        requestedDelivery: '2026-04-02T18:30:00.000Z',
        status: 'TO_PLAN',
        description: 'Livraison Bâle Industries - Boîtes à outils',
        quantity: 20,
        uom: 'PCS',
        weight: 125,
        volume: 0.8,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 65,
        shipmentNo: 'EXP-2026-443',
        customerId: 9, // TechnoPlus France
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 4, // Client Default, Paris
        priority: 'MEDIUM',
        requestedPickup: '2026-04-02T11:00:00.000Z',
        requestedDelivery: '2026-04-02T19:00:00.000Z',
        status: 'EXPEDITION',
        description: 'Livraison TechnoPlus - Chaises de bureau',
        quantity: 6,
        uom: 'PCS',
        weight: 195,
        volume: 1.6,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      
      // Trip 34 (3 April) - COMPLETED trip - all DELIVERED
      {
        id: 66,
        shipmentNo: 'EXP-2026-444',
        customerId: 2, // Client B
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 4, // Client Default, Paris
        priority: 'HIGH',
        requestedPickup: '2026-04-03T08:00:00.000Z',
        requestedDelivery: '2026-04-03T18:00:00.000Z',
        status: 'DELIVERED', // Completed
        description: 'Livraison Client B - Produits standards',
        quantity: 85,
        uom: 'PCS',
        weight: 290,
        volume: 1.4,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 67,
        shipmentNo: 'EXP-2026-445',
        customerId: 4, // Client B
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 4, // Client Default, Paris
        priority: 'MEDIUM',
        requestedPickup: '2026-04-03T08:30:00.000Z',
        requestedDelivery: '2026-04-03T17:30:00.000Z',
        status: 'DELIVERED',
        description: 'Livraison Client B - Cartons',
        quantity: 45,
        uom: 'CTN',
        weight: 380,
        volume: 2.3,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 68,
        shipmentNo: 'EXP-2026-446',
        customerId: 6, // Swiss Pharma AG
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 8, // Zurich - Dépôt Suisse
        priority: 'HIGH',
        requestedPickup: '2026-04-03T09:00:00.000Z',
        requestedDelivery: '2026-04-03T16:00:00.000Z',
        status: 'DELIVERED',
        description: 'Livraison Swiss Pharma - Stéthoscopes',
        quantity: 28,
        uom: 'PCS',
        weight: 110,
        volume: 0.5,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 69,
        shipmentNo: 'EXP-2026-447',
        customerId: 10, // SantéPlus Médical
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 8, // Zurich - Dépôt Suisse
        priority: 'HIGH',
        requestedPickup: '2026-04-03T09:30:00.000Z',
        requestedDelivery: '2026-04-03T15:30:00.000Z',
        status: 'DELIVERED',
        description: 'Livraison SantéPlus - Conserves alimentaires',
        quantity: 65,
        uom: 'PCS',
        weight: 195,
        volume: 1.2,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 70,
        shipmentNo: 'EXP-2026-448',
        customerId: 7, // Lausanne Technologies
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 9, // Lausanne - Agence
        priority: 'MEDIUM',
        requestedPickup: '2026-04-03T10:00:00.000Z',
        requestedDelivery: '2026-04-03T17:00:00.000Z',
        status: 'DELIVERED',
        description: 'Livraison Lausanne Tech - Vêtements',
        quantity: 40,
        uom: 'PCS',
        weight: 88,
        volume: 0.6,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 71,
        shipmentNo: 'EXP-2026-449',
        customerId: 8, // Bâle Industries SA
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 10, // Bâle - Entrepôt
        priority: 'LOW',
        requestedPickup: '2026-04-03T10:30:00.000Z',
        requestedDelivery: '2026-04-03T18:30:00.000Z',
        status: 'DELIVERED',
        description: 'Livraison Bâle Industries - Livres scolaires',
        quantity: 75,
        uom: 'PCS',
        weight: 135,
        volume: 0.9,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      },
      {
        id: 72,
        shipmentNo: 'EXP-2026-450',
        customerId: 9, // TechnoPlus France
        type: 'OUTBOUND',
        originId: 3, // Entrepôt Principal, Lyon
        destinationId: 4, // Client Default, Paris
        priority: 'MEDIUM',
        requestedPickup: '2026-04-03T11:00:00.000Z',
        requestedDelivery: '2026-04-03T19:00:00.000Z',
        status: 'DELIVERED',
        description: 'Livraison TechnoPlus - Matériel divers',
        quantity: 22,
        uom: 'PCS',
        weight: 168,
        volume: 1.1,
        driverId: 5,
        vehicleId: 3,
        tenantId: 1,
        createdById: 1
      }
    ];
    
    for (const shipment of shipments) {
      const query = `
        INSERT INTO "Shipment" (
          id, "shipmentNo", "customerId", type, "originId", "destinationId",
          priority, "requestedPickup", "requestedDelivery", status, description,
          quantity, uom, weight, volume, "driverId", "vehicleId", "tenantId",
          "createdAt", "updatedAt", "createdById", "deliveryCountry"
        ) VALUES (
          $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18,
          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, $19, 'France'
        )
      `;
      
      await pool.query(query, [
        shipment.id, shipment.shipmentNo, shipment.customerId, shipment.type,
        shipment.originId, shipment.destinationId, shipment.priority,
        shipment.requestedPickup, shipment.requestedDelivery, shipment.status,
        shipment.description, shipment.quantity, shipment.uom, shipment.weight,
        shipment.volume, shipment.driverId, shipment.vehicleId, shipment.tenantId,
        shipment.createdById
      ]);
      
      console.log(`✅ Inserted Shipment: ${shipment.shipmentNo} (${shipment.status})`);
    }
    
    console.log('\n=== SHIPMENT INSERTION COMPLETE ===');
    console.log(`✅ Total shipments inserted: ${shipments.length}`);
    
  } catch (err) {
    console.error('❌ Error inserting shipments:', err.message);
  } finally {
    await pool.end();
  }
}

insertShipments();
