const express = require('express');
const router = express.Router();
const pool = require('../config/database');

/**
 * GET /api/delivery-tracking/date
 * Récupérer une tournée pour une date spécifique
 */
router.get('/date', async (req, res) => {
    const { driverId, date } = req.query;
    
    if (!driverId || !date) {
        return res.status(400).json({ 
            success: false, 
            message: 'driverId et date sont requis' 
        });
    }
    
    try {
        console.log(`📡 Recherche tournée pour driver ${driverId} le ${date}`);
        
        // Récupérer la tournée la plus pertinente pour la date spécifiée
        const query = `
            SELECT 
                id,
                "tripDate",
                "driverId",
                "vehicleId",
                "depotId",
                status,
                "tripId" as "tripIdentifier"
            FROM "Trip" 
            WHERE "driverId" = $1 
            AND DATE("tripDate") = $2
            ORDER BY 
                CASE 
                    WHEN status = 'IN_PROGRESS' THEN 1
                    WHEN status = 'READY' THEN 2
                    WHEN status = 'PLANNING' THEN 3
                    ELSE 4
                END,
                "tripDate" ASC
            LIMIT 1
        `;
        
        console.log('Query:', query);
        console.log('Params:', [driverId, date]);
        
        const tripResult = await pool.query(query, [driverId, date]);
        const trip = tripResult.rows[0] || null;
        
        console.log(`📊 Tournée trouvée: ${trip ? trip.id : 'aucune'}`);
        
        if (!trip) {
            console.log('❌ Aucune tournée trouvée pour cette date');
            return res.json({
                trip: null,
                deliveries: [],
                date: date
            });
        }
        
        console.log('✅ Tournée trouvée:', trip.id);
        
        // Récupérer les livraisons de la tournée
        const deliveriesQuery = `
            SELECT 
                tsl.sequence,
                tsl."shipmentId",
                tsl.status,
                tsl."podDone",
                s."shipmentNo",
                s."destinationId",
                s."deliveryAddress",
                s."deliveryCity",
                s."deliveryZipCode",
                s."deliveryCountry",
                s."distanceKm",
                s."estimatedDuration",
                s.description,
                s.quantity,
                s.uom,
                c.name as "clientName",
                c.phone as "clientPhone",
                l.address as "fullAddress",
                l.city as "locationCity",
                l."postalCode" as "locationPostalCode"
            FROM "TripShipmentLink" tsl
            JOIN "Shipment" s ON tsl."shipmentId" = s.id
            LEFT JOIN "Client" c ON s."customerId" = c.id
            LEFT JOIN "Location" l ON s."destinationId" = l.id
            WHERE tsl."tripId" = $1
            ORDER BY tsl.sequence
        `;
        
        console.log('Deliveries query:', deliveriesQuery);
        console.log('Trip ID:', trip.id);
        
        const deliveriesResult = await pool.query(deliveriesQuery, [trip.id]);
        const deliveries = deliveriesResult.rows;
        
        console.log(`📦 Livraisons trouvées: ${deliveries.length}`);
        
        res.json({
            trip: trip,
            deliveries: deliveries,
            date: date
        });
        
    } catch (error) {
        console.error('Erreur dans /api/delivery-tracking/date:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur: ' + error.message
        });
    }
});

/**
 * GET /api/delivery-tracking/today
 * Récupérer la tournée du jour pour un chauffeur
 */
router.get('/today', async (req, res) => {
    const { driverId } = req.query;
    
    if (!driverId) {
        return res.status(400).json({ 
            success: false, 
            message: 'driverId est requis' 
        });
    }
    
    try {
        const query = `
            SELECT 
                id,
                tripDate,
                driverId,
                vehicleId,
                depotId,
                status,
                tripId as "tripIdentifier"
            FROM Trip 
            WHERE driverId = $1 
            AND DATE(tripDate) = CURRENT_DATE
            LIMIT 1
        `;
        
        const result = await pool.query(query, [driverId]);
        
        res.json({
            success: true,
            trip: result.rows[0] || null,
            exists: result.rows.length > 0
        });
        
    } catch (error) {
        console.error('Erreur dans /api/trips/today:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur'
        });
    }
});

/**
 * GET /api/trips/today/with-deliveries
 * Récupérer la tournée du jour avec toutes les livraisons
 */
router.get('/today/with-deliveries', async (req, res) => {
    const { driverId } = req.query;
    
    if (!driverId) {
        return res.status(400).json({ 
            success: false, 
            message: 'driverId est requis' 
        });
    }
    
    try {
        // Récupérer la tournée du jour
        const tripQuery = `
            SELECT 
                id,
                tripDate,
                driverId,
                vehicleId,
                depotId,
                status,
                tripId as "tripIdentifier"
            FROM Trip 
            WHERE driverId = $1 
            AND DATE(tripDate) = CURRENT_DATE
            LIMIT 1
        `;
        
        const tripResult = await pool.query(tripQuery, [driverId]);
        const trip = tripResult.rows[0] || null;
        
        let deliveries = [];
        
        if (trip) {
            // Récupérer les livraisons de la tournée
            const deliveriesQuery = `
                SELECT 
                    tsl.sequence,
                    tsl.shipmentId,
                    tsl.status,
                    tsl.podDone,
                    s.shipmentNo,
                    s.destinationId,
                    s.deliveryAddress,
                    s.deliveryCity,
                    s.deliveryZipCode,
                    s.deliveryCountry,
                    s.distanceKm,
                    s.estimatedDuration,
                    s.description,
                    s.quantity,
                    s.uom,
                    c.name as "clientName",
                    c.phone as "clientPhone",
                    l.address as "fullAddress",
                    l.city as "locationCity",
                    l.postalCode as "locationPostalCode"
                FROM TripShipmentLink tsl
                JOIN Shipment s ON tsl.shipmentId = s.id
                LEFT JOIN Client c ON s.customerId = c.id
                LEFT JOIN Location l ON s.destinationId = l.id
                WHERE tsl.tripId = $1
                ORDER BY tsl.sequence
            `;
            
            const deliveriesResult = await pool.query(deliveriesQuery, [trip.id]);
            deliveries = deliveriesResult.rows;
        }
        
        res.json({
            trip: trip,
            deliveries: deliveries
        });
        
    } catch (error) {
        console.error('Erreur dans /api/trips/today/with-deliveries:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur'
        });
    }
});

/**
 * GET /api/trips/:tripId/deliveries
 * Récupérer les livraisons pour une tournée spécifique
 */
router.get('/:tripId/deliveries', async (req, res) => {
    const { tripId } = req.params;
    
    try {
        const query = `
            SELECT 
                tsl.sequence,
                tsl.shipmentId,
                tsl.status,
                tsl.podDone,
                s.shipmentNo,
                s.destinationId,
                s.deliveryAddress,
                s.deliveryCity,
                s.deliveryZipCode,
                s.deliveryCountry,
                s.distanceKm,
                s.estimatedDuration,
                s.description,
                s.quantity,
                s.uom,
                c.name as "clientName",
                c.phone as "clientPhone",
                l.address as "fullAddress",
                l.city as "locationCity",
                l.postalCode as "locationPostalCode"
            FROM TripShipmentLink tsl
            JOIN Shipment s ON tsl.shipmentId = s.id
            LEFT JOIN Client c ON s.customerId = c.id
            LEFT JOIN Location l ON s.destinationId = l.id
            WHERE tsl.tripId = $1
            ORDER BY tsl.sequence
        `;
        
        const result = await pool.query(query, [tripId]);
        res.json(result.rows);
        
    } catch (error) {
        console.error('Erreur dans /api/trips/:tripId/deliveries:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur'
        });
    }
});

/**
 * PUT /api/shipments/:shipmentId/status
 * Mettre à jour le statut d'une livraison
 */
router.put('/shipments/:shipmentId/status', async (req, res) => {
    const { shipmentId } = req.params;
    const { status } = req.body;
    
    if (!status) {
        return res.status(400).json({ 
            success: false, 
            message: 'status est requis' 
        });
    }
    
    try {
        const query = `
            UPDATE TripShipmentLink 
            SET status = $1, updatedAt = CURRENT_TIMESTAMP
            WHERE shipmentId = $2
        `;
        
        const result = await pool.query(query, [status, shipmentId]);
        
        if (result.rowCount === 0) {
            return res.status(404).json({
                success: false,
                message: 'Livraison non trouvée'
            });
        }
        
        res.json({
            success: true,
            message: 'Statut mis à jour avec succès'
        });
        
    } catch (error) {
        console.error('Erreur dans /api/shipments/:shipmentId/status:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur'
        });
    }
});

/**
 * PUT /api/shipments/:shipmentId/complete
 * Marquer une livraison comme complétée (POD done)
 */
router.put('/shipments/:shipmentId/complete', async (req, res) => {
    const { shipmentId } = req.params;
    
    try {
        const query = `
            UPDATE TripShipmentLink 
            SET status = 'COMPLETED', 
                podDone = true, 
                updatedAt = CURRENT_TIMESTAMP
            WHERE shipmentId = $1
        `;
        
        const result = await pool.query(query, [shipmentId]);
        
        if (result.rowCount === 0) {
            return res.status(404).json({
                success: false,
                message: 'Livraison non trouvée'
            });
        }
        
        res.json({
            success: true,
            message: 'Livraison complétée avec succès'
        });
        
    } catch (error) {
        console.error('Erreur dans /api/shipments/:shipmentId/complete:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur'
        });
    }
});

module.exports = router;
