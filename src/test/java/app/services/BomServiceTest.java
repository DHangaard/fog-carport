package app.services;

import app.entities.*;
import app.enums.MaterialType;
import app.enums.RoofType;
import app.enums.ShedPlacement;
import app.exceptions.DatabaseException;
import app.exceptions.MaterialNotFoundException;
import app.persistence.ConnectionPool;
import app.persistence.MaterialVariantMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BomServiceTest
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "carport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static MaterialVariantMapper variantMapper;
    private static IBomService bomService;

    @BeforeAll
    static void setupClass()
    {
        try (Connection connection = connectionPool.getConnection();
             Statement stmt = connection.createStatement())
        {
            stmt.execute("DROP TABLE IF EXISTS test.material_variant CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.material CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS test.material_variant_material_variant_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS test.material_material_id_seq CASCADE");

            stmt.execute("CREATE TABLE test.material AS (SELECT * FROM public.material) WITH NO DATA");
            stmt.execute("CREATE SEQUENCE test.material_material_id_seq");
            stmt.execute("ALTER TABLE test.material ALTER COLUMN material_id SET DEFAULT nextval('test.material_material_id_seq')");
            stmt.execute("ALTER TABLE test.material ADD PRIMARY KEY (material_id)");

            stmt.execute("CREATE TABLE test.material_variant AS (SELECT * FROM public.material_variant) WITH NO DATA");
            stmt.execute("CREATE SEQUENCE test.material_variant_material_variant_id_seq");
            stmt.execute("ALTER TABLE test.material_variant ALTER COLUMN material_variant_id SET DEFAULT nextval('test.material_variant_material_variant_id_seq')");
            stmt.execute("ALTER TABLE test.material_variant ADD PRIMARY KEY (material_variant_id)");
            stmt.execute("ALTER TABLE test.material_variant ADD CONSTRAINT material_variant_material_fk FOREIGN KEY (material_id) REFERENCES test.material (material_id) ON DELETE CASCADE");

            stmt.execute("INSERT INTO test.material (name, category, type, material_width, material_height, unit, usage) VALUES " +
                    "('trykimp.  Stolpe', 'WOOD_AND_ROOFING', 'POST', 97, 97, 'stk', 'Stolper nedgraves 90 cm.  i jord')");

            stmt.execute("INSERT INTO test.material (name, category, type, material_width, material_height, unit, usage) VALUES " +
                    "('spærtræ ubh. ', 'WOOD_AND_ROOFING', 'BEAM', 45, 195, 'stk', 'Remme i sider, sadles ned i stolper')");

            stmt.execute("INSERT INTO test.material (name, category, type, material_width, material_height, unit, usage) VALUES " +
                    "('spærtræ ubh.', 'WOOD_AND_ROOFING', 'RAFTER', 45, 195, 'stk', 'Spær, monteres på rem')");

            stmt.execute("INSERT INTO test.material (name, category, type, material_width, material_height, unit, usage) VALUES " +
                    "('Plastmo Ecolite blåtonet', 'WOOD_AND_ROOFING', 'ROOF', 109, 5, 'stk', 'Tagplader monteres på spær')");

            stmt.execute("INSERT INTO test.material (name, category, type, material_width, material_height, unit, usage) VALUES " +
                    "('Plastmo Bundskruer', 'FITTINGS_AND_FASTENERS', 'FASTENER', NULL, NULL, 'pakke', 'Skruer til tagplader'), " +
                    "('Beslagskruer', 'FITTINGS_AND_FASTENERS', 'FASTENER', null, null, 'Pakke', 'Til montering af universalbeslag + hulbånd'), " +
                    "('bræddebolt', 'FITTINGS_AND_FASTENERS', 'FASTENER', 10, 120, 'Stk', 'Til montering af rem på stolper')");

            stmt.execute("INSERT INTO test.material (name, category, type, material_width, material_height, unit, usage) VALUES " +
                    "('Universal højre', 'FITTINGS_AND_FASTENERS', 'FITTING', NULL, NULL, 'stk', 'Beslag til montering'), " +
                    "('Universal venstre', 'FITTINGS_AND_FASTENERS', 'FITTING', NULL, NULL, 'stk', 'Beslag til montering')");

            stmt.execute("INSERT INTO test.material (name, category, type, material_width, material_height, unit, usage) VALUES " +
                    "('hulbånd', 'FITTINGS_AND_FASTENERS', 'METAL_STRAP', 20, 1, 'Rulle', 'Til vindkryds på spær')");

            stmt.execute("INSERT INTO test.material (name, category, type, material_width, material_height, unit, usage) VALUES " +
                    "('firkantskiver', 'FITTINGS_AND_FASTENERS', 'WASHER', 40, 40, 'Stk', 'Til montering af rem på stolper')");

            stmt.execute("INSERT INTO test.material (name, category, type, material_width, material_height, unit, usage) VALUES " +
                    "('trykimp. Brædt', 'WOOD_AND_ROOFING', 'UNDER_FASCIA_BOARD', 25, 200, 'stk', 'Understernbrædder til for & bag ende og sider'), " +
                    "('trykimp. Brædt', 'WOOD_AND_ROOFING', 'OVER_FASCIA_BOARD', 25, 125, 'stk', 'Oversternbrædder til forenden og sider'), " +
                    "('trykimp. Brædt', 'WOOD_AND_ROOFING', 'WATER_BOARD', 19, 100, 'stk', 'Vandbrædt på stern i forende og sider')");

            stmt.execute("SELECT setval('test.material_material_id_seq', COALESCE((SELECT MAX(material_id) + 1 FROM test.material), 1), false)");

        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }

        variantMapper = new MaterialVariantMapper(connectionPool);
        bomService = new BomService(variantMapper);
    }

    @BeforeEach
    void setUp()
    {
        try (Connection connection = connectionPool.getConnection();
             Statement stmt = connection.createStatement())
        {
            stmt.execute("DELETE FROM test.material_variant");

            stmt.execute("INSERT INTO test.material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                    "((SELECT material_id FROM test. material WHERE name = 'trykimp.  Stolpe' AND material_width = 97), 300, 177.48, 1)");

            stmt.execute("INSERT INTO test.material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                    "((SELECT material_id FROM test.material WHERE type = 'BEAM' AND material_height = 195), 300, 127.08, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'BEAM' AND material_height = 195), 360, 152.49, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'BEAM' AND material_height = 195), 420, 177.91, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'BEAM' AND material_height = 195), 480, 203.32, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'BEAM' AND material_height = 195), 540, 228.74, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'BEAM' AND material_height = 195), 600, 383.76, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'BEAM' AND material_height = 195), 660, 422.13, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'BEAM' AND material_height = 195), 720, 460.51, 1)");

            stmt.execute("INSERT INTO test.material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                    "((SELECT material_id FROM test.material WHERE type = 'RAFTER' AND material_height = 195), 300, 127.08, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'RAFTER' AND material_height = 195), 360, 152.49, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'RAFTER' AND material_height = 195), 420, 177.91, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'RAFTER' AND material_height = 195), 480, 203.32, 1), " +
                    "((SELECT material_id FROM test. material WHERE type = 'RAFTER' AND material_height = 195), 540, 228.74, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'RAFTER' AND material_height = 195), 600, 383.76, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'RAFTER' AND material_height = 195), 660, 422.13, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'RAFTER' AND material_height = 195), 720, 460.51, 1)");

            stmt.execute("INSERT INTO test.material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                    "((SELECT material_id FROM test.material WHERE name = 'Plastmo Ecolite blåtonet'), 240, 111.20, 1), " +
                    "((SELECT material_id FROM test.material WHERE name = 'Plastmo Ecolite blåtonet'), 300, 143.20, 1), " +
                    "((SELECT material_id FROM test.material WHERE name = 'Plastmo Ecolite blåtonet'), 360, 159.20, 1), " +
                    "((SELECT material_id FROM test.material WHERE name = 'Plastmo Ecolite blåtonet'), 420, 191.20, 1), " +
                    "((SELECT material_id FROM test.material WHERE name = 'Plastmo Ecolite blåtonet'), 480, 215.20, 1), " +
                    "((SELECT material_id FROM test.material WHERE name = 'Plastmo Ecolite blåtonet'), 600, 271.20, 1)");

            stmt.execute("INSERT INTO test.material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                    "((SELECT material_id FROM test.material WHERE name = 'Plastmo Bundskruer'), NULL, 343.20, 200), " +
                    "((SELECT material_id FROM test.material WHERE name = 'Beslagskruer'), 5, 189.75, 250), " +
                    "((SELECT material_id FROM test.material WHERE name = 'bræddebolt'), 12, 23.46, 1)");

            stmt.execute("INSERT INTO test.material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                    "((SELECT material_id FROM test.material WHERE name = 'Universal højre'), NULL, 43.96, 1), " +
                    "((SELECT material_id FROM test.material WHERE name = 'Universal venstre'), NULL, 43.96, 1)");

            stmt.execute("INSERT INTO test.material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                    "((SELECT material_id FROM test. material WHERE name = 'hulbånd'), 1000, 239.75, 1)");

            stmt.execute("INSERT INTO test.material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                    "((SELECT material_id FROM test.material WHERE name = 'firkantskiver'), 1, 12.57, 1)");

            stmt.execute("INSERT INTO test.material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                    "((SELECT material_id FROM test.material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 300, 117.48, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 360, 140.80, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 420, 164.47, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 480, 187.96, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 540, 211.46, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'UNDER_FASCIA_BOARD' AND material_width = 25 AND material_height = 200), 600, 268.56, 1)");

            stmt.execute("INSERT INTO test.material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                    "((SELECT material_id FROM test.material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 300, 83.88, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 360, 97.49, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 420, 117.43, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 480, 134.20, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 540, 150.93, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'OVER_FASCIA_BOARD' AND material_width = 25 AND material_height = 125), 600, 128.56, 1)");

            stmt.execute("INSERT INTO test.material_variant (material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                    "((SELECT material_id FROM test. material WHERE type = 'WATER_BOARD' AND material_width = 19 AND material_height = 100), 300, 43.08, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'WATER_BOARD' AND material_width = 19 AND material_height = 100), 360, 51.68, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'WATER_BOARD' AND material_width = 19 AND material_height = 100), 420, 60.29, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'WATER_BOARD' AND material_width = 19 AND material_height = 100), 480, 68.92, 1), " +
                    "((SELECT material_id FROM test.material WHERE type = 'WATER_BOARD' AND material_width = 19 AND material_height = 100), 540, 77.54, 1)");

            stmt.execute("SELECT setval('test.material_variant_material_variant_id_seq', COALESCE((SELECT MAX(material_variant_id) + 1 FROM test.material_variant), 1), false)");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @Test
    void testPricingCalculator() throws DatabaseException
    {
        List<MaterialVariant> postVariants = variantMapper.getAllVariantsByType(MaterialType.POST);
        List<MaterialVariant> rafterVariants = variantMapper.getAllVariantsByType(MaterialType.RAFTER);

        List<MaterialLine> materialLines = new ArrayList<>();
        materialLines.add(new MaterialLine(postVariants.get(0),4)); // Price 177.48 x 4
        materialLines.add(new MaterialLine(rafterVariants.get(0),4)); // Price 127.08 x 4

        double expectedCostPrice = 1218.24;
        double coveragePercentage = 40.0;
        double expectedPriceWithCoveragePercentage = expectedCostPrice  / (1 - coveragePercentage / 100.0);

        PricingDetails pricingDetails = bomService.calculateCarportPrice(materialLines);

        assertNotNull(pricingDetails);
        assertEquals(expectedCostPrice, pricingDetails.getCostPrice());
        assertTrue(coveragePercentage == pricingDetails.getCoveragePercentage());
        assertEquals(expectedPriceWithCoveragePercentage, pricingDetails.getPriceWithoutVat());

    }

    @Test
    void test240CmWidthCarportGetsCorrectRafters() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 240, 240, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.RAFTER)
                .collect(Collectors.toList());

        int expectedLength = 300;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 1);
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test330CmWidthCarportGetsCorrectRafters() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 500, 330, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.RAFTER)
                .collect(Collectors.toList());

        int expectedLength = 360;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 1);
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }


    @Test
    void test600CmWidthCarportGetsCorrectRafters() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 600, 600, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.RAFTER)
                .collect(Collectors.toList());

        int expectedLength = 600;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 1);
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test240CmCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 240, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int expectedLength = 300;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 1);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test360CmCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 360, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int expectedLength = 360;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 1 );
        assertTrue(beamLines.get(0).getMaterialVariant().getVariantLength() >= 360);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test600CmCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 600, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int expectedLength = 600;

        assertTrue(beamLines.size() == 1);
        assertNotNull(beamLines);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test780CmCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 780, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int firstBeam = 420;
        int secondBeam = 420;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 2);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(2, beamLines.get(1).getQuantity());
        assertEquals(firstBeam, beamLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondBeam, beamLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test750CmWithShedCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 750, 420, RoofType.TRAPEZ_ROOF, new Shed(0,200,200, ShedPlacement.FULL_WIDTH));

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int firstBeam = 420;
        int secondBeam = 720;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 2);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(1, beamLines.get(1).getQuantity());
        assertEquals(firstBeam, beamLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondBeam, beamLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test780CmWithShedCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 780, 420, RoofType.TRAPEZ_ROOF, new Shed(0,400,200, ShedPlacement.FULL_WIDTH));

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int firstBeam = 480;
        int secondBeam = 660;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 2);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(1, beamLines.get(1).getQuantity());
        assertEquals(firstBeam, beamLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondBeam, beamLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test240CmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 240, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int roofLengthExpected = 300;
        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 1);
        assertEquals(roofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test360CmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 360, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int roofLengthExpected = 420;
        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 1);
        assertEquals(roofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test300CmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 300, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int roofLengthExpected = 360;
        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 1);
        assertEquals(roofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test560CmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 570, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int roofLengthExpected = 600;
        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 1);
        assertEquals(roofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
    }


    @Test
    void test600cmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 600, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int firstRoofLengthExpected = 300;
        int secondRoofLengthExpected = 360;

        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 2);
        assertEquals(firstRoofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondRoofLengthExpected, roofLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test630cmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 630, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int firstRoofLengthExpected = 300;
        int secondRoofLengthExpected = 420;

        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 2);
        assertEquals(firstRoofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondRoofLengthExpected, roofLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test660cmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 660, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int firstRoofLengthExpected = 300;
        int secondRoofLengthExpected = 420;

        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 2);
        assertEquals(firstRoofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondRoofLengthExpected, roofLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test690cmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 690, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int firstRoofLengthExpected = 300;
        int secondRoofLengthExpected = 480;

        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 2);
        assertEquals(firstRoofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondRoofLengthExpected, roofLines.get(1).getMaterialVariant().getVariantLength());
    }


    @Test
    void test780CmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 780, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int firstRoofLengthExpected = 300;
        int secondRoofLengthExpected = 600;

        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 2);
        assertEquals(firstRoofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondRoofLengthExpected, roofLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test420x240CarportGetsCorrectUnderFasciaBoards() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 420, 240, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> underFasciaBoardLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.UNDER_FASCIA_BOARD)
                .collect(Collectors.toList());

        int expectedWidthLength = 300;
        int expectedLengthLength = 480;

        assertNotNull(underFasciaBoardLines);
        assertEquals(2, underFasciaBoardLines.size());

        assertEquals(2, underFasciaBoardLines.get(0).getQuantity());
        assertEquals(expectedWidthLength, underFasciaBoardLines.get(0).getMaterialVariant().getVariantLength());

        assertEquals(2, underFasciaBoardLines.get(1).getQuantity());
        assertEquals(expectedLengthLength, underFasciaBoardLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test600x420CarportGetsCorrectUnderFasciaBoards() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 600, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> underFasciaBoardLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.UNDER_FASCIA_BOARD)
                .collect(Collectors.toList());

        int expectedWidthLength = 480;
        int expectedLengthLength = 360;

        assertNotNull(underFasciaBoardLines);
        assertEquals(2, underFasciaBoardLines.size());

        assertEquals(2, underFasciaBoardLines.get(0).getQuantity());
        assertEquals(expectedWidthLength, underFasciaBoardLines.get(0).getMaterialVariant().getVariantLength());

        assertEquals(4, underFasciaBoardLines.get(1).getQuantity());
        assertEquals(expectedLengthLength, underFasciaBoardLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test780x600CarportGetsCorrectUnderFasciaBoards() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 780, 600, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> underFasciaBoardLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.UNDER_FASCIA_BOARD)
                .collect(Collectors.toList());

        int expectedWidthLength = 360;
        int expectedLengthLength = 420;

        assertNotNull(underFasciaBoardLines);
        assertEquals(2, underFasciaBoardLines.size());

        assertEquals(4, underFasciaBoardLines.get(0).getQuantity());
        assertEquals(expectedWidthLength, underFasciaBoardLines.get(0).getMaterialVariant().getVariantLength());

        assertEquals(4, underFasciaBoardLines.get(1).getQuantity());
        assertEquals(expectedLengthLength, underFasciaBoardLines.get(1).getMaterialVariant().getVariantLength());
    }


    @Test
    void test420x240CarportGetsCorrectOverFasciaBoards() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 420, 240, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> overFasciaBoardLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.OVER_FASCIA_BOARD)
                .collect(Collectors.toList());

        int expectedWidthLength = 300;
        int expectedLengthLength = 480;

        assertNotNull(overFasciaBoardLines);
        assertEquals(2, overFasciaBoardLines.size());

        assertEquals(1, overFasciaBoardLines.get(0).getQuantity());
        assertEquals(expectedWidthLength, overFasciaBoardLines.get(0).getMaterialVariant().getVariantLength());

        assertEquals(2, overFasciaBoardLines.get(1).getQuantity());
        assertEquals(expectedLengthLength, overFasciaBoardLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test600x420CarportGetsCorrectOverFasciaBoards() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 600, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> overFasciaBoardLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.OVER_FASCIA_BOARD)
                .collect(Collectors.toList());

        int expectedWidthLength = 480;
        int expectedLengthLength = 360;

        assertNotNull(overFasciaBoardLines);
        assertEquals(2, overFasciaBoardLines.size());

        assertEquals(1, overFasciaBoardLines.get(0).getQuantity());
        assertEquals(expectedWidthLength, overFasciaBoardLines.get(0).getMaterialVariant().getVariantLength());

        assertEquals(4, overFasciaBoardLines.get(1).getQuantity());
        assertEquals(expectedLengthLength, overFasciaBoardLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test780x600CarportGetsCorrectOverFasciaBoards() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 780, 600, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> overFasciaBoardLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.OVER_FASCIA_BOARD)
                .collect(Collectors.toList());

        int expectedWidthLength = 360;
        int expectedLengthLength = 420;

        assertNotNull(overFasciaBoardLines);
        assertEquals(2, overFasciaBoardLines.size());

        assertEquals(2, overFasciaBoardLines.get(0).getQuantity());
        assertEquals(expectedWidthLength, overFasciaBoardLines.get(0).getMaterialVariant().getVariantLength());

        assertEquals(4, overFasciaBoardLines.get(1).getQuantity());
        assertEquals(expectedLengthLength, overFasciaBoardLines.get(1).getMaterialVariant().getVariantLength());
    }


    @Test
    void test420x240CarportGetsCorrectWaterBoards() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 420, 240, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> waterBoardLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.WATER_BOARD)
                .collect(Collectors.toList());

        int expectedWidthLength = 300;
        int expectedLengthLength = 480;

        assertNotNull(waterBoardLines);
        assertEquals(2, waterBoardLines.size());

        assertEquals(1, waterBoardLines.get(0).getQuantity());
        assertEquals(expectedWidthLength, waterBoardLines.get(0).getMaterialVariant().getVariantLength());

        assertEquals(2, waterBoardLines.get(1).getQuantity());
        assertEquals(expectedLengthLength, waterBoardLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test540x420CarportGetsCorrectWaterBoards() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 540, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> waterBoardLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.WATER_BOARD)
                .collect(Collectors.toList());

        int expectedWidthLength = 480;
        int expectedLengthLength = 300;

        assertNotNull(waterBoardLines);
        assertEquals(2, waterBoardLines.size());

        assertEquals(1, waterBoardLines.get(0).getQuantity());
        assertEquals(expectedWidthLength, waterBoardLines.get(0).getMaterialVariant().getVariantLength());

        assertEquals(4, waterBoardLines.get(1).getQuantity());
        assertEquals(expectedLengthLength, waterBoardLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test780x600CarportGetsCorrectWaterBoards() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 780, 600, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> waterBoardLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.WATER_BOARD)
                .collect(Collectors.toList());

        int expectedWidthLength = 360;
        int expectedLengthLength = 420;

        assertNotNull(waterBoardLines);
        assertEquals(2, waterBoardLines.size());

        assertEquals(2, waterBoardLines.get(0).getQuantity());
        assertEquals(expectedWidthLength, waterBoardLines.get(0).getMaterialVariant().getVariantLength());

        assertEquals(4, waterBoardLines.get(1).getQuantity());
        assertEquals(expectedLengthLength, waterBoardLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void testCarportGetsMetalStrap() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 600, 600, RoofType.FLAT, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> strapLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.METAL_STRAP)
                .collect(Collectors.toList());

        assertNotNull(strapLines);
        assertEquals(1, strapLines.size());
    }

    @Test
    void testCarportGetsCarriageBoltsAndWashers() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 600, 600, RoofType.FLAT, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> boltLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getName().equals("bræddebolt"))
                .collect(Collectors.toList());

        List<MaterialLine> washerLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.WASHER)
                .collect(Collectors.toList());

        assertNotNull(boltLines);
        assertNotNull(washerLines);
        assertEquals(1, boltLines.size());
        assertEquals(1, washerLines.size());

        assertEquals(boltLines.get(0).getQuantity(), washerLines.get(0).getQuantity());
    }

    @Test
    void testCarportGetsBracketScrews() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 600, 600, RoofType.FLAT, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> bracketScrewLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getName().equals("Beslagskruer"))
                .collect(Collectors.toList());

        assertNotNull(bracketScrewLines);
        assertEquals(1, bracketScrewLines.size());
    }
}