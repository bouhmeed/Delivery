-- Mettre à jour originId pour les shipments du Trip 102 (28 avril 2026)
-- Changer de Entrepôt Lyon (ID: 1) vers Zurich - Dépôt Suisse (ID: 8)

UPDATE "Shipment"
SET "originId" = 8, "updatedAt" = CURRENT_TIMESTAMP
WHERE id IN (108, 109, 110, 111);
