const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// GET /api/shipment/trip/:tripId - Obtenir les expéditions d'un trip spécifique
router.get('/trip/:tripId', async (req, res) => {
    try {
        const { tripId } = req.params;
        
        console.log(`📦 Recherche des expéditions pour tripId: ${tripId}`);
        
        // Requête pour obtenir les expéditions liées à ce trip via TripShipmentLink
        const query = `
            SELECT 
                s.id,
                s."shipmentNo",
                SUBSTRING(s.description, 1, 200) as description,
                s."deliveryCity",
                s."trackingNumber",
                s.status,
                s."createdAt",
                tsl.role
            FROM "Shipment" s
            INNER JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId"
            WHERE tsl."tripId" = $1
            ORDER BY s.id
        `;
        
        const result = await pool.query(query, [tripId]);
        
        console.log(`✅ ${result.rows.length} expédition(s) trouvée(s) pour tripId: ${tripId}`);
        
        res.json(result.rows);
        
    } catch (error) {
        console.error('❌ Erreur lors de la récupération des expéditions du trip:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

// PUT /api/shipment/trip-shipment/:id/status - Mettre à jour le statut d'une livraison
router.put('/trip-shipment/:id/status', async (req, res) => {
    const client = await pool.connect();
    
    try {
        const { id } = req.params;
        const { status } = req.body;
        
        console.log(`🔄 Mise à jour statut TripShipmentLink ID: ${id} -> ${status}`);
        
        // Validation du statut
        const validStatuses = ['NON_DEMARRE', 'ASSIGNED', 'EN_COURS', 'TERMINE'];
        if (!validStatuses.includes(status)) {
            return res.status(400).json({
                error: 'Statut invalide',
                message: `Le statut doit être l'un des: ${validStatuses.join(', ')}`
            });
        }
        
        await client.query('BEGIN');
        
        // 1. Récupérer les informations actuelles du TripShipmentLink
        const currentQuery = `
            SELECT tsl.*, s.id as "shipmentId", t.id as "tripId", t.status as trip_status
            FROM "TripShipmentLink" tsl
            INNER JOIN "Shipment" s ON tsl."shipmentId" = s.id
            INNER JOIN "Trip" t ON tsl."tripId" = t.id
            WHERE tsl.id = $1
        `;
        
        const currentResult = await client.query(currentQuery, [id]);
        
        if (currentResult.rows.length === 0) {
            await client.query('ROLLBACK');
            return res.status(404).json({
                error: 'TripShipmentLink non trouvé',
                message: `Aucune livraison trouvée avec l'ID: ${id}`
            });
        }
        
        const current = currentResult.rows[0];
        console.log(`📊 Statut actuel: ${current.status} -> Nouveau: ${status}`);
        
        // 2. Mettre à jour TripShipmentLink
        let podDone = current.podDone;
        let updateShipment = false;
        
        if (status === 'TERMINE') {
            podDone = true;
            updateShipment = true;
        } else if (status === 'NON_DEMARRE' || status === 'ASSIGNED') {
            podDone = false;
            updateShipment = true;
        }
        
        const updateTripShipmentQuery = `
            UPDATE "TripShipmentLink" 
            SET status = $1, "podDone" = $2, "updatedAt" = CURRENT_TIMESTAMP
            WHERE id = $3
            RETURNING *
        `;
        
        const updateResult = await client.query(updateTripShipmentQuery, [status, podDone, id]);
        console.log(`✅ TripShipmentLink mis à jour: ${updateResult.rows[0]}`);
        
        // 3. Mettre à jour Shipment si nécessaire
        if (updateShipment) {
            let shipmentStatus;
            switch (status) {
                case 'TERMINE':
                    shipmentStatus = 'DELIVERED';
                    break;
                case 'EN_COURS':
                    shipmentStatus = 'EXPEDITION';
                    break;
                case 'NON_DEMARRE':
                case 'ASSIGNED':
                    shipmentStatus = 'TO_PLAN';
                    break;
                default:
                    shipmentStatus = 'TO_PLAN';
            }
            
            const updateShipmentQuery = `
                UPDATE "Shipment" 
                SET status = $1, "updatedAt" = CURRENT_TIMESTAMP
                WHERE id = $2
                RETURNING *
            `;
            
            const shipmentResult = await client.query(updateShipmentQuery, [shipmentStatus, current.shipmentId]);
            console.log(`✅ Shipment mis à jour: ${shipmentResult.rows[0]}`);
        }
        
        // 4. Mettre à jour Trip status si nécessaire
        if (status === 'EN_COURS' && current.trip_status !== 'IN_PROGRESS') {
            const updateTripQuery = `
                UPDATE "Trip" 
                SET status = 'IN_PROGRESS'
                WHERE id = $1
                RETURNING *
            `;
            
            const tripResult = await client.query(updateTripQuery, [current.tripId]);
            console.log(`✅ Trip mis à jour en IN_PROGRESS: ${tripResult.rows[0]}`);
        }
        
        // 5. Vérifier si toutes les livraisons du trip sont terminées
        const allCompletedQuery = `
            SELECT COUNT(*) as total,
                   COUNT(CASE WHEN status = 'TERMINE' THEN 1 END) as completed
            FROM "TripShipmentLink"
            WHERE "tripId" = $1
        `;
        
        const allCompletedResult = await client.query(allCompletedQuery, [current.tripId]);
        const { total, completed } = allCompletedResult.rows[0];
        
        if (parseInt(total) === parseInt(completed)) {
            const completeTripQuery = `
                UPDATE "Trip" 
                SET status = 'COMPLETED'
                WHERE id = $1
                RETURNING *
            `;
            
            const completeTripResult = await client.query(completeTripQuery, [current.tripId]);
            console.log(`✅ Trip complété: ${completeTripResult.rows[0]}`);
        }
        
        await client.query('COMMIT');
        
        res.json({
            success: true,
            message: 'Statut mis à jour avec succès',
            data: {
                tripShipmentLink: updateResult.rows[0],
                tripId: current.tripId,
                shipmentId: current.shipmentId
            }
        });
        
    } catch (error) {
        await client.query('ROLLBACK');
        console.error('❌ Erreur lors de la mise à jour du statut:', error);
        res.status(500).json({
            error: 'Erreur serveur',
            message: error.message
        });
    } finally {
        client.release();
    }
});

module.exports = router;
