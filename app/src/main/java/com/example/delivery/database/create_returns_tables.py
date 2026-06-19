import sys
import requests
import json

# Configuration Neon
NEON_SQL_ENDPOINT = "https://ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/sql"
NEON_CONNECTION_STRING = "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require"

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

def create_returns_tables():
    """Create ShipmentReturns and ShipmentReturnDefects tables"""

    # Table 1: ShipmentReturns
    query1 = """
        CREATE TABLE IF NOT EXISTS "ShipmentReturns" (
            id SERIAL PRIMARY KEY,
            shipmentId INTEGER NOT NULL REFERENCES "Shipment"(id),
            tripShipmentLinkId INTEGER REFERENCES "TripShipmentLink"(id),
            
            -- Statuts de récupération
            packagesRecovered BOOLEAN NOT NULL DEFAULT false,
            packagingRecovered BOOLEAN NOT NULL DEFAULT false,
            
            -- Quantités récupérées
            palettes INTEGER DEFAULT 0,
            caisses INTEGER DEFAULT 0,
            bouteilles INTEGER DEFAULT 0,
            futs INTEGER DEFAULT 0,
            autre INTEGER DEFAULT 0,
            
            -- Commentaire
            comment TEXT,
            
            -- Photo de preuve (pattern ShipmentProof)
            proofImageUrl TEXT,
            
            -- Multi-tenant support
            tenantId INTEGER NOT NULL REFERENCES "Tenant"(id),
            
            -- Timestamps
            createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            
            -- Contraintes
            CONSTRAINT "ShipmentReturns_shipmentId_key" UNIQUE (shipmentId)
        );
    """

    # Indexes for ShipmentReturns
    query2 = """
        CREATE INDEX IF NOT EXISTS "ShipmentReturns_shipmentId_idx" ON "ShipmentReturns"("shipmentId");
        CREATE INDEX IF NOT EXISTS "ShipmentReturns_tripShipmentLinkId_idx" ON "ShipmentReturns"("tripShipmentLinkId");
        CREATE INDEX IF NOT EXISTS "ShipmentReturns_tenantId_idx" ON "ShipmentReturns"("tenantId");
    """

    # Table 2: ShipmentReturnDefects
    query3 = """
        CREATE TABLE IF NOT EXISTS "ShipmentReturnDefects" (
            id SERIAL PRIMARY KEY,
            shipmentReturnId INTEGER NOT NULL REFERENCES "ShipmentReturns"(id) ON DELETE CASCADE,
            
            -- Relation vers Item (integrité référentielle)
            itemId INTEGER NOT NULL REFERENCES "Item"(id),
            
            -- Détails du défaut
            quantity INTEGER NOT NULL,
            reason TEXT NOT NULL,
            
            -- Timestamp
            createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );
    """

    # Indexes for ShipmentReturnDefects
    query4 = """
        CREATE INDEX IF NOT EXISTS "ShipmentReturnDefects_shipmentReturnId_idx" ON "ShipmentReturnDefects"("shipmentReturnId");
        CREATE INDEX IF NOT EXISTS "ShipmentReturnDefects_itemId_idx" ON "ShipmentReturnDefects"("itemId");
    """

    headers = {
        "Content-Type": "application/json",
        "neon-connection-string": NEON_CONNECTION_STRING
    }

    print("📋 Création de la table ShipmentReturns...")
    print("="*80)
    
    # Execute query 1
    try:
        payload = {"query": query1}
        response = requests.post(NEON_SQL_ENDPOINT, json=payload, headers=headers, timeout=30)
        response.raise_for_status()
        result = response.json()
        print("✅ Table ShipmentReturns créée")
        print(f"   Command: {result.get('command')}")
    except Exception as e:
        print(f"❌ Erreur création ShipmentReturns: {e}")
        return

    print("\n📋 Création des indexes pour ShipmentReturns...")
    
    # Execute query 2
    try:
        payload = {"query": query2}
        response = requests.post(NEON_SQL_ENDPOINT, json=payload, headers=headers, timeout=30)
        response.raise_for_status()
        print("✅ Indexes ShipmentReturns créés")
    except Exception as e:
        print(f"❌ Erreur création indexes ShipmentReturns: {e}")

    print("\n📋 Création de la table ShipmentReturnDefects...")
    print("="*80)
    
    # Execute query 3
    try:
        payload = {"query": query3}
        response = requests.post(NEON_SQL_ENDPOINT, json=payload, headers=headers, timeout=30)
        response.raise_for_status()
        result = response.json()
        print("✅ Table ShipmentReturnDefects créée")
        print(f"   Command: {result.get('command')}")
    except Exception as e:
        print(f"❌ Erreur création ShipmentReturnDefects: {e}")
        return

    print("\n📋 Création des indexes pour ShipmentReturnDefects...")
    
    # Execute query 4
    try:
        payload = {"query": query4}
        response = requests.post(NEON_SQL_ENDPOINT, json=payload, headers=headers, timeout=30)
        response.raise_for_status()
        print("✅ Indexes ShipmentReturnDefects créés")
    except Exception as e:
        print(f"❌ Erreur création indexes ShipmentReturnDefects: {e}")

    print("\n" + "="*80)
    print("🎉 Tables ShipmentReturns et ShipmentReturnDefects créées avec succès !")

if __name__ == "__main__":
    create_returns_tables()
