const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// GET /api/shipment
// Rechercher une livraison par code-barres ou numéro de suivi
router.get('/', async (req, res) => {
  try {
    const { barcode, driverId } = req.query;
    
    if (!barcode || !driverId) {
      return res.status(400).json({
        success: false,
        message: "Le code-barres et l'ID du chauffeur sont requis"
      });
    }
    
    console.log(`📦 Recherche livraison: barcode=${barcode}, driverId=${driverId}`);
    
    // Rechercher la livraison par shipmentNo ou trackingNumber
    const shipmentQuery = `
      SELECT 
        s.id,
        s."shipmentNo",
        s."trackingNumber",
        s.status,
        s.description,
        s.quantity,
        s."deliveryAddress",
        s."deliveryCity",
        s."deliveryZipCode",
        s."customerId",
        s.priority,
        s."plannedStart",
        s."plannedEnd"
      FROM "Shipment" s
      WHERE s."shipmentNo" = $1 OR s."trackingNumber" = $1
      LIMIT 1
    `;
    
    const shipmentResult = await pool.query(shipmentQuery, [barcode]);
    
    if (shipmentResult.rows.length === 0) {
      return res.json({
        success: true,
        data: null,
        message: "Colis introuvable dans le système"
      });
    }
    
    const shipment = shipmentResult.rows[0];
    console.log(`✅ Livraison trouvée: ${shipment.shipmentNo}`);
    
    // Vérifier si la livraison appartient à la tournée actuelle du chauffeur
    const today = new Date().toISOString().split('T')[0];
    const tourCheckQuery = `
      SELECT 
        tsl.sequence,
        t.id as tripId,
        t."tripId" as tripNumber
      FROM "TripShipmentLink" tsl
      JOIN "Trip" t ON tsl."tripId" = t.id
      WHERE tsl."shipmentId" = $1 
        AND t."driverId" = $2 
        AND DATE(t."tripDate") = $3
      LIMIT 1
    `;
    
    const tourCheckResult = await pool.query(tourCheckQuery, [shipment.id, driverId, today]);
    const belongsToCurrentTour = tourCheckResult.rows.length > 0;
    const tourSequence = belongsToCurrentTour ? tourCheckResult.rows[0].sequence : null;
    
    console.log(`📍 Appartient à la tournée: ${belongsToCurrentTour}, Séquence: ${tourSequence}`);
    
    // Récupérer les informations du client
    let clientInfo = null;
    if (shipment.customerId) {
      const clientQuery = `
        SELECT 
          id, 
          name, 
          address, 
          city, 
          "postalCode",
          phone
        FROM "Client" 
        WHERE id = $1
      `;
      
      const clientResult = await pool.query(clientQuery, [shipment.customerId]);
      if (clientResult.rows.length > 0) {
        clientInfo = clientResult.rows[0];
      }
    }
    
    res.json({
      success: true,
      data: {
        shipment: {
          id: shipment.id,
          shipmentNo: shipment.shipmentNo,
          trackingNumber: shipment.trackingNumber,
          status: shipment.status,
          description: shipment.description,
          quantity: shipment.quantity,
          deliveryAddress: shipment.deliveryAddress,
          deliveryCity: shipment.deliveryCity,
          deliveryZipCode: shipment.deliveryZipCode,
          customerId: shipment.customerId,
          priority: shipment.priority,
          plannedStart: shipment.plannedStart,
          plannedEnd: shipment.plannedEnd
        },
        belongsToCurrentTour,
        tourSequence,
        client: clientInfo
      }
    });
    
  } catch (error) {
    console.error('❌ Erreur lors de la recherche de livraison:', error);
    res.status(500).json({
      success: false,
      message: "Erreur serveur lors de la recherche de livraison"
    });
  }
});

// GET /api/shipment/tracking
// Rechercher par numéro de suivi uniquement
router.get('/tracking', async (req, res) => {
  try {
    const { trackingNumber, driverId } = req.query;
    
    if (!trackingNumber || !driverId) {
      return res.status(400).json({
        success: false,
        message: "Le numéro de suivi et l'ID du chauffeur sont requis"
      });
    }
    
    console.log(`📦 Recherche par tracking: ${trackingNumber}, driverId=${driverId}`);
    
    // Réutiliser la logique du endpoint principal
    req.query.barcode = trackingNumber;
    return router.handle(req, res);
    
  } catch (error) {
    console.error('❌ Erreur lors de la recherche par tracking:', error);
    res.status(500).json({
      success: false,
      message: "Erreur serveur lors de la recherche par tracking"
    });
  }
});

// GET /api/shipment/complete
// Marquer une livraison comme complétée
router.get('/complete', async (req, res) => {
  try {
    const { shipmentId, driverId } = req.query;
    
    if (!shipmentId || !driverId) {
      return res.status(400).json({
        success: false,
        message: "L'ID de la livraison et l'ID du chauffeur sont requis"
      });
    }
    
    console.log(`✅ Marquer livraison comme complétée: shipmentId=${shipmentId}, driverId=${driverId}`);
    
    // Vérifier que la livraison appartient bien à la tournée du chauffeur
    const today = new Date().toISOString().split('T')[0];
    const verifyQuery = `
      SELECT s.id, s."shipmentNo"
      FROM "Shipment" s
      JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId"
      JOIN "Trip" t ON tsl."tripId" = t.id
      WHERE s.id = $1 
        AND t."driverId" = $2 
        AND DATE(t."tripDate") = $3
      LIMIT 1
    `;
    
    const verifyResult = await pool.query(verifyQuery, [shipmentId, driverId, today]);
    
    if (verifyResult.rows.length === 0) {
      return res.status(403).json({
        success: false,
        message: "Cette livraison n'appartient pas à votre tournée"
      });
    }
    
    // Mettre à jour le statut
    const updateQuery = `
      UPDATE "Shipment" 
      SET status = 'DELIVERED', 
          "updatedAt" = CURRENT_TIMESTAMP
      WHERE id = $1
      RETURNING id, "shipmentNo", status
    `;
    
    const updateResult = await pool.query(updateQuery, [shipmentId]);
    
    res.json({
      success: true,
      data: {
        shipment: updateResult.rows[0],
        message: "Livraison marquée comme complétée avec succès"
      }
    });
    
  } catch (error) {
    console.error('❌ Erreur lors de la mise à jour de la livraison:', error);
    res.status(500).json({
      success: false,
      message: "Erreur serveur lors de la mise à jour de la livraison"
    });
  }
});

module.exports = router;
