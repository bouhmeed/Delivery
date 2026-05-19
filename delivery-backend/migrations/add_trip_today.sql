-- Ajouter un Trip pour aujourd'hui (18 mai 2026)
-- Ce script crée un nouveau trajet avec les données de test existantes

INSERT INTO "Trip" (
    "tripDate",
    "depotId",
    "driverId",
    "vehicleId",
    "status",
    "tenantId",
    "tripId"
) VALUES (
    '2026-05-18 08:00:00',  -- Date du trajet aujourd'hui à 8h
    5,                        -- depotId (dépôt existant)
    5,                        -- driverId (chauffeur existant)
    2,                        -- vehicleId (véhicule existant)
    'PLANNING',               -- status: PLANNING (par défaut)
    1,                        -- tenantId (tenant par défaut)
    'TRIP-2026-0518-001'      -- tripId unique
);

-- Vérifier que le trip a été créé
SELECT * FROM "Trip" WHERE "tripId" = 'TRIP-2026-0518-001';
