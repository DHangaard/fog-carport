ALTER TABLE material_variant
    ADD COLUMN pieces_per_unit integer;

UPDATE material_variant
SET pieces_per_unit = 200
WHERE material_id = (SELECT material_id FROM material WHERE name = 'Plastmo Bundskruer');

UPDATE material_variant
SET pieces_per_unit = 1
WHERE pieces_per_unit IS NULL;


INSERT INTO material (
    name, category, type, material_width, material_height, unit, usage
) VALUES (
             'hulbånd', 'FITTINGS_AND_FASTENERS', 'METAL_STRAP', 20, 1, 'Rulle', 'Til vindkryds på spær'
         );

INSERT INTO material_variant (
    material_id, variant_length, unit_price, pieces_per_unit
) VALUES (
             (SELECT material_id FROM material WHERE name = 'hulbånd'), 1000, 239.75, 1
         );

INSERT INTO material (
    name, category, type, material_width, material_height, unit, usage
) VALUES (
             'Beslagskruer', 'FITTINGS_AND_FASTENERS', 'FASTENER', null, null, 'Pakke', 'Til	montering	af	universalbeslag	+	hulbånd	'
         );

INSERT INTO material_variant (
    material_id, variant_length, unit_price, pieces_per_unit
) VALUES (
             (SELECT material_id FROM material WHERE name = 'Beslagskruer'), 5, 189.75, 250
         );