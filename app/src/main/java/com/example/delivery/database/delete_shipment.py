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
    shipment_no = "SHIP-JULY9-003"
    
    print(f"--- 🗑️ DELETING SHIPMENT: {shipment_no} ---")
    
    # First, check if the shipment exists
    check_query = f"""
        SELECT id, "shipmentNo", status FROM "Shipment" 
        WHERE "shipmentNo" = '{shipment_no}'
    """
    
    check_result = execute_query(check_query)
    if check_result and "rows" in check_result and len(check_result["rows"]) > 0:
        shipment_id = check_result["rows"][0]["id"]
        print(f"✅ Found shipment: ID={shipment_id}, No={shipment_no}")
        
        # First, delete related records in ShipmentProof
        print(f"🗑️ Deleting related ShipmentProof records...")
        delete_proof_query = f"""
            DELETE FROM "ShipmentProof" 
            WHERE "shipmentId" = {shipment_id}
        """
        
        delete_proof_result = execute_query(delete_proof_query)
        if delete_proof_result:
            print(f"✅ ShipmentProof records deleted successfully!")
        else:
            print(f"⚠️ Failed to delete ShipmentProof records (might not exist)")
        
        # Then delete related records in ShipmentLine
        print(f"🗑️ Deleting related ShipmentLine records...")
        delete_line_query = f"""
            DELETE FROM "ShipmentLine" 
            WHERE "shipmentId" = {shipment_id}
        """
        
        delete_line_result = execute_query(delete_line_query)
        if delete_line_result:
            print(f"✅ ShipmentLine records deleted successfully!")
        else:
            print(f"⚠️ Failed to delete ShipmentLine records (might not exist)")
        
        # Then delete related records in TripShipmentLink
        print(f"🗑️ Deleting related TripShipmentLink records...")
        delete_trip_link_query = f"""
            DELETE FROM "TripShipmentLink" 
            WHERE "shipmentId" = {shipment_id}
        """
        
        delete_trip_link_result = execute_query(delete_trip_link_query)
        if delete_trip_link_result:
            print(f"✅ TripShipmentLink records deleted successfully!")
        else:
            print(f"⚠️ Failed to delete TripShipmentLink records (might not exist)")
        
        # Then delete related records in ShipmentReturns
        print(f"🗑️ Deleting related ShipmentReturns records...")
        delete_returns_query = f"""
            DELETE FROM "ShipmentReturns" 
            WHERE "shipmentid" = {shipment_id}
        """
        
        delete_returns_result = execute_query(delete_returns_query)
        if delete_returns_result:
            print(f"✅ ShipmentReturns records deleted successfully!")
        else:
            print(f"⚠️ Failed to delete ShipmentReturns records (might not exist)")
        
        # Then delete the shipment
        print(f"🗑️ Deleting shipment...")
        delete_query = f"""
            DELETE FROM "Shipment" 
            WHERE "shipmentNo" = '{shipment_no}'
        """
        
        delete_result = execute_query(delete_query)
        if delete_result:
            print(f"✅ Shipment {shipment_no} deleted successfully!")
        else:
            print(f"❌ Failed to delete shipment {shipment_no}")
    else:
        print(f"⚠️ Shipment {shipment_no} not found in database")

if __name__ == "__main__":
    main()
