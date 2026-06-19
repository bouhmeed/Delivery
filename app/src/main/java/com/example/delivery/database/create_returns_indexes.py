import sys
import requests
import json

# Configuration Neon
NEON_SQL_ENDPOINT = "https://ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/sql"
NEON_CONNECTION_STRING = "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require"

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

def create_indexes():
    """Create indexes for ShipmentReturns and ShipmentReturnDefects tables"""

    headers = {
        "Content-Type": "application/json",
        "neon-connection-string": NEON_CONNECTION_STRING
    }

    indexes = [
        "CREATE INDEX IF NOT EXISTS \"ShipmentReturns_shipmentId_idx\" ON \"ShipmentReturns\"(\"shipmentId\")",
        "CREATE INDEX IF NOT EXISTS \"ShipmentReturns_tripShipmentLinkId_idx\" ON \"ShipmentReturns\"(\"tripShipmentLinkId\")",
        "CREATE INDEX IF NOT EXISTS \"ShipmentReturns_tenantId_idx\" ON \"ShipmentReturns\"(\"tenantId\")",
        "CREATE INDEX IF NOT EXISTS \"ShipmentReturnDefects_shipmentReturnId_idx\" ON \"ShipmentReturnDefects\"(\"shipmentReturnId\")",
        "CREATE INDEX IF NOT EXISTS \"ShipmentReturnDefects_itemId_idx\" ON \"ShipmentReturnDefects\"(\"itemId\")"
    ]

    print("📋 Création des indexes...")
    print("="*80)

    for i, index_query in enumerate(indexes, 1):
        try:
            payload = {"query": index_query}
            response = requests.post(NEON_SQL_ENDPOINT, json=payload, headers=headers, timeout=30)
            response.raise_for_status()
            result = response.json()
            print(f"✅ Index {i}/{len(indexes)} créé: {result.get('command')}")
        except Exception as e:
            print(f"❌ Erreur index {i}/{len(indexes)}: {e}")

    print("\n" + "="*80)
    print("🎉 Création des indexes terminée !")

if __name__ == "__main__":
    create_indexes()
