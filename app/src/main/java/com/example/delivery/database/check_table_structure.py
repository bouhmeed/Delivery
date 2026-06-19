import sys
import requests
import json

# Configuration Neon
NEON_SQL_ENDPOINT = "https://ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/sql"
NEON_CONNECTION_STRING = "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require"

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

def check_table_structure():
    """Check the structure of ShipmentReturns table"""
    
    headers = {
        "Content-Type": "application/json",
        "neon-connection-string": NEON_CONNECTION_STRING
    }
    
    # Query to get table structure
    query = """
        SELECT 
            column_name,
            data_type,
            is_nullable,
            column_default
        FROM information_schema.columns
        WHERE table_name = 'ShipmentReturns'
        ORDER BY ordinal_position;
    """
    
    print("📋 Structure de la table ShipmentReturns...")
    print("="*80)
    
    try:
        payload = {"query": query}
        response = requests.post(NEON_SQL_ENDPOINT, json=payload, headers=headers, timeout=30)
        response.raise_for_status()
        result = response.json()
        
        rows = result.get('rows', [])
        
        if rows:
            print(f"{'Column Name':<30} {'Data Type':<20} {'Nullable':<10} {'Default'}")
            print("-"*80)
            for row in rows:
                col_name = row.get('column_name', '')
                data_type = row.get('data_type', '')
                is_nullable = row.get('is_nullable', '')
                default = str(row.get('column_default', ''))
                print(f"{col_name:<30} {data_type:<20} {is_nullable:<10} {default}")
        else:
            print("❌ Table ShipmentReturns n'existe pas ou est vide")
            
    except Exception as e:
        print(f"❌ Erreur: {e}")

if __name__ == "__main__":
    check_table_structure()
