import sys
import requests
import json

# Configuration Neon
NEON_SQL_ENDPOINT = "https://ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/sql"
NEON_CONNECTION_STRING = "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require"

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

def list_tables():
    """List all tables in the database"""
    query = """
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public'
        ORDER BY table_name
    """
    
    payload = {
        "query": query
    }
    
    headers = {
        "Content-Type": "application/json",
        "neon-connection-string": NEON_CONNECTION_STRING
    }
    
    print("📋 Requête SQL pour lister les tables...")
    print(query)
    print("\n" + "="*80 + "\n")
    
    try:
        response = requests.post(NEON_SQL_ENDPOINT, json=payload, headers=headers, timeout=30)
        response.raise_for_status()
        
        result = response.json()
        
        if "rows" in result:
            tables = result["rows"]
            print(f"✅ {len(tables)} tables trouvées dans la base de données:\n")
            
            for i, table in enumerate(tables, 1):
                table_name = table.get("table_name", "")
                print(f"{i}. {table_name}")
            
            print("\n" + "="*80)
            print(f"\n📊 Total: {len(tables)} tables")
        else:
            print("❌ Erreur: Aucune table trouvée ou format de réponse invalide")
            print("Réponse:", json.dumps(result, indent=2))
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Erreur de connexion: {e}")
    except Exception as e:
        print(f"❌ Erreur: {e}")

if __name__ == "__main__":
    list_tables()
