const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// Get complete tournee details by ID using real database tables
router.get('/:id/details', async (req, res) => {
  try {
    const { id } = req.params;
    
    // Get basic trip info
    const tripResult = await pool.query(`
      SELECT "id", "tripDate", "depotId", "driverId", "vehicleId", "status", 
             "tenantId", "createdAt", "tripId"
      FROM "Trip"
      WHERE "id" = $1
    `, [id]);

    if (tripResult.rows.length === 0) {
      return res.status(404).json({ 
        success: false, 
        error: 'Trip not found' 
      });
    }

    const trip = tripResult.rows[0];

    // Get all shipments for this trip via TripShipmentLink
    const shipmentsResult = await pool.query(`
      SELECT 
        s.id,
        s."shipmentNo",
        s."trackingNumber",
        s.status,
        SUBSTRING(s.description, 1, 200) as description,
        s.quantity,
        s.uom,
        s.packaging,
        s.weight,
        s."deliveryAddress",
        s."deliveryCity",
        s."deliveryZipCode",
        s."deliveryCountry",
        s."customerId",
        s."originId",
        s."destinationId",
        s."vehicleId",
        s.priority,
        s."requestedPickup",
        s."requestedDelivery",
        s."plannedStart",
        s."plannedEnd",
        s."distanceKm",
        s."estimatedDuration",
        s."createdAt",
        s."updatedAt",
        tsl.sequence as "tripSequence",
        tsl.role as "shipmentRole",
        tsl.status as "linkStatus",
        tsl."podDone",
        tsl."returnsDone",
        c.name as "customerName",
        c.address as "customerAddress",
        c.city as "customerCity",
        c.phone as "customerPhone",
        l1.name as "originName",
        l1.address as "originAddress",
        l1.city as "originCity",
        l2.name as "destinationName",
        l2.address as "destinationAddress",
        l2.city as "destinationCity",
        v.name as "vehicleName",
        v.registration as "vehicleRegistration"
      FROM "TripShipmentLink" tsl
      JOIN "Shipment" s ON tsl."shipmentId" = s."id"
      LEFT JOIN "Client" c ON s."customerId" = c."id"
      LEFT JOIN "Location" l1 ON s."originId" = l1."id"
      LEFT JOIN "Location" l2 ON s."destinationId" = l2."id"
      LEFT JOIN "Vehicle" v ON s."vehicleId" = v."id"
      WHERE tsl."tripId" = $1
      ORDER BY tsl.sequence
    `, [id]);

    // Get trip stops
    const stopsResult = await pool.query(`
      SELECT 
        ts.*,
        l.name as "locationName",
        l.address as "locationAddress",
        l.city as "locationCity",
        l."postalCode" as "locationPostalCode"
      FROM "TripStop" ts
      JOIN "Location" l ON ts."locationId" = l."id"
      WHERE ts."tripId" = $1
      ORDER BY ts.sequence
    `, [id]);

    // Get driver info
    let driver = null;
    if (trip.driverId) {
      const driverResult = await pool.query(`
        SELECT "id", "name", "licenseNumber", "employmentType", "status", "phone", "email",
               "address", "city", "postalCode"
        FROM "Driver"
        WHERE "id" = $1
      `, [trip.driverId]);
      
      if (driverResult.rows.length > 0) {
        driver = driverResult.rows[0];
      }
    }

    // Get vehicle info
    let vehicle = null;
    if (trip.vehicleId) {
      const vehicleResult = await pool.query(`
        SELECT "id", "name", "registration", "capacityWeight", "capacityVolume", 
               "type", "status", "year"
        FROM "Vehicle"
        WHERE "id" = $1
      `, [trip.vehicleId]);
      
      if (vehicleResult.rows.length > 0) {
        vehicle = vehicleResult.rows[0];
      }
    }

    // Get depot info
    let depot = null;
    if (trip.depotId) {
      const depotResult = await pool.query(`
        SELECT "id", "name", "address", "city", "postalCode", "phone", "email"
        FROM "Location"
        WHERE "id" = $1
      `, [trip.depotId]);
      
      if (depotResult.rows.length > 0) {
        depot = depotResult.rows[0];
      }
    }

    // Calculate trip statistics from shipments
    const stats = {
      totalShipments: shipmentsResult.rows.length,
      completedShipments: shipmentsResult.rows.filter(s => s.status === 'DELIVERED').length,
      totalWeight: shipmentsResult.rows.reduce((sum, s) => sum + (s.weight || 0), 0),
      totalDistance: shipmentsResult.rows.reduce((sum, s) => sum + (s.distanceKm || 0), 0),
      estimatedDuration: shipmentsResult.rows.reduce((sum, s) => sum + (s.estimatedDuration || 0), 0)
    };

    // Build detailed trip object with real data
    const detailedTrip = {
      ...trip,
      ...stats,
      actualDuration: null // Would need to be calculated from actual start/end times
    };

    // Add origin and destination objects to shipments
    const shipmentsWithLocations = shipmentsResult.rows.map(shipment => {
      return {
        ...shipment,
        origin: {
          id: shipment.originId,
          name: shipment.originName || `Location ${shipment.originId}`,
          address: shipment.originAddress,
          city: shipment.originCity,
          postalCode: null // Could be added if needed
        },
        destination: {
          id: shipment.destinationId,  
          name: shipment.destinationName || `Location ${shipment.destinationId}`,
          address: shipment.destinationAddress,
          city: shipment.destinationCity,
          postalCode: null // Could be added if needed
        }
      };
    });

    const response = {
      success: true,
      data: {
        trip: detailedTrip,
        shipments: shipmentsWithLocations,
        stops: stopsResult.rows,
        driver: driver,
        vehicle: vehicle,
        depot: depot
      }
    };

    res.json(response);
  } catch (error) {
    console.error('Error fetching tournee details:', error);
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
});

module.exports = router;
