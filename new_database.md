# New Database Documentation

This document contains the structure and documentation for the PostgreSQL database tables in the Delivery application.

**Total Tables**: 27

**Table List**:
1. Address
2. Client
3. ClientAddress
4. ClientContact
5. DeliveryDocument
6. Driver
7. ExternalReference
8. Item
9. Location
10. Order
11. OrderItem
12. Service
13. Shipment
14. ShipmentAddress
15. ShipmentLine
16. ShipmentProof
17. ShipmentReturnDefects
18. ShipmentReturns
19. Tenant
20. Trip
21. TripShipmentLink
22. TripStop
23. TripStopShipment
24. User
25. Vehicle
26. VehicleCategory
27. VehicleMaintenance

---

## Table: Address

**Description**: Stores address information for depots, warehouses, and other locations with contact details.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Address_id_seq"'::regclass) - Auto-incrementing primary key
- **label**: text NOT NULL - Address label/name (e.g., "Dépôt Paris")
- **address1**: text NOT NULL - Primary address line
- **address2**: text NULL - Secondary address line
- **city**: text NOT NULL - City name
- **postalCode**: text NOT NULL - Postal/ZIP code
- **country**: text NOT NULL DEFAULT 'Tunisia' - Country name
- **state**: text NULL - State/Province
- **latitude**: double precision NULL - Geographic latitude
- **longitude**: double precision NULL - Geographic longitude
- **contactName**: text NULL - Contact person name
- **contactPhone**: text NULL - Contact phone number
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: tenantId references Tenant(id)
- NOT NULL constraints: id, label, address1, city, postalCode, country, tenantId, createdAt, updatedAt
- Default values: country = 'Tunisia'

### Example Data
```json
Record 1: {
  "id": 1,
  "label": "Dépôt Paris",
  "address1": "123 Rue de la Pompe",
  "address2": "Zone Industrielle Paris Nord",
  "city": "Paris",
  "postalCode": "75017",
  "country": "France",
  "state": null,
  "latitude": null,
  "longitude": null,
  "contactName": "Dépôt Principal",
  "contactPhone": "+33 1 42 12 34 56",
  "tenantId": 1,
  "createdAt": "2026-07-10 14:52:08.973",
  "updatedAt": "2026-07-10 14:52:08.973"
}
```

---

## Table: Client

**Description**: Stores client/customer information including company details and contact information.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Client_id_seq"'::regclass) - Auto-incrementing primary key
- **code**: text NOT NULL - Unique client code (e.g., "AUCHAN001")
- **name**: text NOT NULL - Client name
- **firstName**: text NULL - Contact person first name
- **companyName**: text NULL - Company legal name
- **email**: text NULL - Email address
- **phone**: text NULL - Phone number
- **taxNumber**: text NULL - Tax/VAT number
- **isActive**: boolean NOT NULL DEFAULT true - Whether client is active
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: tenantId references Tenant(id)
- NOT NULL constraints: id, code, name, isActive, tenantId, createdAt, updatedAt
- Default values: isActive = true

### Example Data
```json
Record 1: {
  "id": 3,
  "code": "AUCHAN001",
  "name": "Auchan France",
  "firstName": null,
  "companyName": "Auchan SA",
  "email": "logistique@auchan.fr",
  "phone": "+33 3 20 66 00 00",
  "taxNumber": "FR98765432109",
  "isActive": true,
  "tenantId": 1,
  "createdAt": "2026-07-10 14:52:10.353",
  "updatedAt": "2026-07-10 14:52:10.353"
}
```

---

## Table: ClientAddress

**Description**: Junction table linking clients to their addresses with type classification and default address selection.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ClientAddress_id_seq"'::regclass) - Auto-incrementing primary key
- **clientId**: integer NOT NULL - Foreign key to Client table
- **addressId**: integer NOT NULL - Foreign key to Address table
- **type**: USER-DEFINED NOT NULL DEFAULT 'OTHER'::"AddressType" - Address type (enum)
- **isDefault**: boolean NOT NULL DEFAULT false - Whether this is the default address
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp

### Relationships
- **clientId** → Client.id (Foreign Key)
- **addressId** → Address.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: clientId references Client(id), addressId references Address(id)
- NOT NULL constraints: id, clientId, addressId, type, isDefault, createdAt
- Default values: type = 'OTHER', isDefault = false
- Uses custom enum type: AddressType

### Example Data
```json
Record 1: {
  "id": 3,
  "clientId": 3,
  "addressId": 6,
  "type": "WAREHOUSE",
  "isDefault": true,
  "createdAt": "2026-07-10 14:52:11.541"
}
```

---

## Table: ClientContact

**Description**: Stores contact person information for clients with role classification and activity status.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ClientContact_id_seq"'::regclass) - Auto-incrementing primary key
- **clientId**: integer NOT NULL - Foreign key to Client table
- **firstName**: text NOT NULL - Contact person first name
- **lastName**: text NOT NULL - Contact person last name
- **email**: text NULL - Email address
- **phone**: text NULL - Phone number
- **role**: text NULL - Contact role/job title
- **isActive**: boolean NOT NULL DEFAULT true - Whether contact is active
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp

### Relationships
- **clientId** → Client.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: clientId references Client(id)
- NOT NULL constraints: id, clientId, firstName, lastName, isActive, createdAt
- Default values: isActive = true

### Example Data
```json
Record 1: {
  "id": 4,
  "clientId": 3,
  "firstName": "Pierre",
  "lastName": "Bernard",
  "email": "pierre.bernard@auchan.fr",
  "phone": "+33 3 20 66 01 01",
  "role": "Responsable Logistique",
  "isActive": true,
  "createdAt": "2026-07-10 14:52:11.044"
}
```

---

## Table: DeliveryDocument

**Description**: Stores delivery documents related to shipments, including bill of lading information and signing status.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"DeliveryDocument_id_seq"'::regclass) - Auto-incrementing primary key
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **blNumber**: text NOT NULL - Bill of Lading number
- **pdfUrl**: text NULL - URL to PDF document
- **signed**: boolean NOT NULL DEFAULT false - Whether document has been signed
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp

### Relationships
- **shipmentId** → Shipment.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: shipmentId references Shipment(id)
- NOT NULL constraints: id, shipmentId, blNumber, signed, createdAt
- Default values: signed = false

### Example Data
No data found in DeliveryDocument table

---

## Table: Driver

**Description**: Stores driver information including employment details, licensing, contact information, and vehicle assignments.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Driver_id_seq"'::regclass) - Auto-incrementing primary key
- **firstName**: text NOT NULL - Driver first name
- **lastName**: text NOT NULL - Driver last name
- **phone**: text NULL - Phone number
- **email**: text NULL - Email address
- **licenseNumber**: text NULL - Driver license number
- **licenseExpiry**: timestamp without time zone NULL - License expiration date
- **licenseIssueDate**: timestamp without time zone NULL - License issue date
- **employmentType**: USER-DEFINED NOT NULL DEFAULT 'FULL_TIME'::"EmploymentType" - Employment type (enum)
- **contractHoursWeek**: integer NOT NULL DEFAULT 40 - Weekly contract hours
- **status**: USER-DEFINED NOT NULL DEFAULT 'ACTIF'::"DriverStatus" - Driver status (enum)
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **addressId**: integer NULL - Foreign key to Address table
- **dateOfBirth**: timestamp without time zone NULL - Date of birth
- **hireDate**: timestamp without time zone NULL - Hire date
- **assignedVehicleId**: integer NULL - Foreign key to Vehicle table (assigned vehicle)
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record last update timestamp

### Relationships
- **tenantId** → Tenant.id (Foreign Key)
- **addressId** → Address.id (Foreign Key)
- **assignedVehicleId** → Vehicle.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: tenantId references Tenant(id), addressId references Address(id), assignedVehicleId references Vehicle(id)
- NOT NULL constraints: id, firstName, lastName, employmentType, contractHoursWeek, status, tenantId, createdAt, updatedAt
- Default values: employmentType = 'FULL_TIME', contractHoursWeek = 40, status = 'ACTIF'
- Uses custom enum types: EmploymentType, DriverStatus

### Example Data
```json
Record 1: {
  "id": 1,
  "firstName": "Pierre",
  "lastName": "Bernard",
  "phone": "+33 6 12 34 56 78",
  "email": "pierre.bernard@almakom.eu",
  "licenseNumber": "FR-75-123456-78",
  "licenseExpiry": "2025-12-31 00:00:00",
  "licenseIssueDate": null,
  "employmentType": "FULL_TIME",
  "contractHoursWeek": 40,
  "status": "ACTIF",
  "tenantId": 1,
  "addressId": null,
  "dateOfBirth": null,
  "hireDate": "2023-01-15 00:00:00",
  "assignedVehicleId": 1,
  "createdAt": "2026-07-10 14:52:12.699",
  "updatedAt": "2026-07-10 14:52:13.766"
}
```

---

## Table: ExternalReference

**Description**: Stores external system references linked to shipments for integration purposes with external systems.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ExternalReference_id_seq"'::regclass) - Auto-incrementing primary key
- **sourceSystem**: text NOT NULL - Name of the external system
- **externalId**: text NOT NULL - Identifier in the external system
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp

### Relationships
- **shipmentId** → Shipment.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: shipmentId references Shipment(id)
- NOT NULL constraints: id, sourceSystem, externalId, shipmentId, createdAt

### Example Data
No data found in ExternalReference table

---

## Table: Item

**Description**: Stores product/item information including descriptions, dimensions, categories, and inventory details with SKU and barcode tracking.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Item_id_seq"'::regclass) - Auto-incrementing primary key
- **sku**: text NOT NULL - Stock Keeping Unit (unique identifier)
- **barcode**: text NULL - Product barcode
- **itemNo**: text NOT NULL - Item number
- **description**: text NOT NULL - Item description
- **unit**: text NOT NULL DEFAULT 'PCS' - Unit of measurement
- **weight**: double precision NULL - Item weight
- **volume**: double precision NULL - Item volume
- **category**: text NULL - Item category
- **isActive**: boolean NOT NULL DEFAULT true - Whether item is active
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: tenantId references Tenant(id)
- NOT NULL constraints: id, sku, itemNo, description, unit, isActive, tenantId, createdAt, updatedAt
- Default values: unit = 'PCS', isActive = true

### Example Data
```json
Record 1: {
  "id": 8,
  "sku": "PAL-BEURRE-250G",
  "barcode": "3301234567895",
  "itemNo": "ITM006",
  "description": "Palette Beurre 250g",
  "unit": "PCS",
  "weight": 180.0,
  "volume": 0.4,
  "category": "Produits Laitiers",
  "isActive": true,
  "tenantId": 1,
  "createdAt": "2026-07-10 15:12:33.115",
  "updatedAt": "2026-07-10 15:12:33.115"
}
```

---

## Table: Location

**Description**: Stores location information including warehouses, depots, and hubs with geographic coordinates and contact details.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Location_id_seq"'::regclass) - Auto-incrementing primary key
- **code**: text NOT NULL - Location code (e.g., "DEP-PAR-01")
- **name**: text NOT NULL - Location name
- **address1**: text NOT NULL - Primary address line
- **address2**: text NULL - Secondary address line
- **city**: text NOT NULL - City name
- **postalCode**: text NOT NULL - Postal/ZIP code
- **country**: text NOT NULL DEFAULT 'Tunisia' - Country name
- **latitude**: double precision NULL - Geographic latitude
- **longitude**: double precision NULL - Geographic longitude
- **contactName**: text NULL - Contact person name
- **contactPhone**: text NULL - Contact phone number
- **isActive**: boolean NOT NULL DEFAULT true - Whether location is active
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp
- **type**: USER-DEFINED NOT NULL DEFAULT 'DEPOT'::"LocationType" - Location type (enum)

### Relationships
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: tenantId references Tenant(id)
- NOT NULL constraints: id, code, name, address1, city, postalCode, country, isActive, tenantId, type, createdAt, updatedAt
- Default values: country = 'Tunisia', isActive = true, type = 'DEPOT'
- Uses custom enum type: LocationType

### Example Data
```json
Record 1: {
  "id": 33,
  "code": "DEP-PAR-01",
  "name": "Dépôt Paris Nord",
  "address1": "125 Avenue de la Logistique",
  "address2": null,
  "city": "Paris",
  "postalCode": "75018",
  "country": "France",
  "latitude": null,
  "longitude": null,
  "contactName": null,
  "contactPhone": null,
  "isActive": true,
  "tenantId": 1,
  "createdAt": "2026-07-10 21:56:39.19",
  "updatedAt": "2026-07-10 21:56:39.19",
  "type": "DEPOT"
}
```

---

## Table: Order

**Description**: Stores customer orders with comprehensive tracking including priority, delivery information, contact details, and location references.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Order_id_seq"'::regclass) - Auto-incrementing primary key
- **orderNumber**: text NOT NULL - Unique order number
- **clientId**: integer NOT NULL - Foreign key to Client table
- **orderDate**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Order creation date
- **requestedDelivery**: timestamp without time zone NULL - Requested delivery date
- **priority**: USER-DEFINED NOT NULL DEFAULT 'MEDIUM'::"Priority" - Order priority (enum)
- **totalWeight**: double precision NULL - Total order weight
- **totalVolume**: double precision NULL - Total order volume
- **notes**: text NULL - Order notes
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp
- **status**: USER-DEFINED NOT NULL DEFAULT 'BROUILLON'::"OrderStatus" - Order status (enum)
- **clientAddress**: text NULL - Client address
- **clientCity**: text NULL - Client city
- **clientPostalCode**: text NULL - Client postal code
- **clientCountry**: text NULL - Client country
- **originLocationId**: integer NULL - Foreign key to Location table (origin)
- **destinationLocationId**: integer NULL - Foreign key to Location table (destination)
- **mainContactName**: text NULL - Main contact person name
- **mainContactPhone**: text NULL - Main contact phone
- **mainContactEmail**: text NULL - Main contact email
- **originAddressId**: integer NULL - Foreign key to Address table (origin)
- **destinationAddressId**: integer NULL - Foreign key to Address table (destination)

### Relationships
- **clientId** → Client.id (Foreign Key)
- **tenantId** → Tenant.id (Foreign Key)
- **originLocationId** → Location.id (Foreign Key)
- **destinationLocationId** → Location.id (Foreign Key)
- **originAddressId** → Address.id (Foreign Key)
- **destinationAddressId** → Address.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: clientId, tenantId, originLocationId, destinationLocationId, originAddressId, destinationAddressId
- NOT NULL constraints: id, orderNumber, clientId, orderDate, priority, tenantId, createdAt, updatedAt, status
- Default values: orderDate = CURRENT_TIMESTAMP, priority = 'MEDIUM', status = 'BROUILLON'
- Uses custom enum types: Priority, OrderStatus

### Example Data
```json
Record 1: {
  "id": 6,
  "orderNumber": "ORD-20260710-3998",
  "clientId": 3,
  "orderDate": "2026-07-10 18:34:24.815",
  "requestedDelivery": null,
  "priority": "MEDIUM",
  "totalWeight": null,
  "totalVolume": null,
  "notes": null,
  "tenantId": 1,
  "createdAt": "2026-07-10 18:34:24.819",
  "updatedAt": "2026-07-10 18:34:24.819",
  "status": "BROUILLON",
  "clientAddress": null,
  "clientCity": null,
  "clientPostalCode": null,
  "clientCountry": null,
  "originLocationId": null,
  "destinationLocationId": null,
  "mainContactName": "Trey Research",
  "mainContactPhone": "+33 3 20 66 00 00",
  "mainContactEmail": "logistique@auchan.fr",
  "originAddressId": null,
  "destinationAddressId": null
}
```

---

## Table: OrderItem

**Description**: Junction table linking orders to items with quantities, measurements, and packaging information.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"OrderItem_id_seq"'::regclass) - Auto-incrementing primary key
- **orderId**: integer NOT NULL - Foreign key to Order table
- **itemId**: integer NULL - Foreign key to Item table
- **description**: text NOT NULL - Item description
- **quantity**: double precision NOT NULL - Quantity ordered
- **unit**: text NOT NULL DEFAULT 'PCS' - Unit of measurement
- **weight**: double precision NULL - Total weight for this line
- **volume**: double precision NULL - Total volume for this line
- **packageType**: text NULL - Package type
- **notes**: text NULL - Line item notes

### Relationships
- **orderId** → Order.id (Foreign Key)
- **itemId** → Item.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: orderId references Order(id), itemId references Item(id)
- NOT NULL constraints: id, orderId, description, quantity, unit
- Default values: unit = 'PCS'

### Example Data
```json
Record 1: {
  "id": 27,
  "orderId": 6,
  "itemId": null,
  "description": "- Service de livraison standard",
  "quantity": 1.0,
  "unit": "PCS",
  "weight": null,
  "volume": null,
  "packageType": null,
  "notes": null
}
```

---

## Table: Service

**Description**: Stores available delivery services with pricing and descriptions for order management.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Service_id_seq"'::regclass) - Auto-incrementing primary key
- **name**: text NOT NULL - Service name
- **description**: text NULL - Service description
- **price**: double precision NULL - Service price
- **isActive**: boolean NOT NULL DEFAULT true - Whether service is active
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: tenantId references Tenant(id)
- NOT NULL constraints: id, name, isActive, tenantId, createdAt, updatedAt
- Default values: isActive = true

### Example Data
```json
Record 1: {
  "id": 1,
  "name": "Livraison Standard",
  "description": "Service de livraison standard",
  "price": 15.99,
  "isActive": true,
  "tenantId": 1,
  "createdAt": "2026-07-10 21:25:51.972",
  "updatedAt": "2026-07-10 21:25:51.972"
}
```

---

## Table: Shipment

**Description**: Core shipment management table with comprehensive logistics information including planning, tracking, and execution details.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Shipment_id_seq"'::regclass) - Auto-incrementing primary key
- **shipmentNo**: text NOT NULL - Unique shipment number
- **orderId**: integer NULL - Foreign key to Order table
- **clientId**: integer NULL - Foreign key to Client table
- **type**: USER-DEFINED NOT NULL - Shipment type (enum: OUTBOUND, INBOUND, TRANSFER)
- **priority**: USER-DEFINED NOT NULL - Shipment priority (enum)
- **requestedPickup**: timestamp without time zone NULL - Requested pickup time
- **requestedDelivery**: timestamp without time zone NULL - Requested delivery time
- **plannedStart**: timestamp without time zone NULL - Planned start time
- **plannedEnd**: timestamp without time zone NULL - Planned end time
- **description**: text NOT NULL - Shipment description
- **trackingNumber**: text NULL - Tracking number
- **driverId**: integer NULL - Foreign key to Driver table
- **vehicleId**: integer NULL - Foreign key to Vehicle table
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp
- **createdById**: integer NULL - Foreign key to User table (creator)
- **updatedById**: integer NULL - Foreign key to User table (updater)
- **distanceKm**: double precision NULL - Distance in kilometers
- **estimatedDuration**: integer NULL - Estimated duration in minutes
- **status**: USER-DEFINED NOT NULL DEFAULT 'TO_PLAN'::"ShipmentStatus" - Shipment status (enum)

### Relationships
- **orderId** → Order.id (Foreign Key)
- **clientId** → Client.id (Foreign Key)
- **driverId** → Driver.id (Foreign Key)
- **vehicleId** → Vehicle.id (Foreign Key)
- **tenantId** → Tenant.id (Foreign Key)
- **createdById** → User.id (Foreign Key)
- **updatedById** → User.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: orderId, clientId, driverId, vehicleId, tenantId, createdById, updatedById
- NOT NULL constraints: id, shipmentNo, type, priority, description, tenantId, createdAt, updatedAt, status
- Default values: status = 'TO_PLAN'
- Uses custom enum types: ShipmentType, ShipmentPriority, ShipmentStatus

### Example Data
```json
Record 1: {
  "id": 23,
  "shipmentNo": "SHP-20260712-9103",
  "orderId": 40,
  "clientId": 15,
  "type": "OUTBOUND",
  "priority": "MEDIUM",
  "requestedPickup": null,
  "requestedDelivery": "2026-07-10 02:26:00",
  "plannedStart": null,
  "plannedEnd": null,
  "description": "Expédition pour commande ORD-20260712-7354",
  "trackingNumber": null,
  "driverId": 1,
  "vehicleId": 1,
  "tenantId": 1,
  "createdAt": "2026-07-12 02:27:20.157",
  "updatedAt": "2026-07-12 02:27:20.157",
  "createdById": 11,
  "updatedById": 11,
  "distanceKm": null,
  "estimatedDuration": null,
  "status": "EXPEDITION"
}
```

---

## Table: ShipmentAddress

**Description**: Junction table linking shipments to addresses with sequence ordering and type classification for pickup/delivery locations.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ShipmentAddress_id_seq"'::regclass) - Auto-incrementing primary key
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **addressId**: integer NOT NULL - Foreign key to Address table
- **sequence**: integer NOT NULL - Stop sequence order
- **type**: USER-DEFINED NOT NULL DEFAULT 'DELIVERY'::"AddressType" - Address type (enum)
- **notes**: text NULL - Address notes
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp

### Relationships
- **shipmentId** → Shipment.id (Foreign Key)
- **addressId** → Address.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: shipmentId references Shipment(id), addressId references Address(id)
- NOT NULL constraints: id, shipmentId, addressId, sequence, type, createdAt
- Default values: type = 'DELIVERY'
- Uses custom enum type: AddressType

### Example Data
```json
Record 1: {
  "id": 13,
  "shipmentId": 4,
  "addressId": 146,
  "sequence": 1,
  "type": "DELIVERY",
  "notes": null,
  "createdAt": "2026-07-11 13:03:19.773"
}
```

---

## Table: ShipmentLine

**Description**: Detailed line items within shipments, linking shipments to specific items with quantities, measurements, and packaging information.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ShipmentLine_id_seq"'::regclass) - Auto-incrementing primary key
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **itemId**: integer NULL - Foreign key to Item table
- **description**: text NULL - Item description
- **quantity**: double precision NOT NULL - Quantity of items
- **unit**: text NOT NULL - Unit of measurement
- **weight**: double precision NULL - Total weight for this line
- **volume**: double precision NULL - Total volume for this line
- **packageType**: text NULL - Package type
- **orderItemId**: integer NULL - Foreign key to OrderItem table

### Relationships
- **shipmentId** → Shipment.id (Foreign Key)
- **itemId** → Item.id (Foreign Key)
- **orderItemId** → OrderItem.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: shipmentId references Shipment(id), itemId references Item(id), orderItemId references OrderItem(id)
- NOT NULL constraints: id, shipmentId, quantity, unit

### Example Data
```json
Record 1: {
  "id": 1,
  "shipmentId": 4,
  "itemId": null,
  "description": "Palette Légumes Mixtes Surgelés",
  "quantity": 1.0,
  "unit": "PCS",
  "weight": null,
  "volume": null,
  "packageType": null,
  "orderItemId": null
}
```

---

## Table: ShipmentProof

**Description**: Stores delivery proof documents including images and signatures for shipments with delivery confirmation tracking.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ShipmentProof_id_seq"'::regclass) - Auto-incrementing primary key
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **imageUrl**: text NOT NULL - URL to delivery proof image (base64 encoded)
- **signatureUrl**: text NULL - URL to signature image
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **comment**: text NULL - Delivery comments
- **deliveredAt**: timestamp without time zone NULL - Delivery timestamp
- **signedBy**: text NULL - Person who signed

### Relationships
- **shipmentId** → Shipment.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: shipmentId references Shipment(id)
- NOT NULL constraints: id, shipmentId, imageUrl, createdAt

### Example Data
```json
Record 1: {
  "id": 1,
  "shipmentId": 4,
  "imageUrl": "data:image/png;base64,...[base64 encoded image data]",
  "signatureUrl": null,
  "createdAt": "2026-07-12 00:59:20.012",
  "comment": null,
  "deliveredAt": null,
  "signedBy": null
}
```

---

## Table: ShipmentReturnDefects

**Description**: Stores defect information for returned shipments with item-level defect tracking and quantities.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ShipmentReturnDefects_id_seq"'::regclass) - Auto-incrementing primary key
- **shipmentReturnId**: integer NOT NULL - Foreign key to ShipmentReturns table
- **itemId**: integer NULL - Foreign key to Item table
- **defectType**: text NOT NULL DEFAULT 'OTHER' - Type of defect
- **defectDescription**: text NULL - Description of the defect
- **quantity**: double precision NULL - Quantity of defective items
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **reason**: text NULL - Reason for defect

### Relationships
- **shipmentReturnId** → ShipmentReturns.id (Foreign Key)
- **itemId** → Item.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: shipmentReturnId references ShipmentReturns(id), itemId references Item(id)
- NOT NULL constraints: id, shipmentReturnId, defectType, createdAt
- Default values: defectType = 'OTHER'

### Example Data
```json
Record 1: {
  "id": 1,
  "shipmentReturnId": 5,
  "itemId": null,
  "defectType": "OTHER",
  "defectDescription": "- Palette Poisson Surgelé",
  "quantity": 1.0,
  "createdAt": "2026-07-12 01:55:14.934",
  "reason": "cass"
}
```

---

## Table: ShipmentReturns

**Description**: Comprehensive shipment returns management with packaging recovery tracking, status management, and proof documentation.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ShipmentReturns_id_seq"'::regclass) - Auto-incrementing primary key
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **tripShipmentLinkId**: integer NULL - Foreign key to TripShipmentLink table
- **returnStatus**: text NOT NULL DEFAULT 'PENDING' - Return status
- **returnReason**: text NULL - Reason for return
- **returnDate**: timestamp without time zone NULL - Return date
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record last update timestamp
- **autre**: text NULL - Other items (French)
- **bouteilles**: integer NULL - Bottles count (French)
- **caisses**: integer NULL - Boxes/crates count (French)
- **comment**: text NULL - Return comments
- **futs**: integer NULL - Kegs/drums count (French)
- **packagesrecovered**: integer NULL - Packages recovered
- **packagingrecovered**: integer NULL - Packaging recovered
- **palettes**: integer NULL - Pallets count (French)
- **proofimageurl**: text NULL - URL to proof image

### Relationships
- **shipmentId** → Shipment.id (Foreign Key)
- **tenantId** → Tenant.id (Foreign Key)
- **tripShipmentLinkId** → TripShipmentLink.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: shipmentId references Shipment(id), tenantId references Tenant(id), tripShipmentLinkId references TripShipmentLink(id)
- NOT NULL constraints: id, shipmentId, tenantId, returnStatus, createdAt, updatedAt
- Default values: returnStatus = 'PENDING'

### Example Data
```json
Record 1: {
  "id": 1,
  "shipmentId": 4,
  "tenantId": 1,
  "tripShipmentLinkId": null,
  "returnStatus": "PENDING",
  "returnReason": null,
  "returnDate": "2026-07-11 23:18:48.673",
  "createdAt": "2026-07-11 23:18:48.678",
  "updatedAt": "2026-07-11 23:18:48.678",
  "autre": null,
  "bouteilles": null,
  "caisses": null,
  "comment": null,
  "futs": null,
  "packagesrecovered": null,
  "packagingrecovered": null,
  "palettes": null,
  "proofimageurl": null
}
```

---

## Table: Tenant

**Description**: Multi-tenant support table storing tenant information for data isolation across the application.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Tenant_id_seq"'::regclass) - Auto-incrementing primary key
- **name**: text NOT NULL - Tenant name
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp

### Relationships
This table is referenced by many other tables for multi-tenant data isolation.

### Constraints
- Primary Key: id
- NOT NULL constraints: id, name, createdAt
- Unique constraint: name

### Example Data
```json
Record 1: {
  "id": 1,
  "name": "ALMAKOM Europe",
  "createdAt": "2026-07-10 14:52:06.924"
}
```

---

## Table: Trip

**Description**: Core trip management table organizing driver schedules and vehicle assignments for delivery operations with comprehensive planning and tracking.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Trip_id_seq"'::regclass) - Auto-incrementing primary key
- **tripNumber**: text NOT NULL - Unique trip identifier
- **departureAddressId**: integer NULL - Foreign key to Address table (departure location)
- **returnAddressId**: integer NULL - Foreign key to Address table (return location)
- **driverId**: integer NULL - Foreign key to Driver table
- **vehicleId**: integer NULL - Foreign key to Vehicle table
- **tripDate**: timestamp without time zone NOT NULL - Scheduled trip date
- **status**: USER-DEFINED NOT NULL DEFAULT 'PLANNING'::"TripStatus" - Trip status (enum)
- **priority**: USER-DEFINED NOT NULL DEFAULT 'MEDIUM'::"Priority" - Trip priority (enum)
- **estimatedDuration**: integer NULL - Estimated duration in minutes
- **estimatedDistance**: double precision NULL - Estimated distance in kilometers
- **notes**: text NULL - Trip notes
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **departureAddressId** → Address.id (Foreign Key)
- **returnAddressId** → Address.id (Foreign Key)
- **driverId** → Driver.id (Foreign Key)
- **vehicleId** → Vehicle.id (Foreign Key)
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: departureAddressId, returnAddressId, driverId, vehicleId, tenantId
- NOT NULL constraints: id, tripNumber, tripDate, status, priority, tenantId, createdAt, updatedAt
- Default values: status = 'PLANNING', priority = 'MEDIUM'
- Uses custom enum types: TripStatus, Priority

### Example Data
```json
Record 1: {
  "id": 7,
  "tripNumber": "TRP-20260711-2605",
  "departureAddressId": 143,
  "returnAddressId": 143,
  "driverId": null,
  "vehicleId": 2,
  "tripDate": "2026-07-23 13:11:00",
  "status": "COMPLETED",
  "priority": "MEDIUM",
  "estimatedDuration": null,
  "estimatedDistance": null,
  "notes": "Tournée automatique - Commande ORD-20260711-9451",
  "tenantId": 1,
  "createdAt": "2026-07-11 13:12:31.87",
  "updatedAt": "2026-07-11 19:13:21.813"
}
```

---

## Table: TripShipmentLink

**Description**: Junction table linking trips to shipments with role definitions, execution status tracking, and sequence ordering.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"TripShipmentLink_id_seq"'::regclass) - Auto-incrementing primary key
- **tripId**: integer NOT NULL - Foreign key to Trip table
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **role**: USER-DEFINED NOT NULL - Role type (enum: PICKUP, DELIVERY, BOTH)
- **status**: text NOT NULL DEFAULT 'NON_DEMARRE' - Execution status
- **podDone**: boolean NOT NULL DEFAULT false - Proof of delivery completed
- **returnsDone**: boolean NOT NULL DEFAULT false - Returns completed
- **sequence**: integer NULL - Sequence order within trip
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **tripId** → Trip.id (Foreign Key)
- **shipmentId** → Shipment.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: tripId references Trip(id), shipmentId references Shipment(id)
- NOT NULL constraints: id, tripId, shipmentId, role, status, podDone, returnsDone, createdAt, updatedAt
- Default values: status = 'NON_DEMARRE', podDone = false, returnsDone = false
- Uses custom enum type: TripShipmentRole

### Example Data
```json
Record 1: {
  "id": 3,
  "tripId": 6,
  "shipmentId": 4,
  "role": "DELIVERY",
  "status": "NON_DEMARRE",
  "podDone": false,
  "returnsDone": false,
  "sequence": 1,
  "createdAt": "2026-07-11 13:03:26.75",
  "updatedAt": "2026-07-11 13:03:26.75"
}
```

---

## Table: TripStop

**Description**: Defines the sequence of stops (pickup/delivery locations) for each trip with arrival/departure tracking and status management.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"TripStop_id_seq"'::regclass) - Auto-incrementing primary key
- **tripId**: integer NOT NULL - Foreign key to Trip table
- **sequence**: integer NOT NULL - Stop sequence order
- **addressId**: integer NOT NULL - Foreign key to Address table
- **type**: USER-DEFINED NOT NULL - Type of stop (enum: PICKUP, DELIVERY)
- **arrival**: timestamp without time zone NULL - Actual arrival time
- **departure**: timestamp without time zone NULL - Actual departure time
- **status**: USER-DEFINED NOT NULL DEFAULT 'PLANNED'::"StopStatus" - Stop status (enum)
- **notes**: text NULL - Stop notes
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **tripId** → Trip.id (Foreign Key)
- **addressId** → Address.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: tripId references Trip(id), addressId references Address(id)
- NOT NULL constraints: id, tripId, sequence, addressId, type, status, createdAt, updatedAt
- Default values: status = 'PLANNED'
- Uses custom enum types: TripStopType, StopStatus

### Example Data
```json
Record 1: {
  "id": 17,
  "tripId": 6,
  "sequence": 1,
  "addressId": 146,
  "type": "DELIVERY",
  "arrival": null,
  "departure": null,
  "status": "PLANNED",
  "notes": "Livraison pour expédition SHP-20260711-9336",
  "createdAt": "2026-07-11 13:03:26.75",
  "updatedAt": "2026-07-11 13:03:26.75"
}
```

---

## Table: TripStopShipment

**Description**: Junction table linking trip stops to shipments with quantity tracking for loaded and delivered items.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"TripStopShipment_id_seq"'::regclass) - Auto-incrementing primary key
- **tripStopId**: integer NOT NULL - Foreign key to TripStop table
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **type**: USER-DEFINED NOT NULL - Operation type (enum: PICKUP, DELIVERY)
- **quantityLoaded**: double precision NULL - Quantity loaded at stop
- **quantityDelivered**: double precision NULL - Quantity delivered at stop
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp

### Relationships
- **tripStopId** → TripStop.id (Foreign Key)
- **shipmentId** → Shipment.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: tripStopId references TripStop(id), shipmentId references Shipment(id)
- NOT NULL constraints: id, tripStopId, shipmentId, type, createdAt
- Uses custom enum type: TripStopShipmentType

### Example Data
```json
Record 1: {
  "id": 5,
  "tripStopId": 34,
  "shipmentId": 30,
  "type": "DELIVERY",
  "quantityLoaded": null,
  "quantityDelivered": null,
  "createdAt": "2026-07-12 10:34:04.202"
}
```

---

## Table: User

**Description**: User authentication and authorization table with role-based access control and driver linkage for system users.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"User_id_seq"'::regclass) - Auto-incrementing primary key
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **email**: text NOT NULL - User email address
- **password**: text NOT NULL - Hashed password
- **role**: USER-DEFINED NOT NULL - User role (enum: ADMIN, DRIVER, DISPATCHER, etc.)
- **firstName**: text NOT NULL - User first name
- **lastName**: text NOT NULL - User last name
- **driverId**: integer NULL - Foreign key to Driver table (for driver users)
- **isActive**: boolean NOT NULL DEFAULT true - Whether user account is active
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **tenantId** → Tenant.id (Foreign Key)
- **driverId** → Driver.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: tenantId references Tenant(id), driverId references Driver(id)
- NOT NULL constraints: id, tenantId, email, password, role, firstName, lastName, isActive, createdAt, updatedAt
- Default values: isActive = true
- Unique constraints: (email, tenantId), driverId
- Uses custom enum type: UserRole

### Example Data
```json
Record 1: {
  "id": 6,
  "tenantId": 1,
  "email": "pierre.bernard@almakom.eu",
  "password": "$2b$10$vSuFLrUA93o74sImiT3pT.gxzZ9YzHEdKQhSHyiZEXNyp5rIOSAa2",
  "role": "DRIVER",
  "firstName": "Pierre",
  "lastName": "Bernard",
  "driverId": 1,
  "isActive": true,
  "createdAt": "2026-07-10 14:52:13.273",
  "updatedAt": "2026-07-10 14:52:13.273"
}
```

---

## Table: Vehicle

**Description**: Vehicle fleet management with comprehensive specifications, capacity details, equipment features, and maintenance tracking.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Vehicle_id_seq"'::regclass) - Auto-incrementing primary key
- **registrationNumber**: text NOT NULL - Vehicle registration number
- **categoryId**: integer NOT NULL - Foreign key to VehicleCategory table
- **brand**: text NULL - Vehicle brand
- **model**: text NULL - Vehicle model
- **year**: integer NOT NULL DEFAULT 2023 - Vehicle year
- **vin**: text NULL - Vehicle Identification Number
- **color**: text NULL - Vehicle color
- **capacityWeight**: double precision NULL - Weight capacity in kg
- **capacityVolume**: double precision NULL - Volume capacity in m³
- **length**: double precision NULL - Vehicle length in meters
- **width**: double precision NULL - Vehicle width in meters
- **height**: double precision NULL - Vehicle height in meters
- **status**: USER-DEFINED NOT NULL DEFAULT 'ACTIVE'::"VehicleStatus" - Vehicle status (enum)
- **mileage**: double precision NULL - Current mileage
- **fuelType**: USER-DEFINED NOT NULL DEFAULT 'DIESEL'::"FuelType" - Fuel type (enum)
- **lastMaintenanceDate**: timestamp without time zone NULL - Last maintenance date
- **nextMaintenanceDate**: timestamp without time zone NULL - Next scheduled maintenance date
- **insuranceExpiry**: timestamp without time zone NULL - Insurance expiration date
- **technicalControlDate**: timestamp without time zone NULL - Technical control inspection date
- **hasLiftGate**: boolean NOT NULL DEFAULT false - Whether vehicle has lift gate
- **hasRefrigeration**: boolean NOT NULL DEFAULT false - Whether vehicle has refrigeration
- **hasGPS**: boolean NOT NULL DEFAULT false - Whether vehicle has GPS
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record last update timestamp

### Relationships
- **categoryId** → VehicleCategory.id (Foreign Key)
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: categoryId references VehicleCategory(id), tenantId references Tenant(id)
- NOT NULL constraints: id, registrationNumber, categoryId, year, status, fuelType, hasLiftGate, hasRefrigeration, hasGPS, tenantId, createdAt, updatedAt
- Default values: year = 2023, status = 'ACTIVE', fuelType = 'DIESEL', hasLiftGate = false, hasRefrigeration = false, hasGPS = false
- Unique constraint: registrationNumber
- Uses custom enum types: VehicleStatus, FuelType

### Example Data
```json
Record 1: {
  "id": 2,
  "registrationNumber": "FR-456-CD-69",
  "categoryId": 1,
  "brand": "Volvo",
  "model": "FH",
  "year": 2022,
  "vin": null,
  "color": null,
  "capacityWeight": 10000.0,
  "capacityVolume": 30.0,
  "length": null,
  "width": null,
  "height": null,
  "status": "ACTIVE",
  "mileage": null,
  "fuelType": "DIESEL",
  "lastMaintenanceDate": null,
  "nextMaintenanceDate": null,
  "insuranceExpiry": null,
  "technicalControlDate": null,
  "hasLiftGate": true,
  "hasRefrigeration": false,
  "hasGPS": true,
  "tenantId": 1,
  "createdAt": "2026-07-10 14:52:12.32",
  "updatedAt": "2026-07-10 14:52:12.32"
}
```

---

## Table: VehicleCategory

**Description**: Vehicle category classification with capacity specifications for fleet organization and vehicle type management.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"VehicleCategory_id_seq"'::regclass) - Auto-incrementing primary key
- **code**: text NOT NULL - Category code (e.g., "CAMION_10T")
- **name**: text NOT NULL - Category name
- **description**: text NULL - Category description
- **maxWeight**: double precision NULL - Maximum weight capacity in kg
- **maxVolume**: double precision NULL - Maximum volume capacity in m³
- **isActive**: boolean NOT NULL DEFAULT true - Whether category is active
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: tenantId references Tenant(id)
- NOT NULL constraints: id, code, name, isActive, tenantId, createdAt, updatedAt
- Default values: isActive = true

### Example Data
```json
Record 1: {
  "id": 1,
  "code": "CAMION_10T",
  "name": "Camion 10 Tonnes",
  "description": null,
  "maxWeight": 10000.0,
  "maxVolume": 30.0,
  "isActive": true,
  "tenantId": 1,
  "createdAt": "2026-07-10 14:52:08.47",
  "updatedAt": "2026-07-10 14:52:08.47"
}
```

---

## Table: VehicleMaintenance

**Description**: Vehicle maintenance scheduling and tracking with cost estimation, technician assignment, and status management.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"VehicleMaintenance_id_seq"'::regclass) - Auto-incrementing primary key
- **vehicleId**: integer NOT NULL - Foreign key to Vehicle table
- **type**: text NOT NULL - Maintenance type
- **description**: text NULL - Maintenance description
- **date**: timestamp without time zone NOT NULL - Maintenance date
- **nextMaintenanceDate**: timestamp without time zone NULL - Next scheduled maintenance date
- **estimatedCost**: double precision NULL - Estimated maintenance cost
- **actualCost**: double precision NULL - Actual maintenance cost
- **technician**: text NULL - Technician name
- **notes**: text NULL - Maintenance notes
- **status**: text NOT NULL DEFAULT 'open' - Maintenance status
- **mileage**: double precision NULL - Vehicle mileage at maintenance
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **vehicleId** → Vehicle.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: vehicleId references Vehicle(id)
- NOT NULL constraints: id, vehicleId, type, date, status, createdAt, updatedAt
- Default values: status = 'open'

### Example Data
```json
Record 1: {
  "id": 1,
  "vehicleId": 6,
  "type": "Suspension",
  "description": null,
  "date": "2026-07-12 00:00:00",
  "nextMaintenanceDate": "2026-07-15 00:00:00",
  "estimatedCost": 9.0,
  "actualCost": null,
  "technician": null,
  "notes": null,
  "status": "open",
  "mileage": null,
  "createdAt": "2026-07-12 17:23:52.28",
  "updatedAt": "2026-07-12 17:23:52.28"
}
```

---

**Documentation Progress**: 27/27 tables completed (100.0%)
**All tables documented successfully!**
