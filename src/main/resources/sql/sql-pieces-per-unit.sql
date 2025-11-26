ALTER TABLE material_variant
    ADD COLUMN pieces_per_unit integer;

UPDATE material_variant
SET pieces_per_unit = 200
WHERE material_id = (SELECT material_id FROM material WHERE name = 'Plastmo Bundskruer');

UPDATE material_variant
SET pieces_per_unit = 1
WHERE pieces_per_unit IS NULL;