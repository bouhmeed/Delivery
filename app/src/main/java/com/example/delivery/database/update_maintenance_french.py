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
    print("--- ⚙️ FINDING CURRENT MAINTENANCE TYPES ---")
    
    # Find all current maintenance types and notes
    find_maintenance_query = """
        SELECT id, "vehicleId", type, notes FROM "VehicleMaintenance"
    """
    
    maintenance_result = execute_query(find_maintenance_query)
    if maintenance_result and "rows" in maintenance_result and len(maintenance_result["rows"]) > 0:
        print(f"✅ Found {len(maintenance_result['rows'])} maintenance records")
        
        # Show current data
        print("\n📋 Current maintenance records:")
        for row in maintenance_result["rows"]:
            print(f"   - ID: {row['id']}, Vehicle: {row['vehicleId']}, Type: {row['type']}, Notes: {row['notes']}")
    else:
        print("❌ No maintenance records found")
        return
    
    print("\n--- ⚙️ UPDATING MAINTENANCE TYPES TO FRENCH ---")
    
    # Mapping of English to French maintenance types
    type_mapping = {
        "Suspension": "Suspension",
        "Oil Change": "Vidange",
        "Brake Service": "Service de freins",
        "Brake Inspection": "Inspection des freins",
        "Tire Rotation": "Rotation des pneus",
        "Engine Service": "Service moteur",
        "Transmission Service": "Service de transmission",
        "Air Filter": "Filtre à air",
        "Air Filter Replacement": "Remplacement du filtre à air",
        "Coolant Flush": "Vidange du liquide de refroidissement",
        "Battery Replacement": "Remplacement de batterie",
        "Battery Check": "Vérification de la batterie",
        "Inspection": "Inspection",
        "General Maintenance": "Maintenance générale",
        "Repair": "Réparation",
        "Preventive Maintenance": "Maintenance préventive"
    }
    
    # Update each maintenance type
    updated_count = 0
    for english_type, french_type in type_mapping.items():
        update_query = f"""
            UPDATE "VehicleMaintenance" 
            SET type = '{french_type}'
            WHERE type = '{english_type}'
        """
        
        result = execute_query(update_query)
        if result:
            print(f"✅ Updated '{english_type}' → '{french_type}'")
            updated_count += 1
        else:
            print(f"❌ Failed to update '{english_type}'")
    
    print(f"\n📊 Total types updated: {updated_count}")
    
    print("\n--- ⚙️ UPDATING MAINTENANCE NOTES TO FRENCH ---")
    
    # Common English notes to French mapping
    notes_mapping = {
        "Regular maintenance": "Maintenance régulière",
        "Urgent repair needed": "Réparation urgente nécessaire",
        "Scheduled maintenance": "Maintenance programmée",
        "Preventive check": "Contrôle préventif",
        "Wear and tear": "Usure normale",
        "Replace parts": "Remplacer les pièces",
        "Check brakes": "Vérifier les freins",
        "Check fluids": "Vérifier les fluides",
        "Safety inspection": "Inspection de sécurité",
        "Performance check": "Contrôle des performances",
        "Regular oil change with filter replacement": "Vidange régulière avec remplacement du filtre",
        "Front brake pads check and replacement if needed": "Vérification et remplacement des plaquettes de frein avant si nécessaire",
        "Rotate all four tires and check pressure": "Rotation des quatre pneus et vérification de la pression",
        "Battery voltage test and terminal cleaning": "Test de tension de la batterie et nettoyage des bornes",
        "Replace engine air filter": "Remplacer le filtre à air du moteur"
    }
    
    # Update each note pattern
    notes_updated = 0
    for english_note, french_note in notes_mapping.items():
        update_query = f"""
            UPDATE "VehicleMaintenance" 
            SET notes = '{french_note}'
            WHERE notes = '{english_note}'
        """
        
        result = execute_query(update_query)
        if result:
            print(f"✅ Updated note '{english_note}' → '{french_note}'")
            notes_updated += 1
        else:
            print(f"❌ Failed to update note '{english_note}'")
    
    print(f"\n📊 Total notes updated: {notes_updated}")
    
    print("\n--- ⚙️ VERIFYING UPDATES ---")
    
    # Verify the updates
    verify_query = """
        SELECT id, "vehicleId", type, notes FROM "VehicleMaintenance"
    """
    
    verify_result = execute_query(verify_query)
    if verify_result and "rows" in verify_result:
        print(f"\n📋 Updated maintenance records:")
        for row in verify_result["rows"]:
            print(f"   - ID: {row['id']}, Vehicle: {row['vehicleId']}, Type: {row['type']}, Notes: {row['notes']}")
    
    print("\n✅ Maintenance types and notes update completed!")

if __name__ == "__main__":
    main()
