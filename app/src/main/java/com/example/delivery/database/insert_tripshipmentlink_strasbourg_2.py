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
    print("--- ⚙️ SYNCHRONIZING TRIP SHIPMENT LINK SEQUENCE ---")
    sync_query = 'SELECT setval(\'"TripShipmentLink_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "TripShipmentLink"), 1), false)'
    execute_query(sync_query)
    print("✅ TripShipmentLink sequence synchronized!")

    print("\n--- ⚙️ INSERTING TRIP SHIPMENT LINK FOR SECOND STRASBOURG SHIPMENT ---")
    
    # Configuration
    trip_id = 111
    shipment_id = 159
    
    insert_link_query = f"""
    INSERT INTO "TripShipmentLink" (
        "tripId", "shipmentId", "role", "status", "podDone", "returnsDone", "sequence", "createdAt", "updatedAt"
    ) VALUES (
        {trip_id},
        {shipment_id},
        'BOTH',
        'EXPEDITION',
        false,
        false,
        2,
        NOW(),
        NOW()
    )
    """
    
    res_link = execute_query(insert_link_query)
    if res_link:
        print(f"✅ TripShipmentLink inserted successfully!")
        print(f"📍 Trip ID: {trip_id}")
        print(f"📍 Shipment ID: {shipment_id}")
        print(f"📍 Sequence: 2")
    else:
        print(f"❌ TripShipmentLink insertion failed!")

if __name__ == "__main__":
    main()
