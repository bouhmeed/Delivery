const express = require('express');
const router = express.Router();
const pool = require('../config/database');

// GET /api/shipments/:shipmentId/details
// Récupérer les détails complets d'une livraison depuis les tables réelles
router.get('/:shipmentId/details', async (req, res) => {
    try {
        const { shipmentId } = req.params;
        
        console.log(`📦 Fetch shipment details for ID: ${shipmentId}`);
        
        // Récupérer les détails du shipment
        const shipmentQuery = `
            SELECT 
                id,
                "shipmentNo",
                "customerId",
                type,
                "originId",
                "destinationId",
                priority,
                "requestedPickup",
                "requestedDelivery",
                status,
                description,
                quantity,
                uom,
                packaging,
                weight,
                volume,
                stackable,
                carrier,
                "trackingNumber",
                "deliveryAddress",
                "deliveryCity",
                "deliveryZipCode",
                "deliveryCountry",
                "driverId",
                "vehicleId",
                "estimatedDuration",
                "plannedEnd",
                "plannedStart",
                "distanceKm",
                "createdAt",
                "updatedAt"
            FROM "Shipment"
            WHERE id = $1
        `;
        
        const shipmentResult = await pool.query(shipmentQuery, [shipmentId]);
        
        if (shipmentResult.rows.length === 0) {
            return res.status(404).json({
                success: false,
                error: 'Livraison non trouvée'
            });
        }
        
        const shipment = shipmentResult.rows[0];
        
        // Récupérer les informations du client si customerId existe
        let customer = null;
        if (shipment.customerId) {
            console.log(`🔍 Looking for customer with ID: ${shipment.customerId}`);
            const customerQuery = `
                SELECT 
                    id,
                    name,
                    phone,
                    email,
                    address,
                    city,
                    "postalCode"
                FROM "Client"
                WHERE id = $1
            `;
            
            try {
                const customerResult = await pool.query(customerQuery, [shipment.customerId]);
                console.log(`📊 Customer query result: ${customerResult.rows.length} rows found`);
                if (customerResult.rows.length > 0) {
                    customer = customerResult.rows[0];
                    console.log(`✅ Customer found: ${customer.name}, Phone: ${customer.phone}`);
                } else {
                    console.log(`❌ No customer found with ID: ${shipment.customerId}`);
                }
            } catch (customerError) {
                console.log('⚠️ Could not fetch customer data:', customerError.message);
                // Continue without customer data
            }
        } else {
            console.log('⚠️ No customerId in shipment');
        }
        
        // Construire la réponse avec les données de base
        const response = {
            success: true,
            data: {
                shipment: {
                    // Basic shipment info
                    id: shipment.id,
                    shipmentNo: shipment.shipmentNo,
                    customerId: shipment.customerId,
                    type: shipment.type,
                    originId: shipment.originId,
                    destinationId: shipment.destinationId,
                    priority: shipment.priority,
                    requestedPickup: shipment.requestedPickup,
                    requestedDelivery: shipment.requestedDelivery,
                    status: shipment.status,
                    description: shipment.description,
                    quantity: shipment.quantity,
                    uom: shipment.uom,
                    packaging: shipment.packaging,
                    weight: shipment.weight,
                    volume: shipment.volume,
                    stackable: shipment.stackable,
                    carrier: shipment.carrier,
                    trackingNumber: shipment.trackingNumber,
                    deliveryAddress: shipment.deliveryAddress,
                    deliveryCity: shipment.deliveryCity,
                    deliveryZipCode: shipment.deliveryZipCode,
                    deliveryCountry: shipment.deliveryCountry,
                    driverId: shipment.driverId,
                    vehicleId: shipment.vehicleId,
                    estimatedDuration: shipment.estimatedDuration,
                    plannedEnd: shipment.plannedEnd,
                    plannedStart: shipment.plannedStart,
                    distanceKm: shipment.distanceKm,
                    createdAt: shipment.createdAt,
                    updatedAt: shipment.updatedAt,
                    
                    // Informations optionnelles
                    customer: customer,
                    origin: null,
                    destination: null,
                    driver: null,
                    vehicle: null,
                    trip: null,
                    deliveryImages: [],
                    deliveryDocuments: []
                }
            }
        };
        
        console.log(`✅ Shipment details fetched successfully for ${shipment.shipmentNo}`);
        res.json(response);
        
    } catch (error) {
        console.error('❌ Error fetching shipment details:', error);
        res.status(500).json({
            success: false,
            error: 'Erreur serveur: ' + error.message
        });
    }
});

module.exports = router;
