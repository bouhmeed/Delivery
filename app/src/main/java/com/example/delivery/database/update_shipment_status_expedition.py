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
    print("--- ⚙️ UPDATING SHIPMENT STATUS TO EXPEDITION ---")
    
    shipment_id = 158
    
    # Update Shipment status
    update_shipment_query = f"""
        UPDATE "Shipment" 
        SET status = 'EXPEDITION', "updatedAt" = NOW()
        WHERE id = {shipment_id}
    """
    
    res_shipment = execute_query(update_shipment_query)
    if res_shipment:
        print(f"✅ Shipment {shipment_id} status updated to EXPEDITION")
    else:
        print(f"❌ Failed to update shipment {shipment_id} status")
    
    # Update TripShipmentLink status
    update_link_query = f"""
        UPDATE "TripShipmentLink" 
        SET status = 'EXPEDITION', "updatedAt" = NOW()
        WHERE "shipmentId" = {shipment_id}
    """
    
    res_link = execute_query(update_link_query)
    if res_link:
        print(f"✅ TripShipmentLink for shipment {shipment_id} status updated to EXPEDITION")
    else:
        print(f"❌ Failed to update TripShipmentLink for shipment {shipment_id} status")

if __name__ == "__main__":
    main()
