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
        print(f"Error running query: {query}")
        print(f"HTTP Code: {e.code}")
        try:
            error_body = e.read().decode("utf-8")
            print(f"Error Body: {error_body}")
        except Exception as read_err:
            print(f"Could not read error body: {read_err}")
        return None

def get_next_val(seq_name):
    res = execute_query(f"SELECT nextval('{seq_name}') as val")
    if res and "rows" in res and len(res["rows"]) > 0:
        return res["rows"][0]["val"]
    return None

def main():
    print("--- ⚙️ SYNCHRONIZING ALL SEQUENCES TO PREVENT CLASHES ---")
    sync_queries = [
        'SELECT setval(\'"Trip_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "Trip"), 1), false)',
        'SELECT setval(\'"Shipment_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "Shipment"), 1), false)',
        'SELECT setval(\'"TripShipmentLink_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "TripShipmentLink"), 1), false)',
        'SELECT setval(\'"ShipmentLine_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "ShipmentLine"), 1), false)',
        'SELECT setval(\'"TripStop_id_seq"\', COALESCE((SELECT MAX(id)+1 FROM "TripStop"), 1), false)'
    ]
    for sq in sync_queries:
        execute_query(sq)
    print("Sequences synchronized successfully!")

    print("\n--- ⚙️ CLEANING UP TEST TOUR DATA FOR 20/05/2026 ---")
    driver_id = 5
    clean_queries = [
        f'DELETE FROM "TripShipmentLink" WHERE "tripId" IN (SELECT id FROM "Trip" WHERE "driverId" = {driver_id} AND "tripDate"::text LIKE \'2026-05-20%\')',
        f'DELETE FROM "TripStop" WHERE "tripId" IN (SELECT id FROM "Trip" WHERE "driverId" = {driver_id} AND "tripDate"::text LIKE \'2026-05-20%\')',
        f'DELETE FROM "ShipmentProof" WHERE "shipmentId" IN (SELECT id FROM "Shipment" WHERE "driverId" = {driver_id} AND "plannedStart"::text LIKE \'2026-05-20%\')',
        f'DELETE FROM "ShipmentLine" WHERE "shipmentId" IN (SELECT id FROM "Shipment" WHERE "driverId" = {driver_id} AND "plannedStart"::text LIKE \'2026-05-20%\')',
        f'DELETE FROM "Shipment" WHERE "driverId" = {driver_id} AND "plannedStart"::text LIKE \'2026-05-20%\'',
        f'DELETE FROM "Trip" WHERE "driverId" = {driver_id} AND "tripDate"::text LIKE \'2026-05-20%\''
    ]
    for q in clean_queries:
        execute_query(q)
    print("Cleanup done!")

    print("\n--- ⚙️ GENERATING NEW DATABASE IDS ---")
    trip_id = get_next_val('"Trip_id_seq"')
    ship_ids = [get_next_val('"Shipment_id_seq"') for _ in range(5)]
    
    print(f"Generated Trip ID: {trip_id}")
    print(f"Generated Shipment IDs: {ship_ids}")
    
    if not trip_id or any(s is None for s in ship_ids):
        print("Failed to generate IDs.")
        return

    # Data configurations using existing IDs we analyzed
    tenant_id = 1
    vehicle_id = 3
    depot_loc_id = 1
    dest_locs = [2, 7, 8, 9, 10]
    clients = [1, 2, 5, 6, 7]
    items = [1, 2, 3, 1, 3]

    print("\n--- ⚙️ INSERTING NEW TRIP ---")
    insert_trip_query = f"""
    INSERT INTO "Trip" ("id", "tripId", "tripDate", "depotId", "driverId", "vehicleId", "status", "tenantId", "createdAt")
    VALUES (
        {trip_id},
        'TRIP-PIERRE-20260520-{trip_id}',
        '2026-05-20 08:00:00',
        {depot_loc_id},
        {driver_id},
        {vehicle_id},
        'READY',
        {tenant_id},
        NOW()
    )
    """
    res_trip = execute_query(insert_trip_query)
    print(f"Trip insertion result: {res_trip.get('command') if res_trip else 'Failed'}")

    print("\n--- ⚙️ INSERTING 5 SHIPMENTS ---")
    shipment_details = [
        ("Colis d''équipements électroniques de pointe", 12, "PCS", 18.0, 0.12, "09:00:00", "10:00:00", 465.0, "45 Avenue des Champs-Élysées", "Paris", "75008"),
        ("Palette de produits manufacturés de luxe", 1, "PAL", 180.0, 1.2, "10:30:00", "11:30:00", 150.0, "Rue du Rhône 30", "Genève", "1204"),
        ("Cartons d''archives hautement confidentielles", 8, "CTN", 30.0, 0.25, "12:00:00", "13:00:00", 310.0, "Bahnhofstrasse 10", "Zürich", "8001"),
        ("Matériel médical d''urgence et chirurgical", 4, "BOX", 15.0, 0.08, "14:00:00", "15:00:00", 230.0, "Avenue d''Ouchy 40", "Lausanne", "1006"),
        ("Fournitures administratives et papiers", 25, "PCS", 10.0, 0.06, "15:30:00", "16:30:00", 380.0, "Aeschenvorstadt 15", "Basel", "4051")
    ]

    for i in range(5):
        s_id = ship_ids[i]
        c_id = clients[i]
        dest_loc = dest_locs[i]
        desc, qty, uom, w, v, p_start, p_end, dist, addr, city, zip_code = shipment_details[i]
        
        insert_shipment_query = f"""
        INSERT INTO "Shipment" (
            "id", "shipmentNo", "customerId", "type", "originId", "destinationId", 
            "priority", "status", "description", "quantity", "uom", "weight", "volume", 
            "tenantId", "createdAt", "updatedAt", "driverId", "vehicleId", "plannedStart", "plannedEnd", "distanceKm",
            "deliveryAddress", "deliveryCity", "deliveryZipCode"
        ) VALUES (
            {s_id},
            'SHIP-PIERRE-20-{s_id}',
            {c_id},
            'OUTBOUND',
            {depot_loc_id},
            {dest_loc},
            'HIGH',
            'TO_PLAN',
            '{desc}',
            {qty},
            '{uom}',
            {w},
            {v},
            {tenant_id},
            NOW(),
            NOW(),
            {driver_id},
            {vehicle_id},
            '2026-05-20 {p_start}',
            '2026-05-20 {p_end}',
            {dist},
            '{addr}',
            '{city}',
            '{zip_code}'
        )
        """
        res_s = execute_query(insert_shipment_query)
        print(f"Shipment {s_id} insertion result: {res_s.get('command') if res_s else 'Failed'}")

        # Insert line item
        insert_line_query = f"""
        INSERT INTO "ShipmentLine" ("id", "shipmentId", "itemId", "quantity", "uom", "weight", "volume")
        VALUES (
            nextval('"ShipmentLine_id_seq"'),
            {s_id},
            {items[i]},
            {qty}.0,
            '{uom}',
            {w},
            {v}
        )
        """
        execute_query(insert_line_query)

        # Insert link
        insert_link_query = f"""
        INSERT INTO "TripShipmentLink" (
            "id", "tripId", "shipmentId", "role", "status", "podDone", "returnsDone", "sequence", "createdAt", "updatedAt"
        ) VALUES (
            nextval('"TripShipmentLink_id_seq"'),
            {trip_id},
            {s_id},
            'BOTH',
            'NON_DEMARRE',
            false,
            false,
            {i + 1},
            NOW(),
            NOW()
        )
        """
        execute_query(insert_link_query)

    print("\n--- ⚙️ INSERTING TRIP STOPS ---")
    # Pickup stop at depot
    execute_query(f"""
        INSERT INTO "TripStop" ("id", "tripId", "sequence", "locationId", "stopType")
        VALUES (nextval('"TripStop_id_seq"'), {trip_id}, 1, {depot_loc_id}, 'PICKUP')
    """)
    # Delivery stops
    for i in range(5):
        dest_loc = dest_locs[i]
        execute_query(f"""
            INSERT INTO "TripStop" ("id", "tripId", "sequence", "locationId", "stopType")
            VALUES (nextval('"TripStop_id_seq"'), {trip_id}, {i + 2}, {dest_loc}, 'DELIVERY')
        """)

    print(f"\n✅ SUCCESS: Tournée du 20/05/2026 (Trip ID: {trip_id}) créée de A à Z avec succès sur Neon PostgreSQL pour Pierre Bernard !")

if __name__ == "__main__":
    main()
