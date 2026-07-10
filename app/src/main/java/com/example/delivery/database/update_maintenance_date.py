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
    print("--- ⚙️ UPDATING MAINTENANCE DATE TO 2027 ---")
    
    # Update maintenance record ID 5 to 2027
    update_query = """
        UPDATE "VehicleMaintenance" 
        SET date = '2027-05-05 11:00:00.000',
            "nextMaintenance" = '2027-05-12 11:00:00.000'
        WHERE id = 5
    """
    
    result = execute_query(update_query)
    if result:
        print(f"✅ Maintenance record ID 5 updated successfully!")
        
        # Verify the update
        verify_query = """
            SELECT id, "vehicleId", type, date, "nextMaintenance", "estimatedCost", notes, technician, status FROM "VehicleMaintenance" 
            WHERE id = 5
        """
        
        verify_result = execute_query(verify_query)
        if verify_result and "rows" in verify_result:
            row = verify_result["rows"][0]
            print(f"\n📋 Updated maintenance record:")
            print(f"   - ID: {row['id']}")
            print(f"   - Vehicle: {row['vehicleId']}")
            print(f"   - Type: {row['type']}")
            print(f"   - Date: {row['date']}")
            print(f"   - Next Maintenance: {row['nextMaintenance']}")
            print(f"   - Estimated Cost: {row['estimatedCost']}")
            print(f"   - Notes: {row['notes']}")
            print(f"   - Technician: {row['technician']}")
            print(f"   - Status: {row['status']}")
    else:
        print(f"❌ Failed to update maintenance record")

if __name__ == "__main__":
    main()
