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
    print("--- ⚙️ SYNCHRONIZING LOCATION SEQUENCE ---")
    sync_query = 'SELECT setval(\'"Location_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "Location"), 1), false)'
    execute_query(sync_query)
    print("✅ Location sequence synchronized!")

    print("\n--- ⚙️ INSERTING NANCY LAXOU LOCATION ---")
    
    insert_location_query = """
    INSERT INTO "Location" (
        name, address, city, "postalCode", "tenantId", "createdAt"
    ) VALUES (
        'Nancy - Agence Laxou',
        '25 Avenue du Général Leclerc',
        'Nancy',
        '54600',
        1,
        NOW()
    )
    """
    
    res_location = execute_query(insert_location_query)
    if res_location:
        print(f"✅ Nancy Laxou location inserted successfully!")
        # Get the inserted location ID
        get_location_id_query = """
            SELECT id FROM "Location" 
            WHERE name = 'Nancy - Agence Laxou' 
            ORDER BY id DESC LIMIT 1
        """
        location_result = execute_query(get_location_id_query)
        if location_result and "rows" in location_result and len(location_result["rows"]) > 0:
            location_id = location_result["rows"][0]["id"]
            print(f"📍 Location ID: {location_id}")
        else:
            print("❌ Could not retrieve location ID")
    else:
        print(f"❌ Location insertion failed!")

if __name__ == "__main__":
    main()
