# Guide d'Insertion de Données de Test

## Vue d'ensemble

Ce guide explique comment ajouter des données de test pour simuler des tournées de livraison dans la base de données PostgreSQL.

## Tables Impliquées

### 1. **Trip** (Tournée)
- **Description**: Représente une tournée de livraison pour un chauffeur à une date donnée
- **Clé primaire**: `id`
- **Colonnes importantes**:
  - `id`: Identifiant unique (ex: 100, 101, 102...)
  - `tripDate`: Date de la tournée (ex: '2026-04-30 08:00:00')
  - `driverId`: ID du chauffeur (ex: 5 pour Pierre Bernard)
  - `vehicleId`: ID du véhicule (ex: 2 pour Peugeot Boxer)
  - `depotId`: ID de l'entrepôt de départ (ex: 1 pour Entrepôt Lyon)
  - `status`: Statut de la tournée (ex: 'PLANNING')
  - `tripId`: Identifiant de tournée (ex: 'TRIP-2026-0430-100')
  - `tenantId`: ID du tenant (ex: 1)

### 2. **Shipment** (Expédition)
- **Description**: Représente une expédition/livraison
- **Clé primaire**: `id`
- **Colonnes importantes**:
  - `id`: Identifiant unique (ex: 100, 101, 102...)
  - `shipmentNo`: Numéro d'expédition (ex: 'EXP-2026-500')
  - `customerId`: ID du client (ex: 9 pour Paris, 10 pour Marseille...)
  - `type`: Type d'expédition (ex: 'OUTBOUND')
  - `originId`: ID du point de départ (ex: 1 pour Entrepôt Lyon)
  - `destinationId`: ID du point de destination (ex: 9 pour Paris)
  - `priority`: Priorité (ex: 'HIGH', 'MEDIUM')
  - `status`: Statut (ex: 'TO_PLAN')
  - `description`: Description de la livraison
  - `quantity`: Quantité (ex: 100)
  - `uom`: Unité de mesure (ex: 'PCS')
  - `weight`: Poids (ex: 250.0)
  - `volume`: Volume (ex: 1.0)
  - `deliveryAddress`: Adresse de livraison
  - `deliveryCity`: Ville de livraison
  - `deliveryZipCode`: Code postal
  - `deliveryCountry`: Pays
  - `tenantId`: ID du tenant (ex: 1)
  - `driverId`: ID du chauffeur (ex: 5)
  - `vehicleId`: ID du véhicule (ex: 2)
  - `estimatedDuration`: Durée estimée en minutes (ex: 60)
  - `distanceKm`: Distance en km (ex: 465)

### 3. **ShipmentLine** (Ligne d'expédition)
- **Description**: Détails des articles dans une expédition
- **Clé primaire**: `id`
- **Colonnes importantes**:
  - `id`: Identifiant unique (ex: 200, 201, 202...)
  - `shipmentId`: ID de l'expédition liée (ex: 100)
  - `itemId`: ID de l'article (ex: 1)
  - `itemCode`: Code de l'article (ex: 'ITEM-001')
  - `description`: Description de l'article
  - `quantity`: Quantité (ex: 100)
  - `uom`: Unité de mesure (ex: 'PCS')
  - `packaging`: Type d'emballage (ex: 'Carton')
  - `weight`: Poids (ex: 250.0)
  - `volume`: Volume (ex: 1.0)
  - `stackable`: Empilable (ex: true)

### 4. **TripShipmentLink** (Liaison Tournée-Expédition)
- **Description**: Lie une expédition à une tournée
- **Clé primaire**: `id`
- **Colonnes importantes**:
  - `id`: Identifiant unique (ex: 100, 101, 102...)
  - `tripId`: ID de la tournée (ex: 100)
  - `shipmentId`: ID de l'expédition (ex: 100)
  - `role`: Rôle (ex: 'BOTH')
  - `status`: Statut (ex: 'NON_DEMARRE')
  - `podDone`: POD effectué (ex: false)
  - `returnsDone`: Retours effectués (ex: false)
  - `sequence`: Ordre de livraison (ex: 1, 2, 3, 4)

## Données Existantes à Utiliser

### Driver (Chauffeur)
- **ID: 5** - Pierre Bernard (DRV-001, FULL_TIME)

### Vehicle (Véhicule)
- **ID: 2** - Peugeot Boxer (VP-002, CAMIONNETTE)

### Location (Emplacements)
- **ID: 1** - Entrepôt Principal Lyon (123 Rue du Dépôt, 69000)
- **ID: 7** - Genève (15 Rue du Mont-Blanc, 1201)
- **ID: 9** - Paris (45 Avenue de la Technologie, 75008)
- **ID: 11** - Lyon (89 Boulevard de l'Éducation, 69003)
- **ID: 12** - Marseille (123 Rue de la Santé, 13001)

### Client (Clients)
- **ID: 5** - Genève (Banque Internationale)
- **ID: 9** - Paris (TechnoPlus France)
- **ID: 10** - Marseille (SantéPlus Médical)
- **ID: 11** - Lyon (EduSchool Services)

### Item (Articles)
- **ID: 1** - ITEM-001 (Produit A - Boîte standard)

## Ordre d'Insertion

**IMPORTANT**: L'ordre est crucial à cause des clés étrangères

1. **Trip** (d'abord - pas de dépendances)
2. **Shipment** (après Trip - utilise driverId, vehicleId)
3. **ShipmentLine** (après Shipment - utilise shipmentId)
4. **TripShipmentLink** (après Trip et Shipment - utilise tripId et shipmentId)

## Exemple Complet - Ajouter une Tournée

### Étape 1: Créer le Trip

```sql
INSERT INTO "Trip" (id, "tripDate", "depotId", "driverId", "vehicleId", status, "tenantId", "createdAt", "tripId")
VALUES (104, '2026-05-01 08:00:00', 1, 5, 2, 'PLANNING', 1, CURRENT_TIMESTAMP, 'TRIP-2026-0501-104');
```

**Explication**:
- `id: 104` - Nouvel ID unique (104 car 100-103 sont déjà utilisés)
- `tripDate: '2026-05-01 08:00:00'` - Date de la tournée
- `depotId: 1` - Entrepôt Lyon
- `driverId: 5` - Pierre Bernard
- `vehicleId: 2` - Peugeot Boxer
- `status: 'PLANNING'` - Statut de planification
- `tenantId: 1` - Tenant par défaut
- `tripId: 'TRIP-2026-0501-104'` - Identifiant lisible

### Étape 2: Créer les Shipments (4 expéditions)

```sql
-- Shipment 1: Paris
INSERT INTO "Shipment" (id, "shipmentNo", "customerId", type, "originId", "destinationId", priority, status, description, quantity, uom, weight, volume, "deliveryAddress", "deliveryCity", "deliveryZipCode", "deliveryCountry", "tenantId", "createdAt", "updatedAt", "driverId", "vehicleId", "estimatedDuration", "distanceKm")
VALUES (116, 'EXP-2026-516', 9, 'OUTBOUND', 1, 9, 'HIGH', 'TO_PLAN', 'Livraison TechnoPlus France Paris', 100, 'PCS', 250.0, 1.0, '45 Avenue de la Technologie', 'Paris', '75008', 'France', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5, 2, 60, 465);

-- Shipment 2: Marseille
INSERT INTO "Shipment" (id, "shipmentNo", "customerId", type, "originId", "destinationId", priority, status, description, quantity, uom, weight, volume, "deliveryAddress", "deliveryCity", "deliveryZipCode", "deliveryCountry", "tenantId", "createdAt", "updatedAt", "driverId", "vehicleId", "estimatedDuration", "distanceKm")
VALUES (117, 'EXP-2026-517', 10, 'OUTBOUND', 1, 12, 'MEDIUM', 'TO_PLAN', 'Livraison SantéPlus Médical Marseille', 150, 'PCS', 300.0, 1.5, '123 Rue de la Santé', 'Marseille', '13001', 'France', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5, 2, 90, 320);

-- Shipment 3: Lyon
INSERT INTO "Shipment" (id, "shipmentNo", "customerId", type, "originId", "destinationId", priority, status, description, quantity, uom, weight, volume, "deliveryAddress", "deliveryCity", "deliveryZipCode", "deliveryCountry", "tenantId", "createdAt", "updatedAt", "driverId", "vehicleId", "estimatedDuration", "distanceKm")
VALUES (118, 'EXP-2026-518', 11, 'OUTBOUND', 1, 11, 'HIGH', 'TO_PLAN', 'Livraison EduSchool Services Lyon', 80, 'PCS', 200.0, 0.8, '89 Boulevard de l''Education', 'Lyon', '69003', 'France', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5, 2, 30, 15);

-- Shipment 4: Genève
INSERT INTO "Shipment" (id, "shipmentNo", "customerId", type, "originId", "destinationId", priority, status, description, quantity, uom, weight, volume, "deliveryAddress", "deliveryCity", "deliveryZipCode", "deliveryCountry", "tenantId", "createdAt", "updatedAt", "driverId", "vehicleId", "estimatedDuration", "distanceKm")
VALUES (119, 'EXP-2026-519', 5, 'OUTBOUND', 1, 7, 'HIGH', 'TO_PLAN', 'Livraison Banque Internationale Genève', 120, 'PCS', 280.0, 1.2, '15 Rue du Mont-Blanc', 'Genève', '1201', 'Suisse', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5, 2, 120, 280);
```

**Explication**:
- `id: 116-119` - IDs uniques (116-119 car 100-115 sont déjà utilisés)
- `shipmentNo: 'EXP-2026-516'` - Numéro d'expédition unique
- `customerId: 9/10/11/5` - IDs des clients existants
- `originId: 1` - Entrepôt Lyon (point de départ)
- `destinationId: 9/12/11/7` - IDs des destinations
- `driverId: 5` - Pierre Bernard
- `vehicleId: 2` - Peugeot Boxer

### Étape 3: Créer les ShipmentLines (4 lignes)

```sql
-- ShipmentLine pour Shipment 116
INSERT INTO "ShipmentLine" (id, "shipmentId", "itemId", "itemCode", description, quantity, uom, packaging, weight, volume, stackable)
VALUES (216, 116, 1, 'ITEM-001', 'Produit A - Boîte standard', 100, 'PCS', 'Carton', 250.0, 1.0, true);

-- ShipmentLine pour Shipment 117
INSERT INTO "ShipmentLine" (id, "shipmentId", "itemId", "itemCode", description, quantity, uom, packaging, weight, volume, stackable)
VALUES (217, 117, 1, 'ITEM-001', 'Produit A - Boîte standard', 150, 'PCS', 'Carton', 300.0, 1.5, true);

-- ShipmentLine pour Shipment 118
INSERT INTO "ShipmentLine" (id, "shipmentId", "itemId", "itemCode", description, quantity, uom, packaging, weight, volume, stackable)
VALUES (218, 118, 1, 'ITEM-001', 'Produit A - Boîte standard', 80, 'PCS', 'Carton', 200.0, 0.8, true);

-- ShipmentLine pour Shipment 119
INSERT INTO "ShipmentLine" (id, "shipmentId", "itemId", "itemCode", description, quantity, uom, packaging, weight, volume, stackable)
VALUES (219, 119, 1, 'ITEM-001', 'Produit A - Boîte standard', 120, 'PCS', 'Carton', 280.0, 1.2, true);
```

**Explication**:
- `id: 216-219` - IDs uniques (216-219 car 200-215 sont déjà utilisés)
- `shipmentId: 116-119` - Correspond aux shipments créés
- `itemId: 1` - Article ITEM-001 existant

### Étape 4: Créer les TripShipmentLinks (4 liaisons)

```sql
-- TripShipmentLink pour Shipment 116 (sequence 1)
INSERT INTO "TripShipmentLink" (id, "tripId", "shipmentId", role, status, "podDone", "returnsDone", sequence, "createdAt", "updatedAt")
VALUES (116, 104, 116, 'BOTH', 'NON_DEMARRE', false, false, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- TripShipmentLink pour Shipment 117 (sequence 2)
INSERT INTO "TripShipmentLink" (id, "tripId", "shipmentId", role, status, "podDone", "returnsDone", sequence, "createdAt", "updatedAt")
VALUES (117, 104, 117, 'BOTH', 'NON_DEMARRE', false, false, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- TripShipmentLink pour Shipment 118 (sequence 3)
INSERT INTO "TripShipmentLink" (id, "tripId", "shipmentId", role, status, "podDone", "returnsDone", sequence, "createdAt", "updatedAt")
VALUES (118, 104, 118, 'BOTH', 'NON_DEMARRE', false, false, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- TripShipmentLink pour Shipment 119 (sequence 4)
INSERT INTO "TripShipmentLink" (id, "tripId", "shipmentId", role, status, "podDone", "returnsDone", sequence, "createdAt", "updatedAt")
VALUES (119, 104, 119, 'BOTH', 'NON_DEMARRE', false, false, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

**Explication**:
- `id: 116-119` - IDs uniques (doivent correspondre aux IDs des shipments)
- `tripId: 104` - ID du trip créé
- `shipmentId: 116-119` - IDs des shipments créés
- `sequence: 1-4` - Ordre de livraison

## Scripts Node.js pour Exécution

### Script d'insertion (insert_trip_new.js)

```javascript
const { Pool } = require('pg');
const fs = require('fs');
require('dotenv').config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: true }
});

(async () => {
    try {
        const sql = fs.readFileSync('./migrations/insert_trip_new.sql', 'utf8');
        await pool.query(sql);
        console.log('✅ Trip inséré avec succès!');
    } catch (err) {
        console.error('❌ Erreur:', err);
    } finally {
        await pool.end();
    }
})();
```

### Script de vérification (check_trip_new.js)

```javascript
const { Pool } = require('pg');
require('dotenv').config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: true }
});

(async () => {
    try {
        const tripRes = await pool.query(`
            SELECT id, "tripDate", "driverId", "vehicleId", "depotId", status, "tripId"
            FROM "Trip"
            WHERE id = 104
        `);
        console.log('TRIP 104:');
        console.table(tripRes.rows);

        const shipmentRes = await pool.query(`
            SELECT id, "shipmentNo", "customerId", status
            FROM "Shipment"
            WHERE id IN (116, 117, 118, 119)
        `);
        console.log('\nSHIPMENTS 116-119:');
        console.table(shipmentRes.rows);

        const linkRes = await pool.query(`
            SELECT id, "tripId", "shipmentId", status, sequence
            FROM "TripShipmentLink"
            WHERE "tripId" = 104
        `);
        console.log('\nTRIP SHIPMENT LINKS pour trip 104:');
        console.table(linkRes.rows);

    } catch (err) {
        console.error('Error:', err);
    } finally {
        await pool.end();
    }
})();
```

## Erreurs Courantes et Solutions

### 1. Erreur: "duplicate key value violates unique constraint"
**Cause**: ID déjà utilisé
**Solution**: Utiliser un ID unique (vérifier les IDs existants)

### 2. Erreur: "column does not exist"
**Cause**: Nom de colonne incorrect (case-sensitive)
**Solution**: Utiliser les guillemets doubles pour les noms de colonnes avec majuscules: `"shipmentNo"`, `"customerId"`, etc.

### 3. Erreur: "foreign key violation"
**Cause**: ID de référence n'existe pas
**Solution**: Vérifier que les IDs de driver, vehicle, client, location existent

### 4. Erreur: "syntax error at or near"
**Cause**: Guillemets simples dans une chaîne
**Solution**: Doubler les guillemets simples: `'89 Boulevard de l''Education'`

## Résumé des IDs Utilisés

### Trips existants
- 100: 30 avril 2026
- 101: 29 avril 2026
- 102: 28 avril 2026
- 103: 27 avril 2026

### Shipments existants
- 100-103: Trip 100
- 104-107: Trip 101
- 108-111: Trip 102
- 112-115: Trip 103

### ShipmentLines existants
- 200-203: Shipments 100-103
- 204-207: Shipments 104-107
- 208-211: Shipments 108-111
- 212-215: Shipments 112-115

### TripShipmentLinks existants
- 100-103: Trip 100
- 104-107: Trip 101
- 108-111: Trip 102
- 112-115: Trip 103

## Prochains IDs à Utiliser

Pour une nouvelle tournée:
- **Trip ID**: 104
- **Shipment IDs**: 116, 117, 118, 119
- **ShipmentLine IDs**: 216, 217, 218, 219
- **TripShipmentLink IDs**: 116, 117, 118, 119

## Structure de la Tournée

```
Trip (104)
├── Driver: Pierre Bernard (ID: 5)
├── Vehicle: Peugeot Boxer (ID: 2)
├── Depot: Entrepôt Lyon (ID: 1)
└── Livraisons (séquence):
    ├── 1. Paris (Shipment 116, Client 9)
    ├── 2. Marseille (Shipment 117, Client 10)
    ├── 3. Lyon (Shipment 118, Client 11)
    └── 4. Genève (Shipment 119, Client 5)
```

## Commandes d'Exécution

```bash
# Exécuter l'insertion
node migrations/insert_trip_new.js

# Vérifier les données
node migrations/check_trip_new.js
```

## Notes Importantes

1. **Toujours utiliser des IDs uniques** - Vérifier les IDs existants avant d'insérer
2. **Respecter l'ordre d'insertion** - Trip → Shipment → ShipmentLine → TripShipmentLink
3. **Utiliser les guillemets doubles** pour les noms de colonnes avec majuscules
4. **Doubler les guillemets simples** dans les chaînes de caractères
5. **Utiliser CURRENT_TIMESTAMP** pour createdAt et updatedAt
6. **Vérifier les clés étrangères** avant l'insertion
7. **La séquence détermine l'ordre** de livraison dans la tournée
