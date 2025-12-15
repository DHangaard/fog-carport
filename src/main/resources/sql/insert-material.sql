BEGIN;


-- MATERIALS

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



-- MATERIAL VARIANTS (unit_price without VAT)

-- Stolpe 97x97 (300 cm)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'trykimp.  Stolpe' AND material_width = 97), 300, 177.48, 1);

-- Spærtræ 45x195 BEAM (300-720 cm)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 300, 127.08, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 360, 152.49, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 420, 177.91, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 480, 203.32, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 540, 228.74, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 600, 383.76, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 660, 422.13, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'BEAM' AND material_height = 195), 720, 460.51, 1);

-- Spærtræ 45x195 RAFTER (300-720 cm)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 300, 127.08, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 360, 152.49, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 420, 177.91, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 480, 203.32, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 540, 228.74, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 600, 383.76, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 660, 422.13, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'RAFTER' AND material_height = 195), 720, 460.51, 1);

-- Plastmo Ecolite tagplader (240-600 cm)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 240, 111.20, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 300, 143.20, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 360, 159.20, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 420, 191.20, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 480, 215.20, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Plastmo Ecolite blåtonet'), 600, 271.20, 1);

-- Plastmo Bundskruer (200 stk pr. pakke)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'Plastmo Bundskruer'), NULL, 343.20, 200);

-- Universal beslag
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
                                                                                            ((SELECT material_id FROM material WHERE name = 'Universal højre'), NULL, 43.96, 1),
                                                                                            ((SELECT material_id FROM material WHERE name = 'Universal venstre'), NULL, 43.96, 1);

-- Hulbånd
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'hulbånd'), 1000, 239.75, 1);

-- Beslagskruer (250 stk pr.  pakke)
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'Beslagskruer'), 5, 189.75, 250);

-- Bræddebolt
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'bræddebolt'), 12, 23.46, 1);

-- Firkantskiver
INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
    ((SELECT material_id FROM material WHERE name = 'firkantskiver'), 1, 12.57, 1);

-- Understernbrædder (Under fascia boards) - 25x200 mm
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('trykimp. Brædt', 'WOOD_AND_ROOFING', 'UNDER_FASCIA_BOARD', 25, 200, 'stk', 'Understernbrædder til for & bag ende og sider');

-- Oversternbrædder (Over fascia boards) - 25x125 mm
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('trykimp. Brædt', 'WOOD_AND_ROOFING', 'OVER_FASCIA_BOARD', 25, 125, 'stk', 'Oversternbrædder til forenden og sider');

-- Vandbrædder (Water boards) - 19x100 mm
INSERT INTO material (name, category, type, material_width, material_height, unit, usage) VALUES
    ('trykimp. Brædt', 'WOOD_AND_ROOFING', 'WATER_BOARD', 19, 100, 'stk', 'Vandbrædt på stern i forende og sider');

INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
                                                                                            ((SELECT material_id FROM material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 300, 117.48, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 360, 140.80, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 420, 164.47, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 480, 187.96, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 540, 211.46, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 600, 268.56, 1);

INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
                                                                                            ((SELECT material_id FROM material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 300, 83.88, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 360, 97.49, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 420, 117.43, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 480, 134.20, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 540, 150.93, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 600, 128.56, 1);

INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES
                                                                                            ((SELECT material_id FROM material WHERE type = 'WATER_BOARD' AND material_width = 19 AND material_height = 100), 300, 43.08, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'WATER_BOARD' AND material_width = 19 AND material_height = 100), 360, 51.68, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'WATER_BOARD' AND material_width = 19 AND material_height = 100), 420, 60.29, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'WATER_BOARD' AND material_width = 19 AND material_height = 100), 480, 68.92, 1),
                                                                                            ((SELECT material_id FROM material WHERE type = 'WATER_BOARD' AND material_width = 19 AND material_height = 100), 540, 77.54, 1);

COMMIT;



-- VERIFICATION

SELECT
    m.name,
    m.type,
    m.category,
    COUNT(mv.material_variant_id) as variants,
    MIN(mv.unit_price) as min_price,
    MAX(mv.unit_price) as max_price
FROM material m
         LEFT JOIN material_variant mv ON m.material_id = mv.material_id
GROUP BY m.name, m.type, m.category
ORDER BY m.category, m.type;