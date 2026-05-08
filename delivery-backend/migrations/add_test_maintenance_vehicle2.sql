-- Add test maintenance data for vehicle 2
-- This will help test the Vehicle Maintenance feature

INSERT INTO "VehicleMaintenance" (
    "vehicleId", 
    "type", 
    "date", 
    "nextMaintenance", 
    "estimatedCost", 
    "notes", 
    "technician", 
    "status"
) VALUES 
-- Urgent maintenance (next maintenance in 5 days)
(
    2, 
    'Oil Change', 
    '2026-05-03 10:00:00', 
    '2026-05-13 10:00:00',  -- 5 days from now (URGENT)
    85.50, 
    'Regular oil change with filter replacement', 
    'Jean Dupont', 
    'completed'
),
-- Warning maintenance (next maintenance in 20 days)
(
    2, 
    'Brake Inspection', 
    '2026-04-15 14:00:00', 
    '2026-05-28 14:00:00',  -- 20 days from now (WARNING)
    120.00, 
    'Front brake pads check and replacement if needed', 
    'Marie Martin', 
    'open'
),
-- Normal maintenance (next maintenance in 45 days)
(
    2, 
    'Tire Rotation', 
    '2026-04-01 09:00:00', 
    '2026-06-22 09:00:00',  -- 45 days from now (NORMAL)
    45.00, 
    'Rotate all four tires and check pressure', 
    'Pierre Durand', 
    'completed'
),
-- Another urgent one
(
    2, 
    'Battery Check', 
    '2026-05-05 11:00:00', 
    '2026-05-12 11:00:00',  -- 4 days from now (URGENT)
    65.00, 
    'Battery voltage test and terminal cleaning', 
    'Sophie Bernard', 
    'open'
),
-- Future maintenance
(
    2, 
    'Air Filter Replacement', 
    '2026-03-20 08:00:00', 
    '2026-06-20 08:00:00',  -- 43 days from now (NORMAL)
    35.00, 
    'Replace engine air filter', 
    'Luc Petit', 
    'completed'
);

-- Verify the data was inserted
SELECT * FROM "VehicleMaintenance" WHERE "vehicleId" = 2 ORDER BY "date" DESC;
