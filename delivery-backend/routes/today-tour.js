const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// GET /api/today-tour/driver/{driverId}
// Version avec vraies données de la base de données
router.get('/driver/:driverId', async (req, res) => {
  try {
    const { driverId } = req.params;
    const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD

    console.log(`📡 Recherche tournée réelle pour driver ${driverId} le ${today}`);

    // Requête simple qui fonctionne
    const tourQuery = `
      SELECT id, "tripId", status, "tripDate", "driverId"
      FROM "Trip" 
      WHERE "driverId" = $1 AND DATE("tripDate") = $2 
      LIMIT 1
    `;

    const tourResult = await pool.query(tourQuery, [driverId, today]);
    console.log(`📊 Tournées trouvées: ${tourResult.rows.length}`);

    if (tourResult.rows.length === 0) {
      console.log(`❌ Aucune tournée réelle trouvée pour driver ${driverId}`);
      return res.json({
        success: true,
        data: {
          hasTour: false,
          message: "Aucune tournée prévue pour aujourd'hui"
        }
      });
    }

    const tour = tourResult.rows[0];
    console.log(`✅ Tournée réelle trouvée: ${tour.tripId} (status: ${tour.status})`);

    // Récupérer les livraisons liées
    try {
      const shipmentsQuery = `
        SELECT s.id, s."shipmentNo", s.status, s.quantity, tsl.sequence
        FROM "TripShipmentLink" tsl
        JOIN "Shipment" s ON tsl."shipmentId" = s.id
        WHERE tsl."tripId" = $1
        ORDER BY tsl.sequence
      `;

      const shipmentsResult = await pool.query(shipmentsQuery, [tour.id]);
      const shipments = shipmentsResult.rows;
      console.log(`📦 Livraisons réelles trouvées: ${shipments.length}`);

      // Calculer les vraies statistiques
      const totalShipments = shipments.length;
      const completedShipments = shipments.filter(s => s.status === 'DELIVERED').length;
      const remainingShipments = totalShipments - completedShipments;
      const completionPercentage = totalShipments > 0 ? Math.round((completedShipments / totalShipments) * 100) : 0;
      const progressBar = '█'.repeat(Math.floor(completionPercentage / 10)) + '░'.repeat(10 - Math.floor(completionPercentage / 10));

      console.log(`📈 Statistiques réelles: ${totalShipments} total, ${completedShipments} complétées, ${completionPercentage}%`);

      res.json({
        success: true,
        data: {
          hasTour: true,
          tour: {
            id: tour.id,
            tripId: tour.tripId,
            status: tour.status,
            date: tour.tripDate
          },
          statistics: {
            totalShipments,
            completedShipments,
            remainingShipments,
            completionPercentage,
            progressBar
          },
          shipments: shipments
        }
      });

    } catch (shipmentError) {
      console.error('❌ Erreur livraisons:', shipmentError.message);
      // Retourner la tournée sans livraisons
      res.json({
        success: true,
        data: {
          hasTour: true,
          tour: {
            id: tour.id,
            tripId: tour.tripId,
            status: tour.status,
            date: tour.tripDate
          },
          statistics: {
            totalShipments: 0,
            completedShipments: 0,
            remainingShipments: 0,
            completionPercentage: 0,
            progressBar: "░░░░░░░░░░"
          },
          shipments: []
        }
      });
    }

  } catch (error) {
    console.error('❌ Erreur:', error.message);
    res.status(500).json({
      success: false,
      error: "Erreur serveur lors de la récupération de la tournée"
    });
  }
});

module.exports = router;
