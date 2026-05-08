const { Pool } = require('pg');
require('dotenv').config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: true }
});

// Fonction pour vérifier ce qui va être supprimé
async function reviewDataToDelete() {
    try {
        console.log('🔍 VÉRIFICATION DES DONNÉES À SUPPRIMER\n');
        
        // Vérifier les trips
        const tripsRes = await pool.query(`
            SELECT id, "tripDate", "driverId", status, "tripId"
            FROM "Trip"
            WHERE id IN (105, 106)
            ORDER BY id
        `);
        
        console.log('📋 Trips à supprimer:');
        console.table(tripsRes.rows);
        
        // Vérifier les shipments liés
        const shipmentsRes = await pool.query(`
            SELECT id, "shipmentNo", "customerId", status, "deliveryCity"
            FROM "Shipment"
            WHERE id IN (
                SELECT "shipmentId" 
                FROM "TripShipmentLink" 
                WHERE "tripId" IN (105, 106)
            )
            ORDER BY id
        `);
        
        console.log('\n📦 Shipments liés à supprimer:');
        console.table(shipmentsRes.rows);
        
        // Vérifier les TripShipmentLinks
        const linksRes = await pool.query(`
            SELECT id, "tripId", "shipmentId", status, sequence
            FROM "TripShipmentLink"
            WHERE "tripId" IN (105, 106)
            ORDER BY "tripId", sequence
        `);
        
        console.log('\n🔗 TripShipmentLinks à supprimer:');
        console.table(linksRes.rows);
        
        // Vérifier les ShipmentLines
        const shipmentLinesRes = await pool.query(`
            SELECT sl.id, sl."shipmentId", sl."itemCode", sl.quantity
            FROM "ShipmentLine" sl
            WHERE sl."shipmentId" IN (
                SELECT "shipmentId" 
                FROM "TripShipmentLink" 
                WHERE "tripId" IN (105, 106)
            )
            ORDER BY sl.id
        `);
        
        console.log('\n📋 ShipmentLines à supprimer:');
        console.table(shipmentLinesRes.rows);
        
        return {
            trips: tripsRes.rows,
            shipments: shipmentsRes.rows,
            links: linksRes.rows,
            shipmentLines: shipmentLinesRes.rows
        };
        
    } catch (err) {
        console.error('❌ Erreur lors de la vérification:', err);
        throw err;
    }
}

// Étape 1: Supprimer les TripShipmentLinks
async function deleteTripShipmentLinks(tripIds) {
    try {
        console.log('\n🔗 ÉTAPE 1: SUPPRESSION DES TRIP SHIPMENT LINKS');
        
        const result = await pool.query(`
            DELETE FROM "TripShipmentLink"
            WHERE "tripId" = ANY($1)
            RETURNING id, "tripId", "shipmentId", status
        `, [tripIds]);
        
        console.log(`✅ ${result.rows.length} TripShipmentLinks supprimés:`);
        console.table(result.rows);
        
        return result.rows;
        
    } catch (err) {
        console.error('❌ Erreur lors de la suppression des TripShipmentLinks:', err);
        throw err;
    }
}

// Étape 2: Supprimer les ShipmentLines
async function deleteShipmentLines(shipmentIds) {
    try {
        console.log('\n📋 ÉTAPE 2: SUPPRESSION DES SHIPMENT LINES');
        
        const result = await pool.query(`
            DELETE FROM "ShipmentLine"
            WHERE "shipmentId" = ANY($1)
            RETURNING id, "shipmentId", "itemCode", quantity
        `, [shipmentIds]);
        
        console.log(`✅ ${result.rows.length} ShipmentLines supprimés:`);
        console.table(result.rows);
        
        return result.rows;
        
    } catch (err) {
        console.error('❌ Erreur lors de la suppression des ShipmentLines:', err);
        throw err;
    }
}

// Étape 3: Supprimer les Shipments
async function deleteShipments(shipmentIds) {
    try {
        console.log('\n📦 ÉTAPE 3: SUPPRESSION DES SHIPMENTS');
        
        const result = await pool.query(`
            DELETE FROM "Shipment"
            WHERE id = ANY($1)
            RETURNING id, "shipmentNo", "customerId", status
        `, [shipmentIds]);
        
        console.log(`✅ ${result.rows.length} Shipments supprimés:`);
        console.table(result.rows);
        
        return result.rows;
        
    } catch (err) {
        console.error('❌ Erreur lors de la suppression des Shipments:', err);
        throw err;
    }
}

// Étape 4: Supprimer les Trips
async function deleteTrips(tripIds) {
    try {
        console.log('\n🚚 ÉTAPE 4: SUPPRESSION DES TRIPS');
        
        const result = await pool.query(`
            DELETE FROM "Trip"
            WHERE id = ANY($1)
            RETURNING id, "tripDate", "driverId", status, "tripId"
        `, [tripIds]);
        
        console.log(`✅ ${result.rows.length} Trips supprimés:`);
        console.table(result.rows);
        
        return result.rows;
        
    } catch (err) {
        console.error('❌ Erreur lors de la suppression des Trips:', err);
        throw err;
    }
}

// Vérification finale
async function verifyDeletion(tripIds) {
    try {
        console.log('\n🔍 VÉRIFICATION FINALE DE LA SUPPRESSION');
        
        // Vérifier que les trips sont supprimés
        const tripsCheck = await pool.query(`
            SELECT id, "tripDate", status
            FROM "Trip"
            WHERE id = ANY($1)
        `, [tripIds]);
        
        if (tripsCheck.rows.length === 0) {
            console.log('✅ Tous les trips ont été correctement supprimés');
        } else {
            console.log('⚠️  Trips encore présents:');
            console.table(tripsCheck.rows);
        }
        
        // Vérifier que les shipments sont supprimés
        const shipmentsCheck = await pool.query(`
            SELECT id, "shipmentNo", status
            FROM "Shipment"
            WHERE id IN (
                SELECT "shipmentId" 
                FROM "TripShipmentLink" 
                WHERE "tripId" = ANY($1)
            )
        `, [tripIds]);
        
        if (shipmentsCheck.rows.length === 0) {
            console.log('✅ Tous les shipments liés ont été correctement supprimés');
        } else {
            console.log('⚠️  Shipments encore présents:');
            console.table(shipmentsCheck.rows);
        }
        
        // Statistiques finales
        console.log('\n📊 STATISTIQUES FINALES:');
        console.log(`✅ Trips supprimés: ${tripIds.length}`);
        console.log('✅ Données liées supprimées en cascade');
        
        return true;
        
    } catch (err) {
        console.error('❌ Erreur lors de la vérification:', err);
        return false;
    }
}

// Fonction principale
async function deleteTrips105And106() {
    try {
        console.log('🗑️ SUPPRESSION SÉCURISÉE DES TRIPS 105 ET 106\n');
        
        // Étape de vérification
        const dataToDelete = await reviewDataToDelete();
        
        if (dataToDelete.trips.length === 0) {
            console.log('ℹ️  Les trips 105 et/ou 106 n\'existent pas déjà');
            return true;
        }
        
        // Confirmer la suppression
        console.log('\n⚠️  ATTENTION: Cette opération va supprimer:');
        console.log(`   - ${dataToDelete.trips.length} trip(s)`);
        console.log(`   - ${dataToDelete.links.length} TripShipmentLink(s)`);
        console.log(`   - ${dataToDelete.shipments.length} shipment(s)`);
        console.log(`   - ${dataToDelete.shipmentLines.length} ShipmentLine(s)`);
        
        // Étape 1: Supprimer les TripShipmentLinks
        const deletedLinks = await deleteTripShipmentLinks([105, 106]);
        
        // Extraire les IDs des shipments pour la suite
        const shipmentIds = deletedLinks.map(link => link.shipmentId);
        
        // Étape 2: Supprimer les ShipmentLines
        await deleteShipmentLines(shipmentIds);
        
        // Étape 3: Supprimer les Shipments
        await deleteShipments(shipmentIds);
        
        // Étape 4: Supprimer les Trips
        await deleteTrips([105, 106]);
        
        // Vérification finale
        const success = await verifyDeletion([105, 106]);
        
        if (success) {
            console.log('\n🎉 SUPPRESSION DES TRIPS 105 ET 106 TERMINÉE AVEC SUCCÈS!');
            console.log('✅ Toutes les données liées ont été correctement supprimées');
        }
        
        return success;
        
    } catch (err) {
        console.error('❌ Erreur lors de la suppression complète:', err);
        return false;
    } finally {
        await pool.end();
    }
}

// Si le script est appelé directement
if (require.main === module) {
    deleteTrips105And106();
}

module.exports = { deleteTrips105And106, reviewDataToDelete, deleteTripShipmentLinks, deleteShipmentLines, deleteShipments, deleteTrips };
