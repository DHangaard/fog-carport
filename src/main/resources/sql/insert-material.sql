BEGIN;

-- ============================================
-- MATERIALS
-- ============================================

-- Stolper (Posts)
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('trykimp.  Stolpe', 'WOOD_AND_ROOFING', 'POST', 97, 97, 'stk', 'Stolper nedgraves 90 cm.  i jord');

-- Remme (Beams)
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('spærtræ ubh. ', 'WOOD_AND_ROOFING', 'BEAM', 45, 195, 'stk', 'Remme i sider, sadles ned i stolper');

-- Spær (Rafters)
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('spærtræ ubh.', 'WOOD_AND_ROOFING', 'RAFTER', 45, 195, 'stk', 'Spær, monteres på rem');

-- Tagplader (Roof)
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('Plastmo Ecolite blåtonet', 'WOOD_AND_ROOFING', 'ROOF', 109, 5, 'stk', 'Tagplader monteres på spær');

-- Skruer (Fasteners)
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('Plastmo Bundskruer', 'FITTINGS_AND_FASTENERS', 'FASTENER', NULL, NULL, 'pakke', 'Skruer til tagplader');

-- Beslag (Fittings)
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
                                                                                              ('Universal højre', 'FITTINGS_AND_FASTENERS', 'FITTING', NULL, NULL, 'stk', 'Beslag til montering'),
                                                                                              ('Universal venstre', 'FITTINGS_AND_FASTENERS', 'FITTING', NULL, NULL, 'stk', 'Beslag til montering');

-- Hulbånd (Metal Strap)
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('hulbånd', 'FITTINGS_AND_FASTENERS', 'METAL_STRAP', 20, 1, 'Rulle', 'Til vindkryds på spær');

-- Beslagskruer
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('Beslagskruer', 'FITTINGS_AND_FASTENERS', 'FASTENER', null, null, 'Pakke', 'Til	montering	af	universalbeslag	+	hulbånd	');

-- Bræddebolt
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('bræddebolt', 'FITTINGS_AND_FASTENERS', 'FASTENER', 10, 120, 'Stk', 'Til montering af rem på stolper');

-- Firkantskiver
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('firkantskiver', 'FITTINGS_AND_FASTENERS', 'WASHER', 40, 40, 'Stk', 'Til montering af rem på stolper');

-- ============================================
-- MATERIAL VARIANTS
-- ============================================

-- Stolpe 97x97 (300 cm)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'trykimp.  Stolpe' AND material_width = 97), 300, 221.85, 1);

-- Spærtræ 45x195 BEAM (300-720 cm)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 300, 158.85, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 360, 190.61, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 420, 222.39, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 480, 254.15, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 540, 285.93, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 600, 479.70, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 660, 527.66, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 720, 575.64, 1);

-- Spærtræ 45x195 RAFTER (300-720 cm)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 300, 158.85, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 360, 190.61, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 420, 222.39, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 480, 254.15, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 540, 285.93, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 600, 479.70, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 660, 527.66, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 720, 575.64, 1);

-- Plastmo Ecolite tagplader (240-600 cm)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 240, 139.00, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 300, 179.00, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 360, 199.00, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 420, 239.00, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 480, 269.00, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 600, 339.00, 1);

-- Plastmo Bundskruer (200 stk pr. pakke)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'Plastmo Bundskruer'), NULL, 149.00, 200);

-- Universal beslag
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
                                                                                            ((SELECT material_id FROM material WHERE name = 'Universal højre'), NULL, 9.45, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Universal venstre'), NULL, 9.45, 1);

-- Hulbånd
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'hulbånd'), 1000, 239.75, 1);

-- Beslagskruer (250 stk pr.  pakke)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'Beslagskruer'), 5, 189.75, 250);

-- Bræddebolt
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'bræddebolt'), 12, 29.33, 1);

-- Firkantskiver
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'firkantskiver'), 1, 15.71, 1);

COMMIT;

-- ============================================
-- VERIFICATION
-- ============================================
SELECT
    m.name,
    m.type,
    m. category,
    COUNT(mv.material_variant_id) as variants,
    MIN(mv.unit_price) as min_price,
    MAX(mv.unit_price) as max_price
FROM material m
         LEFT JOIN material_variant mv ON m.material_id = mv. material_id
GROUP BY m.name, m.type, m.category
ORDER BY m.category, m.type;