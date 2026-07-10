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
    print("--- ⚙️ FINDING LIPSHEIM LOCATION ---")
    
    # Find Lipsheim location
    find_location_query = """
        SELECT id, name, address, city, "postalCode" FROM "Location" 
        WHERE LOWER(city) LIKE '%lipsheim%' OR LOWER(name) LIKE '%lipsheim%'
    """
    
    location_result = execute_query(find_location_query)
    if location_result and "rows" in location_result and len(location_result["rows"]) > 0:
        lipsheim_location = location_result["rows"][0]
        lipsheim_id = lipsheim_location["id"]
        print(f"✅ Found Lipsheim location: ID={lipsheim_id}, {lipsheim_location['name']}, {lipsheim_location['address']}, {lipsheim_location['city']}")
    else:
        print("❌ Lipsheim location not found, creating it...")
        
        # Create Lipsheim location
        insert_location_query = """
            INSERT INTO "Location" (name, address, city, "postalCode", "tenantId", "createdAt")
            VALUES ('Lipsheim', '5 Quai de la Loire', 'Lipsheim', '67120', 1, NOW())
        """
        
        insert_result = execute_query(insert_location_query)
        if insert_result:
            print("✅ Lipsheim location created successfully!")
            
            # Get the created location ID
            get_location_query = """
                SELECT id FROM "Location" 
                WHERE city = 'Lipsheim' 
                ORDER BY id DESC LIMIT 1
            """
            location_result = execute_query(get_location_query)
            if location_result and "rows" in location_result:
                lipsheim_id = location_result["rows"][0]["id"]
                print(f"📍 Lipsheim Location ID: {lipsheim_id}")
            else:
                print("❌ Could not retrieve Lipsheim location ID")
                return
        else:
            print("❌ Failed to create Lipsheim location")
            return
    
    print("\n--- ⚙️ UPDATING SHIPMENT-JULY9-003 DESTINATION ---")
    
    # Update shipment destination
    update_query = f"""
        UPDATE "Shipment" 
        SET "destinationId" = {lipsheim_id},
            "deliveryAddress" = '5 Quai de la Loire',
            "deliveryCity" = 'Lipsheim',
            "deliveryZipCode" = '67120',
            "updatedAt" = NOW()
        WHERE "shipmentNo" = 'SHIP-JULY9-003'
    """
    
    result = execute_query(update_query)
    if result:
        print(f"✅ Shipment SHIP-JULY9-003 destination updated successfully!")
        
        # Verify the update
        verify_query = """
            SELECT id, "shipmentNo", "destinationId", "deliveryAddress", "deliveryCity", "deliveryZipCode" FROM "Shipment" 
            WHERE "shipmentNo" = 'SHIP-JULY9-003'
        """
        
        verify_result = execute_query(verify_query)
        if verify_result and "rows" in verify_result:
            row = verify_result["rows"][0]
            print(f"\n📋 Updated shipment details:")
            print(f"   - Shipment No: {row['shipmentNo']}")
            print(f"   - Destination ID: {row['destinationId']}")
            print(f"   - Address: {row['deliveryAddress']}")
            print(f"   - City: {row['deliveryCity']}")
            print(f"   - Zip Code: {row['deliveryZipCode']}")
    else:
        print(f"❌ Failed to update shipment destination")

if __name__ == "__main__":
    main()
