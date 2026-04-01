const { Pool } = require('pg');

const pool = new Pool({
  connectionString: "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require"
});

async function insertShipmentLines() {
  try {
    console.log('=== INSERTING SHIPMENT LINE RECORDS ===');
    
    // ShipmentLine data using existing items
    const shipmentLines = [
      // Trip 32 Shipments (1 April - TO_PLAN)
      { shipmentId: 52, itemId: 1, description: 'Produit A - Boîte standard', quantity: 120, uom: 'PCS', weight: 280, volume: 1.3 },
      { shipmentId: 53, itemId: 2, description: 'Produit B - Palette', quantity: 15, uom: 'PAL', weight: 450, volume: 2.1 },
      { shipmentId: 53, itemId: 3, description: 'Produit C - Carton', quantity: 60, uom: 'CTN', weight: 450, volume: 2.1 },
      { shipmentId: 54, itemId: 5, description: 'Électronique - Ordinateurs portables', quantity: 25, uom: 'PCS', weight: 85, volume: 0.4 },
      { shipmentId: 55, itemId: 6, description: 'Matériel médical - Stéthoscopes', quantity: 40, uom: 'PCS', weight: 120, volume: 0.8 },
      { shipmentId: 56, itemId: 8, description: 'Textile - Vêtements', quantity: 15, uom: 'PCS', weight: 95, volume: 0.6 },
      { shipmentId: 57, itemId: 10, description: 'Outillage - Boîte à outils', quantity: 30, uom: 'PCS', weight: 180, volume: 1.2 },
      { shipmentId: 58, itemId: 9, description: 'Mobilier - Chaises de bureau', quantity: 8, uom: 'PCS', weight: 220, volume: 1.8 },
      
      // Trip 33 Shipments (2 April - mix EXPEDITION/TO_PLAN)
      { shipmentId: 59, itemId: 7, description: 'Produits alimentaires - Conserves', quantity: 95, uom: 'PCS', weight: 310, volume: 1.5 },
      { shipmentId: 60, itemId: 8, description: 'Textile - Vêtements', quantity: 60, uom: 'PCS', weight: 140, volume: 0.9 },
      { shipmentId: 61, itemId: 11, description: 'Livrés - Livres scolaires', quantity: 50, uom: 'PCS', weight: 75, volume: 0.3 },
      { shipmentId: 62, itemId: 6, description: 'Matériel médical - Stéthoscopes', quantity: 35, uom: 'PCS', weight: 165, volume: 1.1 },
      { shipmentId: 63, itemId: 5, description: 'Électronique - Ordinateurs portables', quantity: 12, uom: 'PCS', weight: 85, volume: 0.7 },
      { shipmentId: 64, itemId: 10, description: 'Outillage - Boîte à outils', quantity: 20, uom: 'PCS', weight: 125, volume: 0.8 },
      { shipmentId: 65, itemId: 9, description: 'Mobilier - Chaises de bureau', quantity: 6, uom: 'PCS', weight: 195, volume: 1.6 },
      
      // Trip 34 Shipments (3 April - DELIVERED)
      { shipmentId: 66, itemId: 1, description: 'Produit A - Boîte standard', quantity: 85, uom: 'PCS', weight: 290, volume: 1.4 },
      { shipmentId: 67, itemId: 3, description: 'Produit C - Carton', quantity: 45, uom: 'CTN', weight: 380, volume: 2.3 },
      { shipmentId: 68, itemId: 6, description: 'Matériel médical - Stéthoscopes', quantity: 28, uom: 'PCS', weight: 110, volume: 0.5 },
      { shipmentId: 69, itemId: 7, description: 'Produits alimentaires - Conserves', quantity: 65, uom: 'PCS', weight: 195, volume: 1.2 },
      { shipmentId: 70, itemId: 8, description: 'Textile - Vêtements', quantity: 40, uom: 'PCS', weight: 88, volume: 0.6 },
      { shipmentId: 71, itemId: 11, description: 'Livrés - Livres scolaires', quantity: 75, uom: 'PCS', weight: 135, volume: 0.9 },
      { shipmentId: 72, itemId: 1, description: 'Produit A - Boîte standard', quantity: 22, uom: 'PCS', weight: 168, volume: 1.1 }
    ];
    
    let lineId = 93; // Starting from next available ID
    
    for (const line of shipmentLines) {
      const query = `
        INSERT INTO "ShipmentLine" (
          id, "shipmentId", "itemId", description, quantity, uom, weight, volume
        ) VALUES (
          $1, $2, $3, $4, $5, $6, $7, $8
        )
      `;
      
      await pool.query(query, [
        lineId, line.shipmentId, line.itemId, line.description,
        line.quantity, line.uom, line.weight, line.volume
      ]);
      
      console.log(`✅ Inserted ShipmentLine: Shipment ${line.shipmentId} → Item ${line.itemId} (${line.quantity} ${line.uom})`);
      lineId++;
    }
    
    console.log('\n=== SHIPMENT LINE INSERTION COMPLETE ===');
    console.log(`✅ Total shipment lines inserted: ${shipmentLines.length}`);
    
  } catch (err) {
    console.error('❌ Error inserting shipment lines:', err.message);
  } finally {
    await pool.end();
  }
}

insertShipmentLines();
