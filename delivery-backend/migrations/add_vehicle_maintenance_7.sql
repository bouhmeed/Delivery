-- Ajouter un enregistrement de maintenance pour le véhicule 2
-- Brake Inspection avec les dates spécifiées

INSERT INTO "VehicleMaintenance" (
    "vehicleId",
    "type",
    "date",
    "nextMaintenance",
    "estimatedCost",
    "notes",
    "technician",
    "status"
) VALUES (
    2,                              -- vehicleId
    'Brake Inspection',             -- type
    '2026-04-18 19:00:00.000',     -- date
    '2026-05-28 19:00:00.000',     -- nextMaintenance
    120.0,                          -- estimatedCost
    'Front brake pads check and replacement if needed',  -- notes
    'Marie Martin',                 -- technician
    'open'                          -- status
);

-- Vérifier que l'enregistrement a été créé
SELECT * FROM "VehicleMaintenance" WHERE id = 7;
