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
    print("--- ⚙️ SYNCHRONIZING SHIPMENT SEQUENCE ---")
    sync_query = 'SELECT setval(\'"Shipment_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "Shipment"), 1), false)'
    execute_query(sync_query)
    print("✅ Shipment sequence synchronized!")

    print("\n--- ⚙️ INSERTING SHIPMENT RECORD ---")
    
    # Configuration
    trip_id = 111
    driver_id = 5  # Pierre Bernard
    tenant_id = 1
    vehicle_id = 3
    depot_loc_id = 1
    dest_loc_id = 18  # Strasbourg
    client_id = 1
    
    insert_shipment_query = f"""
    INSERT INTO "Shipment" (
        "shipmentNo", "customerId", "type", "originId", "destinationId", 
        "priority", "status", "description", "quantity", "uom", "weight", "volume", 
        "tenantId", "createdAt", "updatedAt", "driverId", "vehicleId", "plannedStart", "plannedEnd", "distanceKm",
        "deliveryAddress", "deliveryCity", "deliveryZipCode"
    ) VALUES (
        'SHIP-PIERRE-SINGLE-20260708',
        {client_id},
        'OUTBOUND',
        {depot_loc_id},
        {dest_loc_id},
        'HIGH',
        'TO_PLAN',
        'Colis unique pour test',
        1,
        'PCS',
        15.0,
        0.1,
        {tenant_id},
        NOW(),
        NOW(),
        {driver_id},
        {vehicle_id},
        '2026-07-08 09:00:00',
        '2026-07-08 10:00:00',
        465.0,
        '1 Place de la République',
        'Strasbourg',
        '67000'
    )
    """
    
    res_shipment = execute_query(insert_shipment_query)
    if res_shipment:
        print(f"✅ Shipment inserted successfully!")
        # Get the inserted shipment ID
        get_shipment_id_query = """
            SELECT id FROM "Shipment" 
            WHERE "shipmentNo" = 'SHIP-PIERRE-SINGLE-20260708' 
            ORDER BY id DESC LIMIT 1
        """
        shipment_result = execute_query(get_shipment_id_query)
        if shipment_result and "rows" in shipment_result and len(shipment_result["rows"]) > 0:
            shipment_id = shipment_result["rows"][0]["id"]
            print(f"📍 Shipment ID: {shipment_id}")
        else:
            print("❌ Could not retrieve shipment ID")
    else:
        print(f"❌ Shipment insertion failed!")

if __name__ == "__main__":
    main()
