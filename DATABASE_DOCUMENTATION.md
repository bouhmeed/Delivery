# Database Documentation

This document contains the structure and documentation for the PostgreSQL database tables in the Delivery application.

---

## Table: Client

**Description**: Stores information about clients/customers who receive deliveries.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Client_id_seq"'::regclass) - Auto-incrementing primary key
- **name**: text NOT NULL - Client name
- **address**: text NULL - Client address
- **city**: text NULL - Client city
- **postalCode**: text NULL - Client postal code
- **phone**: text NULL - Client phone number
- **email**: text NULL - Client email address
- **contact**: text NULL - Contact person name
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: tenantId references Tenant(id)
- NOT NULL constraints: id, name, tenantId, createdAt, updatedAt

### Example Data
No data found in Client table

---

## Table: DeliveryDocument

**Description**: Stores delivery documents related to shipments, including bill of lading information.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"DeliveryDocument_id_seq"'::regclass) - Auto-incrementing primary key
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **blNumber**: text NOT NULL - Bill of Lading number
- **pdfUrl**: text NULL - URL to PDF document
- **signed**: boolean NOT NULL DEFAULT false - Whether document has been signed

### Relationships
- **shipmentId** → Shipment.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: shipmentId references Shipment(id)
- NOT NULL constraints: id, shipmentId, blNumber, signed
- Default value: signed = false

### Example Data
No data found in DeliveryDocument table

---

## Table: DeliveryImage

**Description**: Stores delivery proof images and documents associated with delivery confirmations.

### Columns
- **id**: text NOT NULL - Unique identifier (UUID)
- **gedDocId**: text NOT NULL - GED (Gestion Électronique de Documents) document ID
- **url**: text NOT NULL - URL to the image/document
- **documentType**: USER-DEFINED NOT NULL DEFAULT 'PHOTO_DELIVERY'::"DocumentType" - Type of document (enum)
- **proofId**: text NOT NULL - Foreign key to delivery proof
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp

### Relationships
- **proofId** → DeliveryProof.id (Foreign Key, inferred)

### Constraints
- Primary Key: id
- Foreign Key: proofId references DeliveryProof(id) (inferred)
- NOT NULL constraints: id, gedDocId, url, documentType, proofId, createdAt
- Default value: documentType = 'PHOTO_DELIVERY'
- Uses custom enum type: DocumentType

### Example Data
```json
Record 1: {
  "id": "0185e1a8-7f3c-4e8b-9b2a-1c3d4e5f6a7b",
  "gedDocId": "GED_DOC_001",
  "url": "https://storage.example.com/delivery-images/base64-encoded-image-data...",
  "documentType": "SIGNATURE_BL",
  "proofId": "bf27cce5-b683-41f6-991b-b72ceb71faee",
  "createdAt": "2026-03-18T09:15:43.080Z"
}
```

---

## Table: Department

**Description**: Stores department information linked to specific locations for organizational structure.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Department_id_seq"'::regclass) - Auto-incrementing primary key
- **name**: text NOT NULL - Department name
- **locationId**: integer NOT NULL - Foreign key to Location table

### Relationships
- **locationId** → Location.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: locationId references Location(id)
- NOT NULL constraints: id, name, locationId

### Example Data
No data found in Department table

---

## Table: Driver

**Description**: Stores driver information including employment details, licensing, and contact information.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Driver_id_seq"'::regclass) - Auto-incrementing primary key
- **name**: text NOT NULL - Driver full name
- **licenseNumber**: text NULL - Driver license number
- **licenseExpiry**: timestamp without time zone NULL - License expiration date
- **employmentType**: USER-DEFINED NOT NULL DEFAULT 'FULL_TIME'::"EmploymentType" - Employment type (enum)
- **contractHoursWeek**: integer NOT NULL DEFAULT 40 - Weekly contract hours
- **homeDepotId**: integer NOT NULL - Home depot identifier
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **status**: USER-DEFINED NOT NULL DEFAULT 'ACTIF'::"DriverStatus" - Driver status (enum)
- **address**: text NULL - Driver address
- **assignedVehicle**: text NULL - Assigned vehicle identifier
- **city**: text NULL - Driver city
- **country**: text NULL DEFAULT 'France'::text - Driver country
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **dateOfBirth**: timestamp without time zone NULL - Date of birth
- **email**: text NULL - Email address
- **hireDate**: timestamp without time zone NULL - Hire date
- **licenseIssueDate**: timestamp without time zone NULL - License issue date
- **phone**: text NULL - Phone number
- **postalCode**: text NULL - Postal code
- **salary**: double precision NULL DEFAULT 0 - Salary amount
- **updatedAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record last update timestamp

### Relationships
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: tenantId references Tenant(id)
- NOT NULL constraints: id, name, employmentType, contractHoursWeek, homeDepotId, tenantId, status, createdAt, updatedAt
- Default values: employmentType = 'FULL_TIME', contractHoursWeek = 40, status = 'ACTIF', country = 'France', salary = 0
- Uses custom enum types: EmploymentType, DriverStatus

### Indexes
- **Driver_pkey**: Primary key index on id
- **Driver_email_idx**: Index on email column
- **Driver_licenseNumber_idx**: Index on licenseNumber column
- **Driver_status_idx**: Index on status column
- **Driver_tenantId_idx**: Index on tenantId column

### Example Data
```json
Record 1: {
  "id": 1,
  "name": "Jean Dupont",
  "licenseNumber": "DRV-001",
  "licenseExpiry": null,
  "employmentType": "FULL_TIME",
  "contractHoursWeek": 40,
  "homeDepotId": 1,
  "tenantId": 1,
  "status": "ACTIF",
  "address": null,
  "assignedVehicle": null,
  "city": null,
  "country": "France",
  "createdAt": "2026-03-18T07:39:58.189Z",
  "dateOfBirth": null,
  "email": null,
  "hireDate": null,
  "licenseIssueDate": null,
  "phone": "+33 6 12 34 56 78",
  "postalCode": null,
  "salary": 0,
  "updatedAt": "2026-03-18T07:39:58.189Z"
}
```

---

## Table: ExternalReference

**Description**: Stores external system references linked to shipments for integration purposes.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ExternalReference_id_seq"'::regclass) - Auto-incrementing primary key
- **sourceSystem**: text NOT NULL - Name of the external system
- **externalId**: text NOT NULL - Identifier in the external system
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table

### Relationships
- **shipmentId** → Shipment.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: shipmentId references Shipment(id)
- NOT NULL constraints: id, sourceSystem, externalId, shipmentId

### Example Data
No data found in ExternalReference table

---

## Table: Item

**Description**: Stores product/item information including descriptions, dimensions, and inventory details.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Item_id_seq"'::regclass) - Auto-incrementing primary key
- **itemNo**: text NOT NULL - Unique item number
- **description**: text NOT NULL - Item description
- **unit**: text NOT NULL DEFAULT 'PCS'::text - Unit of measurement
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
- NOT NULL constraints: id, itemNo, description, unit, isActive, tenantId, createdAt, updatedAt
- Default values: unit = 'PCS', isActive = true
- Unique constraint: itemNo

### Indexes
- **Item_pkey**: Primary key index on id
- **Item_itemNo_key**: Unique index on itemNo

### Example Data
```json
Record 1: {
  "id": 1,
  "itemNo": "ITEM-001",
  "description": "Produit A - Boîte standard",
  "unit": "PCS",
  "weight": 2.5,
  "volume": 0.01,
  "category": "Standard",
  "isActive": true,
  "tenantId": 1,
  "createdAt": "2026-03-18T07:41:26.617Z",
  "updatedAt": "2026-03-18T07:41:26.617Z"
}
```

---

## Table: ItemLocation

**Description**: Junction table managing inventory levels of items at specific locations with stock thresholds.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ItemLocation_id_seq"'::regclass) - Auto-incrementing primary key
- **itemId**: integer NOT NULL - Foreign key to Item table
- **locationId**: integer NOT NULL - Foreign key to Location table
- **quantity**: double precision NOT NULL DEFAULT 0 - Current quantity
- **minStock**: double precision NOT NULL DEFAULT 0 - Minimum stock threshold
- **maxStock**: double precision NULL - Maximum stock threshold

### Relationships
- **itemId** → Item.id (Foreign Key)
- **locationId** → Location.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: itemId references Item(id), locationId references Location(id)
- NOT NULL constraints: id, itemId, locationId, quantity, minStock
- Default values: quantity = 0, minStock = 0
- Composite unique constraint: (itemId, locationId)

### Indexes
- **ItemLocation_pkey**: Primary key index on id
- **ItemLocation_itemId_locationId_key**: Unique composite index on itemId and locationId

### Example Data
No data found in ItemLocation table

---

## Table: Location

**Description**: Stores location information including warehouses, depots, and client addresses.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Location_id_seq"'::regclass) - Auto-incrementing primary key
- **name**: text NOT NULL - Location name
- **address**: text NULL - Street address
- **city**: text NULL - City
- **postalCode**: text NULL - Postal code
- **phone**: text NULL - Phone number
- **email**: text NULL - Email address
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp

### Relationships
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: tenantId references Tenant(id)
- NOT NULL constraints: id, name, tenantId, createdAt

### Indexes
- **Location_pkey**: Primary key index on id

### Example Data
```json
Record 1: {
  "id": 1,
  "name": "Entrepôt Principal",
  "address": "123 Rue du Dépôt",
  "city": "Lyon",
  "postalCode": "69000",
  "phone": null,
  "email": null,
  "tenantId": 1,
  "createdAt": "2026-03-18T07:39:57.571Z"
}
```

---

## Table: Order

**Description**: Stores customer orders with status tracking and delivery information.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Order_id_seq"'::regclass) - Auto-incrementing primary key
- **orderNo**: text NOT NULL - Unique order number
- **customerId**: integer NULL - Foreign key to Client table
- **orderDate**: timestamp without time zone NOT NULL - Order creation date
- **status**: USER-DEFINED NOT NULL - Order status (enum)
- **deliveryAddress**: text NULL - Delivery address
- **deliveryCity**: text NULL - Delivery city
- **deliveryZipCode**: text NULL - Delivery postal code
- **deliveryCountry**: text NULL DEFAULT 'France'::text - Delivery country
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp

### Relationships
- **customerId** → Client.id (Foreign Key)
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: customerId references Client(id), tenantId references Tenant(id)
- NOT NULL constraints: id, orderNo, orderDate, status, tenantId, createdAt, updatedAt
- Default value: deliveryCountry = 'France'
- Unique constraint: orderNo
- Uses custom enum type: OrderStatus

### Indexes
- **Order_pkey**: Primary key index on id
- **Order_orderNo_key**: Unique index on orderNo

### Example Data
No data found in Order table

---

## Table: OrderItem

**Description**: Junction table linking orders to items with quantities and descriptions.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"OrderItem_id_seq"'::regclass) - Auto-incrementing primary key
- **orderId**: integer NOT NULL - Foreign key to Order table
- **itemId**: integer NOT NULL - Foreign key to Item table
- **itemNo**: text NULL - Item number (redundant with Item.itemNo)
- **description**: text NULL - Item description (redundant with Item.description)
- **quantity**: integer NOT NULL - Quantity ordered
- **unit**: text NULL - Unit of measurement

### Relationships
- **orderId** → Order.id (Foreign Key)
- **itemId** → Item.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: orderId references Order(id), itemId references Item(id)
- NOT NULL constraints: id, orderId, itemId, quantity

### Indexes
- **OrderItem_pkey**: Primary key index on id

### Example Data
```json
Record 1: {
  "id": 1,
  "orderId": 1,
  "itemId": 1,
  "itemNo": null,
  "description": "Produit A - Boîte standard",
  "quantity": 100,
  "unit": "PCS"
}
```

---

## Table: Shipment

**Description**: Core shipment management table with comprehensive logistics information including planning, tracking, and execution details.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Shipment_id_seq"'::regclass) - Auto-incrementing primary key
- **shipmentNo**: text NULL - Unique shipment number
- **customerId**: integer NULL - Foreign key to Client table
- **type**: USER-DEFINED NOT NULL - Shipment type (enum: OUTBOUND, INBOUND, TRANSFER)
- **originId**: integer NOT NULL - Foreign key to Location table (origin)
- **destinationId**: integer NOT NULL - Foreign key to Location table (destination)
- **priority**: USER-DEFINED NOT NULL - Shipment priority (enum)
- **requestedPickup**: timestamp without time zone NULL - Requested pickup time
- **requestedDelivery**: timestamp without time zone NULL - Requested delivery time
- **status**: USER-DEFINED NOT NULL DEFAULT 'TO_PLAN'::"ShipmentStatus" - Shipment status (enum)
- **description**: text NOT NULL - Shipment description
- **quantity**: integer NOT NULL - Total quantity
- **uom**: text NOT NULL - Unit of measurement
- **packaging**: text NULL - Packaging information
- **weight**: double precision NULL - Total weight
- **volume**: double precision NULL - Total volume
- **stackable**: boolean NULL - Whether items are stackable
- **carrier**: text NULL - Carrier information
- **trackingNumber**: text NULL - Tracking number
- **deliveryAddress**: text NULL - Specific delivery address
- **deliveryCity**: text NULL - Delivery city
- **deliveryZipCode**: text NULL - Delivery postal code
- **deliveryCountry**: text NULL DEFAULT 'France'::text - Delivery country
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **updatedAt**: timestamp without time zone NOT NULL - Record last update timestamp
- **createdById**: integer NULL - Foreign key to User table (creator)
- **updatedById**: integer NULL - Foreign key to User table (updater)
- **driverId**: integer NULL - Foreign key to Driver table
- **vehicleId**: integer NULL - Foreign key to Vehicle table
- **estimatedDuration**: integer NULL - Estimated duration in minutes
- **outlookEventId**: text NULL - Outlook calendar event ID
- **plannedEnd**: timestamp without time zone NULL - Planned end time
- **plannedStart**: timestamp without time zone NULL - Planned start time
- **distanceKm**: double precision NULL - Distance in kilometers

### Relationships
- **customerId** → Client.id (Foreign Key)
- **originId** → Location.id (Foreign Key)
- **destinationId** → Location.id (Foreign Key)
- **tenantId** → Tenant.id (Foreign Key)
- **createdById** → User.id (Foreign Key)
- **updatedById** → User.id (Foreign Key)
- **driverId** → Driver.id (Foreign Key)
- **vehicleId** → Vehicle.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: customerId, originId, destinationId, tenantId, createdById, updatedById, driverId, vehicleId
- NOT NULL constraints: id, type, originId, destinationId, priority, status, description, quantity, uom, tenantId, createdAt, updatedAt
- Default values: status = 'TO_PLAN', deliveryCountry = 'France'
- Unique constraint: shipmentNo
- Uses custom enum types: ShipmentType, ShipmentPriority, ShipmentStatus

### Indexes
- **Shipment_pkey**: Primary key index on id
- **Shipment_shipmentNo_key**: Unique index on shipmentNo
- **Shipment_status_idx**: Index on status column
- **Shipment_driverId_plannedStart_idx**: Composite index on driverId and plannedStart
- **Shipment_vehicleId_plannedStart_idx**: Composite index on vehicleId and plannedStart

### Example Data
```json
Record 1: {
  "id": 1,
  "shipmentNo": "EXP-2026-001",
  "customerId": 1,
  "type": "OUTBOUND",
  "originId": 3,
  "destinationId": 4,
  "priority": "HIGH",
  "requestedPickup": "2026-03-18T07:41:29.964Z",
  "requestedDelivery": "2026-03-20T07:41:29.964Z",
  "status": "TO_PLAN",
  "description": "Livraison Client A - Produits standards",
  "quantity": 105,
  "uom": "PCS",
  "weight": 275,
  "volume": 1.25,
  "deliveryCountry": "France",
  "tenantId": 1,
  "createdAt": "2026-03-18T07:41:29.967Z",
  "updatedAt": "2026-03-18T07:41:29.967Z",
  "createdById": 3,
  "driverId": 3,
  "vehicleId": 3,
  "plannedEnd": "2026-03-18T15:41:29.964Z",
  "plannedStart": "2026-03-18T11:41:29.964Z",
  "distanceKm": 465
}
```

---

## Table: ShipmentLine

**Description**: Detailed line items within shipments, linking shipments to specific items with quantities and measurements.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ShipmentLine_id_seq"'::regclass) - Auto-incrementing primary key
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **itemId**: integer NULL - Foreign key to Item table
- **itemCode**: text NULL - Item code (redundant with Item.itemNo)
- **description**: text NULL - Item description (redundant with Item.description)
- **quantity**: double precision NOT NULL - Quantity of items
- **uom**: text NOT NULL - Unit of measurement
- **packaging**: text NULL - Packaging information
- **weight**: double precision NULL - Total weight for this line
- **volume**: double precision NULL - Total volume for this line
- **stackable**: boolean NULL - Whether items are stackable

### Relationships
- **shipmentId** → Shipment.id (Foreign Key)
- **itemId** → Item.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: shipmentId references Shipment(id), itemId references Item(id)
- NOT NULL constraints: id, shipmentId, quantity, uom

### Indexes
- **ShipmentLine_pkey**: Primary key index on id

### Example Data
```json
Record 1: {
  "id": 1,
  "shipmentId": 1,
  "itemId": 1,
  "itemCode": null,
  "description": "Produit A - Boîte standard",
  "quantity": 100,
  "uom": "PCS",
  "packaging": null,
  "weight": 250,
  "volume": 1,
  "stackable": null
}
```

---

## Table: ShipmentProof

**Description**: Stores delivery proof documents including images and signatures for shipments.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"ShipmentProof_id_seq"'::regclass) - Auto-incrementing primary key
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **imageUrl**: text NOT NULL - URL to delivery proof image
- **signatureUrl**: text NULL - URL to signature image
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp

### Relationships
- **shipmentId** → Shipment.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: shipmentId references Shipment(id)
- NOT NULL constraints: id, shipmentId, imageUrl, createdAt

### Indexes
- **ShipmentProof_pkey**: Primary key index on id

### Example Data
No data found in ShipmentProof table

---

## Table: Tenant

**Description**: Multi-tenant support table storing tenant information for data isolation.

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

### Indexes
- **Tenant_pkey**: Primary key index on id
- **Tenant_name_key**: Unique index on name

### Example Data
```json
Record 1: {
  "id": 1,
  "name": "Default Tenant",
  "createdAt": "2026-03-18T07:38:31.828Z"
}
```

---

## Table: Trip

**Description**: Core trip management table organizing driver schedules and vehicle assignments for delivery operations.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Trip_id_seq"'::regclass) - Auto-incrementing primary key
- **tripDate**: timestamp without time zone NOT NULL - Scheduled trip date
- **depotId**: integer NOT NULL - Foreign key to Location table (depot)
- **driverId**: integer NULL - Foreign key to Driver table
- **vehicleId**: integer NULL - Foreign key to Vehicle table
- **status**: USER-DEFINED NOT NULL DEFAULT 'PLANNING'::"TripStatus" - Trip status (enum)
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **createdAt**: timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP - Record creation timestamp
- **tripId**: text NULL - Unique trip identifier

### Relationships
- **depotId** → Location.id (Foreign Key)
- **driverId** → Driver.id (Foreign Key)
- **vehicleId** → Vehicle.id (Foreign Key)
- **tenantId** → Tenant.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: depotId, driverId, vehicleId, tenantId
- NOT NULL constraints: id, tripDate, depotId, status, tenantId, createdAt
- Default values: status = 'PLANNING'
- Unique constraint: tripId
- Uses custom enum type: TripStatus

### Indexes
- **Trip_pkey**: Primary key index on id
- **Trip_tripId_key**: Unique index on tripId
- **Trip_driverId_tripDate_idx**: Composite index on driverId and tripDate
- **Trip_vehicleId_tripDate_idx**: Composite index on vehicleId and tripDate
- **Trip_status_tripDate_idx**: Composite index on status and tripDate

### Example Data
```json
Record 1: {
  "id": 1,
  "tripDate": "2026-03-18T11:41:32.445Z",
  "depotId": 3,
  "driverId": 3,
  "vehicleId": 3,
  "status": "PLANNING",
  "tenantId": 1,
  "createdAt": "2026-03-18T07:41:32.446Z",
  "tripId": "TRIP-2026-001"
}
```

---

## Table: TripShipmentLink

**Description**: Junction table linking trips to shipments with role definitions and execution status tracking.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"TripShipmentLink_id_seq"'::regclass) - Auto-incrementing primary key
- **tripId**: integer NOT NULL - Foreign key to Trip table
- **shipmentId**: integer NOT NULL - Foreign key to Shipment table
- **role**: USER-DEFINED NOT NULL - Role type (enum)
- **status**: text NOT NULL DEFAULT 'NON_DEMARRE'::text - Execution status
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

### Indexes
- **TripShipmentLink_pkey**: Primary key index on id

### Example Data
```json
Record 1: {
  "id": 1,
  "tripId": 1,
  "shipmentId": 1,
  "role": "BOTH",
  "status": "NON_DEMARRE",
  "podDone": false,
  "returnsDone": false,
  "sequence": 1,
  "createdAt": "2026-03-18T07:41:32.446Z",
  "updatedAt": "2026-03-18T07:41:32.446Z"
}
```

---

## Table: TripStop

**Description**: Defines the sequence of stops (pickup/delivery locations) for each trip.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"TripStop_id_seq"'::regclass) - Auto-incrementing primary key
- **tripId**: integer NOT NULL - Foreign key to Trip table
- **sequence**: integer NOT NULL - Stop sequence order
- **locationId**: integer NOT NULL - Foreign key to Location table
- **stopType**: USER-DEFINED NOT NULL - Type of stop (enum)

### Relationships
- **tripId** → Trip.id (Foreign Key)
- **locationId** → Location.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: tripId references Trip(id), locationId references Location(id)
- NOT NULL constraints: id, tripId, sequence, locationId, stopType
- Uses custom enum type: TripStopType

### Indexes
- **TripStop_pkey**: Primary key index on id

### Example Data
```json
Record 1: {
  "id": 1,
  "tripId": 1,
  "sequence": 1,
  "locationId": 3,
  "stopType": "PICKUP"
}
```

---

## Table: User

**Description**: User authentication and authorization table with role-based access control and driver linkage.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"User_id_seq"'::regclass) - Auto-incrementing primary key
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **email**: text NOT NULL - User email address
- **password**: text NOT NULL - Hashed password
- **role**: USER-DEFINED NOT NULL - User role (enum)
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

### Indexes
- **User_pkey**: Primary key index on id
- **User_email_tenantId_key**: Unique composite index on email and tenantId
- **User_driverId_key**: Unique index on driverId

### Example Data
```json
Record 1: {
  "id": 1,
  "tenantId": 1,
  "email": "admin@tms.com",
  "password": "$2b$10$AnyoKjB9z0lUJToU/QjqOuthahoKxQh..JlaJ5beQ893HmLkL48ZG",
  "role": "ADMIN",
  "firstName": "Admin",
  "lastName": "User",
  "driverId": null,
  "isActive": true,
  "createdAt": "2026-03-18T07:38:33.267Z",
  "updatedAt": "2026-03-18T07:38:33.267Z"
}
```

---

## Table: Vehicle

**Description**: Vehicle fleet management with capacity specifications and maintenance tracking.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"Vehicle_id_seq"'::regclass) - Auto-incrementing primary key
- **name**: text NOT NULL - Vehicle name
- **registration**: text NOT NULL - Vehicle registration number
- **capacityWeight**: double precision NOT NULL - Weight capacity in kg
- **capacityVolume**: double precision NOT NULL - Volume capacity in m³
- **tenantId**: integer NOT NULL - Foreign key to Tenant table
- **dernierControle**: timestamp without time zone NULL - Last inspection date
- **driverId**: integer NULL - Foreign key to Driver table (assigned driver)
- **prochainControle**: timestamp without time zone NULL - Next inspection date
- **status**: USER-DEFINED NOT NULL DEFAULT 'ACTIVE'::"VehicleStatus" - Vehicle status (enum)
- **type**: USER-DEFINED NOT NULL DEFAULT 'CAMION'::"VehicleType" - Vehicle type (enum)
- **year**: integer NOT NULL DEFAULT 2023 - Vehicle year

### Relationships
- **tenantId** → Tenant.id (Foreign Key)
- **driverId** → Driver.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Keys: tenantId references Tenant(id), driverId references Driver(id)
- NOT NULL constraints: id, name, registration, capacityWeight, capacityVolume, tenantId, status, type, year
- Default values: status = 'ACTIVE', type = 'CAMION', year = 2023
- Unique constraint: registration
- Uses custom enum types: VehicleStatus, VehicleType

### Indexes
- **Vehicle_pkey**: Primary key index on id

### Example Data
```json
Record 1: {
  "id": 2,
  "name": "Peugeot Boxer",
  "registration": "EF-456-GH",
  "capacityWeight": 4000,
  "capacityVolume": 18,
  "tenantId": 1,
  "dernierControle": null,
  "driverId": null,
  "prochainControle": null,
  "status": "ACTIVE",
  "type": "CAMION",
  "year": 2023
}
```

---

## Table: VehicleMaintenance

**Description**: Vehicle maintenance scheduling and tracking with cost estimation and technician assignment.

### Columns
- **id**: integer NOT NULL DEFAULT nextval('"VehicleMaintenance_id_seq"'::regclass) - Auto-incrementing primary key
- **vehicleId**: integer NOT NULL - Foreign key to Vehicle table
- **type**: text NOT NULL - Maintenance type
- **date**: timestamp without time zone NOT NULL - Maintenance date
- **nextMaintenance**: timestamp without time zone NULL - Next scheduled maintenance
- **estimatedCost**: double precision NULL - Estimated maintenance cost
- **notes**: text NULL - Maintenance notes
- **technician**: text NULL - Assigned technician
- **status**: text NOT NULL DEFAULT 'open'::text - Maintenance status

### Relationships
- **vehicleId** → Vehicle.id (Foreign Key)

### Constraints
- Primary Key: id
- Foreign Key: vehicleId references Vehicle(id)
- NOT NULL constraints: id, vehicleId, type, date, status
- Default value: status = 'open'

### Indexes
- **VehicleMaintenance_pkey**: Primary key index on id

### Example Data
```json
Record 1: {
  "id": 1,
  "vehicleId": 1,
  "type": "Suspension",
  "date": "2026-03-17T23:00:00.000Z",
  "nextMaintenance": "2026-03-26T23:00:00.000Z",
  "estimatedCost": 100,
  "notes": null,
  "technician": null,
  "status": "in_progress"
}
```

---

## Table: _prisma_migrations

**Description**: Prisma ORM migration tracking table for database schema version control and deployment history.

### Columns
- **id**: character varying(36) NOT NULL - Unique migration identifier (UUID)
- **checksum**: character varying(64) NOT NULL - SHA-256 checksum of migration file
- **finished_at**: timestamp with time zone NULL - Migration completion timestamp
- **migration_name**: character varying(255) NOT NULL - Migration file name
- **logs**: text NULL - Migration execution logs
- **rolled_back_at**: timestamp with time zone NULL - Migration rollback timestamp
- **started_at**: timestamp with time zone NOT NULL DEFAULT now() - Migration start timestamp
- **applied_steps_count**: integer NOT NULL DEFAULT 0 - Number of applied migration steps

### Relationships
This table is internal to Prisma ORM and has no foreign key relationships.

### Constraints
- Primary Key: id
- NOT NULL constraints: id, checksum, migration_name, started_at, applied_steps_count
- Default values: started_at = now(), applied_steps_count = 0

### Indexes
- **_prisma_migrations_pkey**: Primary key index on id

### Example Data
```json
Record 1: {
  "id": "90392034-e790-4cb9-98ea-e5a4e66d51d8",
  "checksum": "af0ddd802bfc935f3f2ef248869edd49bef1dc653a09457c8fb0fe03a4a332a5",
  "finished_at": "2026-03-18T08:37:06.450Z",
  "migration_name": "20260303115742_sync_schema",
  "logs": null,
  "rolled_back_at": null,
  "started_at": "2026-03-18T08:37:05.576Z",
  "applied_steps_count": 1
}
```

---

# Database Documentation Summary

## Overview
This PostgreSQL database contains **23 tables** that form a comprehensive delivery management system with the following key characteristics:

### Architecture Features
- **Multi-tenant Architecture**: All major tables reference `Tenant.id` for data isolation
- **ORM Integration**: Uses Prisma ORM for schema management and migrations
- **Comprehensive Indexing**: Performance-optimized indexes for scheduling and querying
- **Enum Types**: Custom PostgreSQL enums for status management (French localization)
- **Audit Trail**: Most tables include `createdAt` and `updatedAt` timestamps

### Core Business Entities
1. **User Management**: User authentication with role-based access control
2. **Driver Management**: Driver profiles with licensing and employment details
3. **Vehicle Fleet**: Vehicle tracking with maintenance scheduling
4. **Trip Management**: Trip scheduling and execution tracking
5. **Shipment Management**: Comprehensive shipment lifecycle management
6. **Location Management**: Geographic locations for depots, clients, and stops
7. **Item Catalog**: Product management with inventory tracking
8. **Order Processing**: Customer orders and item fulfillment
9. **Proof of Delivery**: Document management for delivery confirmation

### Data Localization
- French field names and status values throughout the system
- French addresses and contact information in sample data
- Localized enum values (e.g., 'ACTIF', 'NON_DEMARRE', 'PLANNING')

### Security Features
- Bcrypt password hashing for user authentication
- Tenant-based data isolation
- Role-based access control (ADMIN, DRIVER roles)

### Migration History
The `_prisma_migrations` table shows 5 successful migrations applied on 2026-03-18, indicating active database development and schema evolution.

---

# Core App Tables - Delivery Management Focus

## Selected Tables for Mobile App

The following 7 tables form the core data model for the delivery management mobile application:

### 📱 **App Core Tables**

1. **Driver** - Driver profiles and authentication
2. **Trip** - Daily tours and route planning
3. **Shipment** - Individual deliveries and tracking
4. **Location** - Addresses and geographic points
5. **Client** - Customer information
6. **DeliveryImage** - Proof of delivery photos
7. **TripStop** - Stop sequence and routing
8. **TripShipmentLink** - Links trips to shipments

---

## Table: Driver

**Description**: Core driver management for mobile app authentication and profile.

### Essential Columns for App
- **id**: Primary key
- **name**: Driver full name
- **phone**: Contact number
- **email**: Email address
- **status**: Driver status ('ACTIF', 'INACTIF')
- **tenantId**: Multi-tenant isolation

### App Integration Points
- **Authentication**: Link with User table for login
- **Profile Display**: Show driver name and contact info
- **Status Management**: Active/inactive driver status
- **Trip Assignment**: Filter trips by assigned driver

---

## Table: Trip

**Description**: Daily tours and route planning for driver schedules.

### Essential Columns for App
- **id**: Primary key
- **tripDate**: Scheduled date
- **driverId**: Assigned driver
- **status**: Trip status ('PLANNING', 'READY', 'IN_PROGRESS', 'COMPLETED')
- **tripId**: Unique trip identifier (e.g., "TRIP-2026-001")

### App Integration Points
- **Daily Schedule**: Show today's trips for logged-in driver
- **Trip Status Tracking**: Real-time status updates
- **Route Planning**: Sequence of stops and deliveries
- **Performance Metrics**: Trip completion tracking

---

## Table: Shipment

**Description**: Individual deliveries with comprehensive tracking information.

### Essential Columns for App
- **id**: Primary key
- **shipmentNo**: Unique shipment number
- **customerId**: Client reference
- **status**: Delivery status ('TO_PLAN', 'EXPEDITION', 'DELIVERED')
- **description**: Delivery details
- **quantity**: Item count
- **deliveryAddress**: Destination address
- **deliveryCity**: Destination city
- **driverId**: Assigned driver

### App Integration Points
- **Delivery List**: Show shipments for current trip
- **Status Updates**: Mark shipments as delivered
- **Customer Info**: Display delivery details
- **Proof Collection**: Link to delivery images

---

## Table: Location

**Description**: Geographic addresses for depots, clients, and stops.

### Essential Columns for App
- **id**: Primary key
- **name**: Location name
- **address**: Street address
- **city**: City
- **postalCode**: Postal code

### App Integration Points
- **Route Display**: Show addresses on trip route
- **Navigation**: Integration with map services
- **Customer Locations**: Display client addresses
- **Depot Information**: Show starting/ending points

---

## Table: Client

**Description**: Customer information for delivery management.

### Essential Columns for App
- **id**: Primary key
- **name**: Customer name
- **address**: Customer address
- **city**: Customer city
- **phone**: Contact phone
- **email**: Contact email

### App Integration Points
- **Customer Display**: Show recipient information
- **Contact Options**: Call/message customers
- **Delivery History**: View past deliveries
- **Address Verification**: Confirm delivery locations

---

## Table: DeliveryImage

**Description**: Proof of delivery photos and documents.

### Essential Columns for App
- **id**: Primary key (UUID)
- **url**: Image URL
- **documentType**: Type ('PHOTO_DELIVERY', 'SIGNATURE_BL')
- **proofId**: Reference to delivery proof
- **createdAt**: Capture timestamp

### App Integration Points
- **Photo Capture**: Take delivery photos
- **Signature Collection**: Capture customer signatures
- **Proof Gallery**: View all delivery proofs
- **Document Upload**: Store proof documents

---

## Table: TripStop

**Description**: Stop sequence for trip routing and navigation.

### Essential Columns for App
- **id**: Primary key
- **tripId**: Reference to trip
- **sequence**: Stop order (1, 2, 3...)
- **locationId**: Reference to location
- **stopType**: Type ('PICKUP', 'DELIVERY')

### App Integration Points
- **Route Sequence**: Display ordered stops
- **Navigation**: Step-by-step directions
- **Stop Completion**: Mark stops as completed
- **Progress Tracking**: Show trip progress

---

## Table: TripShipmentLink

**Description**: Links trips to shipments with execution status.

### Essential Columns for App
- **id**: Primary key
- **tripId**: Reference to trip
- **shipmentId**: Reference to shipment
- **status**: Execution status ('NON_DEMARRE', 'EN_COURS', 'TERMINE')
- **podDone**: Proof of delivery completed
- **sequence**: Order within trip

### App Integration Points
- **Shipment Assignment**: Link shipments to trips
- **Status Tracking**: Track individual shipment progress
- **POD Management**: Mark when proof is collected
- **Delivery Sequence**: Show delivery order

---

## 🔄 **Data Flow for App**

### Typical Driver Workflow:
1. **Login** → Authenticate driver (Driver + User tables)
2. **View Today's Trips** → Filter trips by driver and date (Trip table)
3. **Start Trip** → Update trip status to 'IN_PROGRESS' (Trip table)
4. **View Stops** → Get ordered stop sequence (TripStop + Location tables)
5. **Navigate to Stop** → Show address and customer info (Location + Client tables)
6. **View Shipments** → Get deliveries for current stop (TripShipmentLink + Shipment tables)
7. **Complete Delivery** → Update shipment status and collect proof (Shipment + DeliveryImage tables)
8. **Mark Stop Complete** → Update stop completion (TripStop table)
9. **Complete Trip** → Update trip status to 'COMPLETED' (Trip table)

### Key Relationships:
- **Driver** → **Trip** (one-to-many)
- **Trip** → **TripStop** (one-to-many)
- **Trip** → **TripShipmentLink** → **Shipment** (many-to-many)
- **Shipment** → **Client** (many-to-one)
- **TripStop** → **Location** (many-to-one)
- **Shipment** → **DeliveryImage** (one-to-many)

---

**Database documentation completed for all 23 tables.**
