import sys
import requests
import json

# Configuration Neon
NEON_SQL_ENDPOINT = "https://ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/sql"
NEON_CONNECTION_STRING = "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require"

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

def create_test_table():
    """Create a test table in the database"""
    query = """
        CREATE TABLE IF NOT EXISTS "test-table" (
            id SERIAL PRIMARY KEY,
            name TEXT NOT NULL,
            description TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """
    
    payload = {
        "query": query
    }
    
    headers = {
        "Content-Type": "application/json",
        "neon-connection-string": NEON_CONNECTION_STRING
    }
    
    print("📋 Requête SQL pour créer la table test-table...")
    print(query)
    print("\n" + "="*80 + "\n")
    
    try:
        response = requests.post(NEON_SQL_ENDPOINT, json=payload, headers=headers, timeout=30)
        response.raise_for_status()
        
        result = response.json()
        
        print("✅ Réponse du serveur:")
        print(json.dumps(result, indent=2))
        print("\n" + "="*80)
        print("\n🎉 Table 'test-table' créée avec succès !")
        
    except requests.exceptions.RequestException as e:
        print(f"❌ Erreur de connexion: {e}")
    except Exception as e:
        print(f"❌ Erreur: {e}")

if __name__ == "__main__":
    create_test_table()
