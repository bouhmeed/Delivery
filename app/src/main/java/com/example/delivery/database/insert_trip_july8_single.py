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
    print("--- ⚙️ SYNCHRONIZING TRIP SEQUENCE ---")
    sync_query = 'SELECT setval(\'"Trip_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "Trip"), 1), false)'
    execute_query(sync_query)
    print("✅ Trip sequence synchronized!")

    print("\n--- ⚙️ INSERTING TRIP RECORD ---")
    
    # Configuration
    driver_id = 5  # Pierre Bernard
    tenant_id = 1
    vehicle_id = 3
    depot_loc_id = 1
    trip_date = "2026-07-08 08:00:00"
    
    insert_trip_query = f"""
    INSERT INTO "Trip" (
        "tripId", "tripDate", "depotId", "driverId", "vehicleId", "status", "tenantId", "createdAt"
    ) VALUES (
        'TRIP-PIERRE-20260708-SINGLE',
        '{trip_date}',
        {depot_loc_id},
        {driver_id},
        {vehicle_id},
        'PLANNING',
        {tenant_id},
        NOW()
    )
    """
    
    res_trip = execute_query(insert_trip_query)
    if res_trip:
        print(f"✅ Trip inserted successfully!")
        # Get the inserted trip ID
        get_trip_id_query = """
            SELECT id FROM "Trip" 
            WHERE "tripId" = 'TRIP-PIERRE-20260708-SINGLE' 
            ORDER BY id DESC LIMIT 1
        """
        trip_result = execute_query(get_trip_id_query)
        if trip_result and "rows" in trip_result and len(trip_result["rows"]) > 0:
            trip_id = trip_result["rows"][0]["id"]
            print(f"📍 Trip ID: {trip_id}")
        else:
            print("❌ Could not retrieve trip ID")
    else:
        print(f"❌ Trip insertion failed!")

if __name__ == "__main__":
    main()
