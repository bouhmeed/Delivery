import urllib.request
import urllib.error
import json
import sys

NEON_SQL_ENDPOINT = "https://ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/sql"
NEON_CONNECTION_STRING = "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require"

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

def execute_query(query):
    req = urllib.request.Request(
        NEON_SQL_ENDPOINT,
        data=json.dumps({"query": query}).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
            "neon-connection-string": NEON_CONNECTION_STRING
        },
        method="POST"
    )
    try:
        with urllib.request.urlopen(req) as response:
            res_body = response.read().decode("utf-8")
            res_json = json.loads(res_body)
            return res_json
    except urllib.error.HTTPError as e:
        print(f"❌ Error running query: {query}")
        print(f"HTTP Code: {e.code}")
        try:
            error_body = e.read().decode("utf-8")
            print(f"Error Body: {error_body}")
        except Exception as read_err:
            print(f"Could not read error body: {read_err}")
        return None

def main():
    print("--- ⚙️ DELETING EXISTING TRIP IF EXISTS ---")
    delete_existing_trip_query = """
        DELETE FROM "Trip" 
        WHERE "tripId" = 'TRIP-JULY9-2026'
    """
    execute_query(delete_existing_trip_query)
    print("✅ Existing trip deleted (if existed)")
    
    print("\n--- ⚙️ DELETING EXISTING SHIPMENTS IF EXISTS ---")
    delete_existing_shipments_query = """
        DELETE FROM "Shipment" 
        WHERE "shipmentNo" LIKE 'SHIP-JULY9-%'
    """
    execute_query(delete_existing_shipments_query)
    print("✅ Existing shipments deleted (if existed)")
    
    print("\n--- ⚙️ SYNCHRONIZING TRIP SEQUENCE ---")
    sync_query = 'SELECT setval(\'"Trip_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "Trip"), 1), false)'
    execute_query(sync_query)
    print("✅ Trip sequence synchronized!")
    
    print("\n--- ⚙️ SYNCHRONIZING SHIPMENT SEQUENCE ---")
    sync_query = 'SELECT setval(\'"Shipment_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "Shipment"), 1), false)'
    execute_query(sync_query)
    print("✅ Shipment sequence synchronized!")

    print("\n--- ⚙️ INSERTING TRIP FOR 09/07/2026 ---")
    
    # Configuration
    driver_id = 5  # Pierre Bernard
    tenant_id = 1
    vehicle_id = 3
    depot_loc_id = 1
    
    # Insert Trip
    insert_trip_query = f"""
    INSERT INTO "Trip" (
        "tripId", "driverId", "vehicleId", "status", "tripDate", 
        "tenantId", "createdAt", "depotId"
    ) VALUES (
        'TRIP-JULY9-2026',
        {driver_id},
        {vehicle_id},
        'READY',
        '2026-07-09',
        {tenant_id},
        NOW(),
        {depot_loc_id}
    )
    """
    
    res_trip = execute_query(insert_trip_query)
    if res_trip:
        print(f"✅ Trip inserted successfully!")
        # Get the inserted trip ID
        get_trip_id_query = """
            SELECT id FROM "Trip" 
            WHERE "tripId" = 'TRIP-JULY9-2026' 
            ORDER BY id DESC LIMIT 1
        """
        trip_result = execute_query(get_trip_id_query)
        if trip_result and "rows" in trip_result and len(trip_result["rows"]) > 0:
            trip_id = trip_result["rows"][0]["id"]
            print(f"📍 Trip ID: {trip_id}")
        else:
            print("❌ Could not retrieve trip ID")
            return
    else:
        print(f"❌ Trip insertion failed!")
        return

    print("\n--- ⚙️ INSERTING SHIPMENTS FOR TRIP ---")
    
    # Shipments configuration
    shipments_data = [
        {
            "shipmentNo": "SHIP-JULY9-001",
            "client_id": 1,
            "dest_loc_id": 18,  # Strasbourg
            "type": "OUTBOUND",
            "priority": "HIGH",
            "description": "Livraison dequipements informatiques",
            "quantity": 5,
            "uom": "PCS",
            "weight": 25.0,
            "volume": 0.3,
            "deliveryAddress": "1 Place de la République",
            "deliveryCity": "Strasbourg",
            "deliveryZipCode": "67000",
            "plannedStart": "2026-07-09 09:00:00",
            "plannedEnd": "2026-07-09 10:30:00",
            "distanceKm": 465.0
        },
        {
            "shipmentNo": "SHIP-JULY9-002",
            "client_id": 2,
            "dest_loc_id": 19,  # Nancy
            "type": "OUTBOUND",
            "priority": "MEDIUM",
            "description": "Livraison de matériel de bureau",
            "quantity": 10,
            "uom": "BOX",
            "weight": 15.0,
            "volume": 0.5,
            "deliveryAddress": "25 Rue Saint-Dizier",
            "deliveryCity": "Nancy",
            "deliveryZipCode": "54000",
            "plannedStart": "2026-07-09 11:00:00",
            "plannedEnd": "2026-07-09 12:30:00",
            "distanceKm": 380.0
        },
        {
            "shipmentNo": "SHIP-JULY9-003",
            "client_id": 3,
            "dest_loc_id": 20,  # Lyon
            "type": "OUTBOUND",
            "priority": "HIGH",
            "description": "Livraison urgente de pièces détachées",
            "quantity": 3,
            "uom": "PCS",
            "weight": 8.0,
            "volume": 0.15,
            "deliveryAddress": "15 Rue de la République",
            "deliveryCity": "Lyon",
            "deliveryZipCode": "69002",
            "plannedStart": "2026-07-09 14:00:00",
            "plannedEnd": "2026-07-09 15:30:00",
            "distanceKm": 465.0
        }
    ]
    
    for idx, shipment in enumerate(shipments_data):
        print(f"\n📦 Inserting shipment {idx + 1}/{len(shipments_data)}: {shipment['shipmentNo']}")
        
        insert_shipment_query = f"""
        INSERT INTO "Shipment" (
            "shipmentNo", "customerId", "type", "originId", "destinationId", 
            "priority", "status", "description", "quantity", "uom", "weight", "volume", 
            "tenantId", "createdAt", "updatedAt", "driverId", "vehicleId", "plannedStart", "plannedEnd", "distanceKm",
            "deliveryAddress", "deliveryCity", "deliveryZipCode"
        ) VALUES (
            '{shipment["shipmentNo"]}',
            {shipment["client_id"]},
            '{shipment["type"]}',
            {depot_loc_id},
            {shipment["dest_loc_id"]},
            '{shipment["priority"]}',
            'TO_PLAN',
            '{shipment["description"]}',
            {shipment["quantity"]},
            '{shipment["uom"]}',
            {shipment["weight"]},
            {shipment["volume"]},
            {tenant_id},
            NOW(),
            NOW(),
            {driver_id},
            {vehicle_id},
            '{shipment["plannedStart"]}',
            '{shipment["plannedEnd"]}',
            {shipment["distanceKm"]},
            '{shipment["deliveryAddress"]}',
            '{shipment["deliveryCity"]}',
            '{shipment["deliveryZipCode"]}'
        )
        """
        
        res_shipment = execute_query(insert_shipment_query)
        if res_shipment:
            print(f"✅ Shipment {shipment['shipmentNo']} inserted successfully!")
            
            # Get the inserted shipment ID
            get_shipment_id_query = f"""
                SELECT id FROM "Shipment" 
                WHERE "shipmentNo" = '{shipment["shipmentNo"]}' 
                ORDER BY id DESC LIMIT 1
            """
            shipment_result = execute_query(get_shipment_id_query)
            if shipment_result and "rows" in shipment_result and len(shipment_result["rows"]) > 0:
                shipment_id = shipment_result["rows"][0]["id"]
                print(f"📍 Shipment ID: {shipment_id}")
                
                # Link shipment to trip
                insert_trip_link_query = f"""
                INSERT INTO "TripShipmentLink" (
                    "tripId", "shipmentId", "role", "sequence", "status", "createdAt", "updatedAt"
                ) VALUES (
                    {trip_id},
                    {shipment_id},
                    'BOTH',
                    {idx + 1},
                    'TO_PLAN',
                    NOW(),
                    NOW()
                )
                """
                
                res_link = execute_query(insert_trip_link_query)
                if res_link:
                    print(f"✅ Shipment linked to trip successfully!")
                else:
                    print(f"❌ Failed to link shipment to trip")
            else:
                print("❌ Could not retrieve shipment ID")
        else:
            print(f"❌ Shipment insertion failed!")

    print("\n✅ All operations completed!")

if __name__ == "__main__":
    main()
