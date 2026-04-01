# DATABASE ANALYSIS REPORT

## STEP 1: EXISTING DATA ANALYSIS

### Trip Table Structure
**Sample Data:**
- **ID Format**: Auto-increment integers (1, 2, 3, 4, 5)
- **tripId Format**: "TRIP-2026-001", "TRIP-2026-002", etc.
- **Status Values**: "PLANNING", "IN_PROGRESS", "COMPLETED"
- **Date Format**: ISO timestamps (e.g., "2026-03-18T11:41:32.445Z")
- **Relationships**: driverId (3, 4, 5), vehicleId (3, 4), depotId (3, 4, 5, 9, 10)

### TripShipmentLink Table Structure
**Sample Data:**
- **ID Format**: Auto-increment integers
- **Status Values**: "NON_DEMARRE", "EN_COURS", "TERMINE"
- **Sequence**: Integer sequence (1, 2, 3...)
- **Role**: "BOTH" (only value seen)
- **Boolean fields**: podDone (false), returnsDone (false)

### Shipment Table Structure
**Sample Data:**
- **ID Format**: Auto-increment integers
- **shipmentNo Format**: "EXP-2026-001", "EXP-2026-201", "EXP-2026-203", etc.
- **Status Values**: "TO_PLAN", "EXPEDITION", "DELIVERED"
- **Type Values**: "OUTBOUND", "INBOUND", "TRANSFER"
- **Priority Values**: "HIGH", "LOW", "MEDIUM"
- **Date Format**: ISO timestamps
- **Relationships**: 
  - customerId (1, 2, 3, 7)
  - originId (3, 4, 9)
  - destinationId (1, 3, 4, 10)
  - driverId (3, 4, 5, 8)
  - vehicleId (3, 4)

### ShipmentLine Table Structure
**Sample Data:**
- **ID Format**: Auto-increment integers
- **Quantity**: Double precision (100, 5, 50, 39, 6)
- **UOM Values**: "PCS", "PAL", "CTN"
- **Relationships**: 
  - shipmentId (1, 2, 3, 4, 5)
  - itemId (1, 2, 3)

## STEP 2: STYLE DETECTION

### Naming Conventions
- **Trip IDs**: "TRIP-YYYY-###" format (e.g., "TRIP-2026-001")
- **Shipment Numbers**: "EXP-YYYY-###" format (e.g., "EXP-2026-001")
- **Item Numbers**: "ITEM-###" format (e.g., "ITEM-001")

### Status Mapping (CRITICAL)
Based on the memory system, the CORRECT status mappings are:
- **TripShipmentLink.status** → **Shipment.status**
  - "NON_DEMARRE" → "TO_PLAN"
  - "EN_COURS" → "EXPEDITION"  
  - "TERMINE" → "DELIVERED"

### Available Data for Reuse

**Clients (10 available):**
- Client A (id: 1, 3)
- Client B (id: 2, 4)
- Banque Internationale Genève (id: 5)
- Swiss Pharma AG (id: 6)
- Lausanne Technologies (id: 7)
- Bâle Industries SA (id: 8)
- TechnoPlus France (id: 9)
- SantéPlus Médical (id: 10)

**Locations (10 available):**
- Entrepôt Principal, Lyon (id: 1, 3, 5)
- Client Default, Paris (id: 2, 4, 6)
- Genève - Siège Suisse (id: 7)
- Zurich - Dépôt Suisse (id: 8)
- Lausanne - Agence (id: 9)
- Bâle - Entrepôt (id: 10)

**Vehicles (4 available):**
- Peugeot Boxer, EF-456-GH (id: 2, 4)
- Renault Master, AB-123-CD (id: 1, 3)

**Items (11 available):**
- ITEM-001: Produit A - Boîte standard
- ITEM-002: Produit B - Palette
- ITEM-003: Produit C - Carton
- ITEM-008: Électronique - Ordinateurs portables
- ITEM-009: Matériel médical - Stéthoscopes
- ITEM-010: Produits alimentaires - Conserves
- ITEM-011: Textile - Vêtements
- ITEM-012: Mobilier - Chaises de bureau
- ITEM-013: Outillage - Boîte à outils
- ITEM-014: Livrés - Livres scolaires

## STEP 3: DATA GENERATION PLAN

### Target Dates & Driver
- **Driver ID**: 5
- **Dates**: 1 April 2026, 2 April 2026, 3 April 2026
- **Trip Statuses**: 
  - 1 April → PLANNING
  - 2 April → IN_PROGRESS
  - 3 April → COMPLETED

### Planned Structure
```
Trip (1 per day) → TripShipmentLink (5-10 per trip) → Shipment (linked) → ShipmentLine (1-3 per shipment)
```

### Next Steps
1. Generate Trip records for 3 days
2. Generate Shipment records (reusing existing clients/locations)
3. Generate TripShipmentLink records (connecting trips to shipments)
4. Generate ShipmentLine records (using existing items)

## STEP 4: STATUS CONSISTENCY RULES

### Critical Status Mapping
- TripShipmentLink.status = "NON_DEMARRE" → Shipment.status = "TO_PLAN"
- TripShipmentLink.status = "EN_COURS" → Shipment.status = "EXPEDITION"
- TripShipmentLink.status = "TERMINE" → Shipment.status = "DELIVERED"

### Trip Status Progression
- Day 1 (1 April): Trip.status = "PLANNING"
- Day 2 (2 April): Trip.status = "IN_PROGRESS"  
- Day 3 (3 April): Trip.status = "COMPLETED"

## STEP 5: ID SEQUENCING

### Current Maximum IDs
- Trip: Need to check current max
- Shipment: Need to check current max (saw up to 8)
- TripShipmentLink: Need to check current max
- ShipmentLine: Need to check current max (saw up to 5)

### Next Available IDs
Will query for exact maximum values before insertion to avoid conflicts.

## CONCLUSION

The database follows a clear pattern with:
- Consistent naming conventions
- Proper status mappings (CRITICAL for consistency)
- Good separation of concerns
- Reusable reference data

Ready to proceed with data generation following these exact patterns.
