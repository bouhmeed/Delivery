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
    print("--- ⚙️ CHECKING TRIP 111 SHIPMENTS ---")
    
    trip_id = 111
    
    # Check TripShipmentLinks
    print("\n--- TRIP SHIPMENT LINKS ---")
    query = f"""
        SELECT id, "tripId", "shipmentId", sequence, status
        FROM "TripShipmentLink" 
        WHERE "tripId" = {trip_id}
        ORDER BY sequence ASC
    """
    
    result = execute_query(query)
    
    if result and "rows" in result:
        rows = result["rows"]
        print(f"Found {len(rows)} TripShipmentLinks:")
        print("="*80)
        for row in rows:
            print(f"ID: {row['id']}, TripID: {row['tripId']}, ShipmentID: {row['shipmentId']}, Sequence: {row['sequence']}, Status: {row['status']}")
    else:
        print("❌ No TripShipmentLinks found")
    
    # Check Shipments
    print("\n--- SHIPMENTS ---")
    query = """
        SELECT id, "shipmentNo", "deliveryAddress", "deliveryCity", status
        FROM "Shipment" 
        WHERE id IN (158, 159, 160)
        ORDER BY id ASC
    """
    
    result = execute_query(query)
    
    if result and "rows" in result:
        rows = result["rows"]
        print(f"Found {len(rows)} Shipments:")
        print("="*80)
        for row in rows:
            print(f"ID: {row['id']}, ShipmentNo: {row['shipmentNo']}, Address: {row['deliveryAddress']}, City: {row['deliveryCity']}, Status: {row['status']}")
    else:
        print("❌ No Shipments found")
    
    # Check the join query used by the repository
    print("\n--- REPOSITORY QUERY SIMULATION ---")
    query = f"""
        SELECT s.*, tsl.sequence as "tripSequence", tsl.status as "linkStatus", tsl.id as "tripShipmentLinkId", tsl."podDone", c.name as "clientName", c.phone as "clientPhone", l.address as "fullAddress", l.city as "locationCity", l."postalCode" as "locationPostalCode", ol.name as "originName", ol.address as "originAddress", ol.city as "originCity", ol."postalCode" as "originPostalCode"
        FROM "Shipment" s 
        JOIN "TripShipmentLink" tsl ON s.id = tsl."shipmentId"
        LEFT JOIN "Client" c ON s."customerId" = c.id
        LEFT JOIN "Location" l ON s."destinationId" = l.id
        LEFT JOIN "Location" ol ON s."originId" = ol.id
        WHERE tsl."tripId" = {trip_id}
        ORDER BY tsl.sequence ASC
    """
    
    result = execute_query(query)
    
    if result and "rows" in result:
        rows = result["rows"]
        print(f"Found {len(rows)} deliveries from repository query:")
        print("="*80)
        for row in rows:
            print(f"ShipmentID: {row['id']}, ShipmentNo: {row['shipmentNo']}, Sequence: {row['tripSequence']}, Address: {row['deliveryAddress']}, City: {row['deliveryCity']}")
    else:
        print("❌ No deliveries found from repository query")

if __name__ == "__main__":
    main()
