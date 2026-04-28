-- Corriger le Trip 102 (28 avril) pour utiliser le bon driver et vehicle
UPDATE "Trip"
SET "driverId" = 5, "vehicleId" = 2
WHERE id = 102;
