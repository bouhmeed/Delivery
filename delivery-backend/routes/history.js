const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get driver delivery history with real database data
router.get('/driver/:driverId', async (req, res) => {
  try {
    const { driverId } = req.params;
    const { limit = 50, offset = 0 } = req.query;

    // Main query to get delivery history with all required joins
    const historyQuery = `
      SELECT 
        t.id as trip_id,
        t."tripId" as trip_number,
        t."tripDate" as trip_date,
        t.status as trip_status,
        s.id as shipment_id,
        s."shipmentNo" as shipment_number,
        s.status as shipment_status,
        s.description as shipment_description,
        s.quantity,
        s.uom,
        c.name as client_name,
        c.address as client_address,
        c.city as client_city,
        c."postalCode" as client_postal_code,
        origin_loc.name as origin_name,
        origin_loc.city as origin_city,
        origin_loc.address as origin_address,
        dest_loc.name as destination_name,
        dest_loc.city as destination_city,
        dest_loc.address as destination_address,
        v.name as vehicle_name,
        v.registration as vehicle_registration,
        v.type as vehicle_type,
        tsl.status as link_status,
        tsl."podDone" as pod_done,
        tsl.sequence,
        d.name as driver_name
      FROM "Trip" t
      JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
      JOIN "Shipment" s ON tsl."shipmentId" = s.id
      LEFT JOIN "Client" c ON s."customerId" = c.id
      LEFT JOIN "Location" origin_loc ON s."originId" = origin_loc.id
      LEFT JOIN "Location" dest_loc ON s."destinationId" = dest_loc.id
      LEFT JOIN "Vehicle" v ON t."vehicleId" = v.id
      LEFT JOIN "Driver" d ON t."driverId" = d.id
      WHERE t."driverId" = $1 
        AND (t.status = 'COMPLETED' OR t."tripDate" < CURRENT_DATE)
      ORDER BY t."tripDate" DESC, tsl.sequence ASC
      LIMIT $2 OFFSET $3
    `;

    const historyResult = await pool.query(historyQuery, [driverId, limit, offset]);

    // Get total count for pagination
    const countQuery = `
      SELECT COUNT(*) as total
      FROM "Trip" t
      WHERE t."driverId" = $1 
        AND (t.status = 'COMPLETED' OR t."tripDate" < CURRENT_DATE)
    `;
    const countResult = await pool.query(countQuery, [driverId]);

    // Transform data to match expected format
    const history = historyResult.rows.map(row => ({
      id: row.trip_id.toString(),
      tripDate: row.trip_date,
      tripNumber: row.trip_number,
      tripStatus: row.trip_status,
      shipmentId: row.shipment_id.toString(),
      shipmentNumber: row.shipment_number,
      shipmentStatus: row.shipment_status,
      shipmentDescription: row.shipment_description,
      quantity: row.quantity,
      uom: row.uom,
      clientName: row.client_name,
      clientAddress: row.client_address,
      clientCity: row.client_city,
      clientPostalCode: row.client_postal_code,
      originName: row.origin_name,
      originCity: row.origin_city,
      originAddress: row.origin_address,
      destinationName: row.destination_name,
      destinationCity: row.destination_city,
      destinationAddress: row.destination_address,
      vehicleName: row.vehicle_name,
      vehicleRegistration: row.vehicle_registration,
      vehicleType: row.vehicle_type,
      linkStatus: row.link_status,
      podDone: row.pod_done,
      sequence: row.sequence,
      driverName: row.driver_name
    }));

    res.json({
      history,
      pagination: {
        currentPage: Math.floor(offset / limit) + 1,
        totalPages: Math.ceil(countResult.rows[0].total / limit),
        totalItems: parseInt(countResult.rows[0].total),
        itemsPerPage: parseInt(limit)
      }
    });
  } catch (error) {
    console.error('Error fetching driver history:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get driver statistics
router.get('/stats/driver/:driverId', async (req, res) => {
  try {
    const { driverId } = req.params;
    const { period = '30' } = req.query; // days

    // Get driver statistics
    const statsQuery = `
      SELECT 
        COUNT(DISTINCT t.id) as total_trips,
        COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) as completed_trips,
        COUNT(DISTINCT s.id) as total_shipments,
        COUNT(DISTINCT CASE WHEN s.status = 'DELIVERED' THEN s.id END) as delivered_shipments,
        COUNT(DISTINCT CASE WHEN s.status = 'TO_PLAN' THEN s.id END) as pending_shipments,
        COUNT(DISTINCT CASE WHEN s.status = 'EXPEDITION' THEN s.id END) as expedition_shipments,
        COALESCE(SUM(s.quantity), 0) as total_quantity,
        COALESCE(SUM(s.weight), 0) as total_weight,
        COALESCE(AVG(s.weight), 0) as avg_weight,
        MAX(t."tripDate") as last_trip_date,
        MIN(t."tripDate") as first_trip_date
      FROM "Trip" t
      LEFT JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
      LEFT JOIN "Shipment" s ON tsl."shipmentId" = s.id
      WHERE t."driverId" = $1 
        AND t."tripDate" >= CURRENT_DATE - INTERVAL '${period} days'
    `;

    const statsResult = await pool.query(statsQuery, [driverId]);

    // Get monthly trends for the last 6 months
    const trendsQuery = `
      SELECT 
        TO_CHAR(t."tripDate", 'YYYY-MM') as month,
        COUNT(DISTINCT t.id) as trips,
        COUNT(DISTINCT s.id) as deliveries,
        COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) as completed_trips,
        COUNT(DISTINCT CASE WHEN s.status = 'DELIVERED' THEN s.id END) as delivered_shipments,
        COALESCE(SUM(s.quantity), 0) as total_quantity,
        CASE 
          WHEN COUNT(DISTINCT s.id) > 0 
          THEN ROUND((COUNT(DISTINCT CASE WHEN s.status = 'DELIVERED' THEN s.id END) * 100.0 / COUNT(DISTINCT s.id)), 2)
          ELSE 0 
        END as success_rate
      FROM "Trip" t
      LEFT JOIN "TripShipmentLink" tsl ON t.id = tsl."tripId"
      LEFT JOIN "Shipment" s ON tsl."shipmentId" = s.id
      WHERE t."driverId" = $1 
        AND t."tripDate" >= CURRENT_DATE - INTERVAL '6 months'
      GROUP BY TO_CHAR(t."tripDate", 'YYYY-MM')
      ORDER BY month DESC
    `;

    const trendsResult = await pool.query(trendsQuery, [driverId]);

    // Format the response
    const stats = statsResult.rows[0] || {};
    
    const driverStats = {
      driverId: driverId,
      totalTrips: parseInt(stats.total_trips) || 0,
      completedTrips: parseInt(stats.completed_trips) || 0,
      deliveredShipments: parseInt(stats.delivered_shipments) || 0,
      totalShipments: parseInt(stats.total_shipments) || 0,
      pendingShipments: parseInt(stats.pending_shipments) || 0,
      inProgressShipments: parseInt(stats.in_progress_shipments) || 0,
      totalQuantity: parseFloat(stats.total_quantity) || 0,
      totalWeight: parseFloat(stats.total_weight) || 0,
      averageWeight: parseFloat(stats.avg_weight) || 0,
      lastTripDate: stats.last_trip_date,
      firstTripDate: stats.first_trip_date,
      successRate: stats.total_trips > 0 ? 
        Math.round((stats.completed_trips / stats.total_trips) * 100) : 0
    };

    const monthlyTrends = trendsResult.rows.map(row => ({
      month: formatMonth(row.month),
      trips: parseInt(row.trips),
      deliveries: parseInt(row.deliveries),
      completedTrips: parseInt(row.completed_trips),
      deliveredShipments: parseInt(row.delivered_shipments),
      totalQuantity: parseFloat(row.total_quantity),
      successRate: parseFloat(row.success_rate)
    }));

    res.json({
      stats: driverStats,
      monthlyTrends
    });
  } catch (error) {
    console.error('Error fetching driver stats:', error);
    res.status(500).json({ error: error.message });
  }
});

// Helper function to format month
function formatMonth(monthString) {
  const months = {
    '01': 'Janvier', '02': 'Février', '03': 'Mars', '04': 'Avril',
    '05': 'Mai', '06': 'Juin', '07': 'Juillet', '08': 'Août',
    '09': 'Septembre', '10': 'Octobre', '11': 'Novembre', '12': 'Décembre'
  };
  
  const [year, month] = monthString.split('-');
  return `${months[month]} ${year}`;
}

module.exports = router;
