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
    print("--- ⚙️ FINDING SELESTAT LOCATION ---")
    
    # Find Selestat location
    find_location_query = """
        SELECT id, name, address, city, "postalCode" FROM "Location" 
        WHERE LOWER(city) LIKE '%selestat%' OR LOWER(name) LIKE '%selestat%'
    """
    
    location_result = execute_query(find_location_query)
    if location_result and "rows" in location_result and len(location_result["rows"]) > 0:
        selestat_location = location_result["rows"][0]
        selestat_id = selestat_location["id"]
        print(f"✅ Found Selestat location: ID={selestat_id}, {selestat_location['name']}, {selestat_location['address']}, {selestat_location['city']}")
    else:
        print("❌ Selestat location not found, creating it...")
        
        # Create Selestat location
        insert_location_query = """
            INSERT INTO "Location" (name, address, city, "postalCode", "tenantId", "createdAt")
            VALUES ('Selestat', '14 Rue du Cimetiere', 'Selestat', '67600', 1, NOW())
        """
        
        insert_result = execute_query(insert_location_query)
        if insert_result:
            print("✅ Selestat location created successfully!")
            
            # Get the created location ID
            get_location_query = """
                SELECT id FROM "Location" 
                WHERE city = 'Selestat' 
                ORDER BY id DESC LIMIT 1
            """
            location_result = execute_query(get_location_query)
            if location_result and "rows" in location_result:
                selestat_id = location_result["rows"][0]["id"]
                print(f"📍 Selestat Location ID: {selestat_id}")
            else:
                print("❌ Could not retrieve Selestat location ID")
                return
        else:
            print("❌ Failed to create Selestat location")
            return
    
    print("\n--- ⚙️ SYNCHRONIZING SHIPMENT SEQUENCE ---")
    sync_query = 'SELECT setval(\'"Shipment_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "Shipment"), 1), false)'
    execute_query(sync_query)
    print("✅ Shipment sequence synchronized!")

    print("\n--- ⚙️ INSERTING SHIPMENT FOR SELESTAT ---")
    
    # Configuration
    trip_id = 112  # TRIP-JULY9-2026
    driver_id = 5  # Pierre Bernard
    tenant_id = 1
    vehicle_id = 3
    depot_loc_id = 1
    client_id = 1
    
    insert_shipment_query = f"""
    INSERT INTO "Shipment" (
        "shipmentNo", "customerId", "type", "originId", "destinationId", 
        "priority", "status", "description", "quantity", "uom", "weight", "volume", 
        "tenantId", "createdAt", "updatedAt", "driverId", "vehicleId", "plannedStart", "plannedEnd", "distanceKm",
        "deliveryAddress", "deliveryCity", "deliveryZipCode"
    ) VALUES (
        'SHIP-JULY9-003',
        {client_id},
        'OUTBOUND',
        {depot_loc_id},
        {selestat_id},
        'MEDIUM',
        'EXPEDITION',
        'Livraison a Selestat',
        2,
        'PCS',
        10.0,
        0.2,
        {tenant_id},
        NOW(),
        NOW(),
        {driver_id},
        {vehicle_id},
        '2026-07-09 14:00:00',
        '2026-07-09 15:30:00',
        450.0,
        '14 Rue du Cimetiere',
        'Selestat',
        '67600'
    )
    """
    
    res_shipment = execute_query(insert_shipment_query)
    if res_shipment:
        print(f"✅ Shipment inserted successfully!")
        # Get the inserted shipment ID
        get_shipment_id_query = """
            SELECT id FROM "Shipment" 
            WHERE "shipmentNo" = 'SHIP-JULY9-003' 
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
                3,
                'EXPEDITION',
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
