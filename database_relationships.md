# Database Relationships Analysis

This document provides a comprehensive analysis of all foreign key relationships between tables in the Delivery application database.

## Summary
- **Total Tables**: 27
- **Total Foreign Key Relationships**: 48
- **Central Hub Table**: Tenant (referenced by 13 tables)

---

## Relationship Diagram by Table

### 1. Tenant (Central Hub)
**Description**: Multi-tenant isolation table - referenced by most other tables

**Referenced By (13 tables)**:
- Address.tenantId
- Client.tenantId
- Driver.tenantId
- Item.tenantId
- Location.tenantId
- Order.tenantId
- Service.tenantId
- Shipment.tenantId
- ShipmentReturns.tenantId
- Trip.tenantId
- User.tenantId
- Vehicle.tenantId
- VehicleCategory.tenantId

---

### 2. Address
**Foreign Keys To**:
- Tenant.id (via tenantId)

**Referenced By (6 tables)**:
- ClientAddress.addressId
- Driver.addressId
- Order.originAddressId
- Order.destinationAddressId
- ShipmentAddress.addressId
- Trip.departureAddressId
- Trip.returnAddressId
- TripStop.addressId

---

### 3. Client
**Foreign Keys To**:
- Tenant.id (via tenantId)

**Referenced By (3 tables)**:
- ClientAddress.clientId
- ClientContact.clientId
- Order.clientId
- Shipment.clientId

---

### 4. ClientAddress (Junction Table)
**Foreign Keys To**:
- Client.id (via clientId)
- Address.id (via addressId)

**Referenced By**: None

---

### 5. ClientContact
**Foreign Keys To**:
- Client.id (via clientId)

**Referenced By**: None

---

### 6. DeliveryDocument
**Foreign Keys To**:
- Shipment.id (via shipmentId)

**Referenced By**: None

---

### 7. Driver
**Foreign Keys To**:
- Tenant.id (via tenantId)
- Address.id (via addressId)
- Vehicle.id (via assignedVehicleId)

**Referenced By (3 tables)**:
- Shipment.driverId
- Trip.driverId
- User.driverId

---

### 8. ExternalReference
**Foreign Keys To**:
- Shipment.id (via shipmentId)

**Referenced By**: None

---

### 9. Item
**Foreign Keys To**:
- Tenant.id (via tenantId)

**Referenced By (3 tables)**:
- OrderItem.itemId
- ShipmentLine.itemId
- ShipmentReturnDefects.itemId

---

### 10. Location
**Foreign Keys To**:
- Tenant.id (via tenantId)

**Referenced By (2 tables)**:
- Order.originLocationId
- Order.destinationLocationId

---

### 11. Order
**Foreign Keys To**:
- Client.id (via clientId)
- Tenant.id (via tenantId)
- Location.id (via originLocationId)
- Location.id (via destinationLocationId)
- Address.id (via originAddressId)
- Address.id (via destinationAddressId)

**Referenced By (2 tables)**:
- OrderItem.orderId
- Shipment.orderId

---

### 12. OrderItem (Junction Table)
**Foreign Keys To**:
- Order.id (via orderId)
- Item.id (via itemId)

**Referenced By (1 table)**:
- ShipmentLine.orderItemId

---

### 13. Service
**Foreign Keys To**:
- Tenant.id (via tenantId)

**Referenced By**: None

---

### 14. Shipment (Core Logistics Table)
**Foreign Keys To**:
- Order.id (via orderId)
- Client.id (via clientId)
- Driver.id (via driverId)
- Vehicle.id (via vehicleId)
- Tenant.id (via tenantId)
- User.id (via createdById)
- User.id (via updatedById)

**Referenced By (8 tables)**:
- DeliveryDocument.shipmentId
- ExternalReference.shipmentId
- ShipmentAddress.shipmentId
- ShipmentLine.shipmentId
- ShipmentProof.shipmentId
- ShipmentReturns.shipmentId
- TripShipmentLink.shipmentId
- TripStopShipment.shipmentId

---

### 15. ShipmentAddress (Junction Table)
**Foreign Keys To**:
- Shipment.id (via shipmentId)
- Address.id (via addressId)

**Referenced By**: None

---

### 16. ShipmentLine (Junction Table)
**Foreign Keys To**:
- Shipment.id (via shipmentId)
- Item.id (via itemId)
- OrderItem.id (via orderItemId)

**Referenced By**: None

---

### 17. ShipmentProof
**Foreign Keys To**:
- Shipment.id (via shipmentId)

**Referenced By**: None

---

### 18. ShipmentReturnDefects
**Foreign Keys To**:
- ShipmentReturns.id (via shipmentReturnId)
- Item.id (via itemId)

**Referenced By**: None

---

### 19. ShipmentReturns
**Foreign Keys To**:
- Shipment.id (via shipmentId)
- Tenant.id (via tenantId)
- TripShipmentLink.id (via tripShipmentLinkId)

**Referenced By (1 table)**:
- ShipmentReturnDefects.shipmentReturnId

---

### 20. Trip
**Foreign Keys To**:
- Address.id (via departureAddressId)
- Address.id (via returnAddressId)
- Driver.id (via driverId)
- Vehicle.id (via vehicleId)
- Tenant.id (via tenantId)

**Referenced By (2 tables)**:
- TripShipmentLink.tripId
- TripStop.tripId

---

### 21. TripShipmentLink (Junction Table)
**Foreign Keys To**:
- Trip.id (via tripId)
- Shipment.id (via shipmentId)

**Referenced By (1 table)**:
- ShipmentReturns.tripShipmentLinkId

---

### 22. TripStop
**Foreign Keys To**:
- Trip.id (via tripId)
- Address.id (via addressId)

**Referenced By (1 table)**:
- TripStopShipment.tripStopId

---

### 23. TripStopShipment (Junction Table)
**Foreign Keys To**:
- TripStop.id (via tripStopId)
- Shipment.id (via shipmentId)

**Referenced By**: None

---

### 24. User
**Foreign Keys To**:
- Tenant.id (via tenantId)
- Driver.id (via driverId)

**Referenced By (2 tables)**:
- Shipment.createdById
- Shipment.updatedById

---

### 25. Vehicle
**Foreign Keys To**:
- VehicleCategory.id (via categoryId)
- Tenant.id (via tenantId)

**Referenced By (3 tables)**:
- Driver.assignedVehicleId
- Shipment.vehicleId
- Trip.vehicleId
- VehicleMaintenance.vehicleId

---

### 26. VehicleCategory
**Foreign Keys To**:
- Tenant.id (via tenantId)

**Referenced By (1 table)**:
- Vehicle.categoryId

---

### 27. VehicleMaintenance
**Foreign Keys To**:
- Vehicle.id (via vehicleId)

**Referenced By**: None

---

## Key Relationship Patterns

### 1. Multi-Tenant Architecture
All major tables reference **Tenant** for data isolation:
- 13 tables have tenantId foreign keys
- Ensures complete data separation between tenants

### 2. Order → Shipment → Trip Flow
The core logistics workflow follows this pattern:
```
Order → OrderItem → Shipment → ShipmentLine → Trip → TripStop
```

### 3. Address Usage Pattern
**Address** is referenced by multiple entities:
- Client addresses (ClientAddress)
- Driver addresses
- Order locations (origin/destination)
- Trip locations (departure/return)
- Shipment addresses
- Trip stop locations

### 4. Vehicle Management
Vehicle relationships:
```
VehicleCategory → Vehicle → VehicleMaintenance
                    ↓
              Driver.assignedVehicleId
                    ↓
              Shipment.vehicleId
                    ↓
              Trip.vehicleId
```

### 5. User-Driver Linkage
Users can be linked to drivers:
```
User.driverId → Driver.id
```
This allows system users (drivers) to have both authentication credentials and driver profiles.

### 6. Shipment Tracking Chain
Shipment has multiple related tables for tracking:
- **ShipmentAddress**: Pickup/delivery locations
- **ShipmentLine**: Items in shipment
- **ShipmentProof**: Delivery confirmation
- **ShipmentReturns**: Return management
- **ShipmentReturnDefects**: Defect tracking

### 7. Trip-Shipment Relationship
Two-level linking:
```
Trip → TripShipmentLink → Shipment
  ↓                        ↓
TripStop → TripStopShipment → Shipment
```

### 8. Audit Trail Pattern
Several tables track who created/updated records:
- **Shipment**: createdById, updatedById → User
- **User**: Can be linked to Driver for driver accounts

---

## Relationship Groups by Domain

### Customer Management
- Client → ClientAddress → Address
- Client → ClientContact
- Client → Order

### Order Processing
- Order → OrderItem → Item
- Order → Location (origin/destination)
- Order → Address (origin/destination)

### Shipment Management
- Order → Shipment
- Shipment → ShipmentAddress → Address
- Shipment → ShipmentLine → Item/OrderItem
- Shipment → ShipmentProof
- Shipment → ShipmentReturns → ShipmentReturnDefects

### Trip Planning
- Trip → TripShipmentLink → Shipment
- Trip → TripStop → Address
- TripStop → TripStopShipment → Shipment

### Fleet Management
- VehicleCategory → Vehicle → VehicleMaintenance
- Vehicle → Driver (assignedVehicleId)
- Vehicle → Shipment
- Vehicle → Trip

### Driver Management
- Driver → Address
- Driver → Vehicle (assignedVehicleId)
- Driver → Shipment
- Driver → Trip
- Driver → User

### User Management
- User → Tenant
- User → Driver
- User → Shipment (createdById, updatedById)

---

## Circular Relationships

### 1. Driver ↔ Vehicle ↔ Driver
- Driver.assignedVehicleId → Vehicle.id
- Vehicle is referenced by Driver
- This creates a many-to-one relationship (one vehicle assigned to one driver)

### 2. User ↔ Driver
- User.driverId → Driver.id
- Driver has no direct FK to User
- One-to-one relationship (optional)

### 3. Shipment ↔ Order
- Shipment.orderId → Order.id
- Order has no direct FK to Shipment
- One-to-many (one order can have multiple shipments)

### 4. Trip ↔ Shipment
- TripShipmentLink connects Trip and Shipment
- Many-to-many relationship through junction table

---

## Junction Tables Summary

| Junction Table | Connects | Relationship Type |
|----------------|----------|-------------------|
| ClientAddress | Client ↔ Address | Many-to-Many |
| OrderItem | Order ↔ Item | Many-to-Many |
| ShipmentAddress | Shipment ↔ Address | Many-to-Many |
| ShipmentLine | Shipment ↔ Item/OrderItem | Many-to-Many |
| TripShipmentLink | Trip ↔ Shipment | Many-to-Many |
| TripStopShipment | TripStop ↔ Shipment | Many-to-Many |

---

## Foreign Key Count by Table

| Table | FKs Out | FKs In | Total |
|-------|---------|--------|-------|
| Tenant | 0 | 13 | 13 |
| Address | 1 | 8 | 9 |
| Client | 1 | 4 | 5 |
| ClientAddress | 2 | 0 | 2 |
| ClientContact | 1 | 0 | 1 |
| DeliveryDocument | 1 | 0 | 1 |
| Driver | 3 | 3 | 6 |
| ExternalReference | 1 | 0 | 1 |
| Item | 1 | 3 | 4 |
| Location | 1 | 2 | 3 |
| Order | 6 | 2 | 8 |
| OrderItem | 2 | 1 | 3 |
| Service | 1 | 0 | 1 |
| Shipment | 7 | 8 | 15 |
| ShipmentAddress | 2 | 0 | 2 |
| ShipmentLine | 3 | 0 | 3 |
| ShipmentProof | 1 | 0 | 1 |
| ShipmentReturnDefects | 2 | 0 | 2 |
| ShipmentReturns | 3 | 1 | 4 |
| Trip | 5 | 2 | 7 |
| TripShipmentLink | 2 | 1 | 3 |
| TripStop | 2 | 1 | 3 |
| TripStopShipment | 2 | 0 | 2 |
| User | 2 | 2 | 4 |
| Vehicle | 2 | 4 | 6 |
| VehicleCategory | 1 | 1 | 2 |
| VehicleMaintenance | 1 | 0 | 1 |

---

## Most Connected Tables

### Top 5 by Total Relationships:
1. **Shipment** (15 relationships) - Core logistics hub
2. **Tenant** (13 relationships) - Multi-tenant isolation
3. **Address** (9 relationships) - Location management
4. **Order** (8 relationships) - Order processing
5. **Trip** (7 relationships) - Trip planning

---

## Cascade Deletion Considerations

**Critical Tables** (high impact if deleted):
- **Tenant**: Deleting a tenant would cascade to 13 tables
- **Shipment**: Deleting affects 8 related tables
- **Address**: Deleting affects 8 related tables
- **Order**: Deleting affects 2 related tables
- **Vehicle**: Deleting affects 4 related tables

**Recommendation**: Implement soft delete or careful cascade delete strategies for these tables.
