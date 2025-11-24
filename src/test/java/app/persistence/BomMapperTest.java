package app.persistence;

import app.entities.BillOfMaterials;
import app.entities.MaterialLine;
import app.entities.PricingDetails;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BomMapperTest
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "carport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static BomMapper bomMapper;

    @BeforeAll
    static void setupClass()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DROP TABLE IF EXISTS test.material_line CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.bill_of_materials CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.offer CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.material_variant CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.material CASCADE");

                stmt.execute("DROP SEQUENCE IF EXISTS test.material_line_material_line_id_seq CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.bill_of_materials_bom_id_seq CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.offer_offer_id_seq CASCADE");
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
                stmt.execute("ALTER TABLE test.material_variant ADD CONSTRAINT material_variant_material_fk " +
                        "FOREIGN KEY (material_id) REFERENCES test.material (material_id)");

                stmt.execute("CREATE TABLE test.offer AS (SELECT * FROM public.offer) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.offer_offer_id_seq");
                stmt.execute("ALTER TABLE test.offer ALTER COLUMN offer_id SET DEFAULT nextval('test.offer_offer_id_seq')");
                stmt.execute("ALTER TABLE test.offer ADD PRIMARY KEY (offer_id)");

                stmt.execute("CREATE TABLE test.bill_of_materials AS (SELECT * FROM public.bill_of_materials) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.bill_of_materials_bom_id_seq");
                stmt.execute("ALTER TABLE test.bill_of_materials ALTER COLUMN bom_id SET DEFAULT nextval('test.bill_of_materials_bom_id_seq')");
                stmt.execute("ALTER TABLE test.bill_of_materials ADD PRIMARY KEY (bom_id)");
                stmt.execute("ALTER TABLE test.bill_of_materials ADD CONSTRAINT bom_offer_fk " +
                        "FOREIGN KEY (offer_id) REFERENCES test.offer (offer_id)");

                stmt.execute("CREATE TABLE test.material_line AS (SELECT * FROM public.material_line) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.material_line_material_line_id_seq");
                stmt.execute("ALTER TABLE test.material_line ALTER COLUMN material_line_id SET DEFAULT nextval('test.material_line_material_line_id_seq')");
                stmt.execute("ALTER TABLE test.material_line ADD PRIMARY KEY (material_line_id)");
                stmt.execute("ALTER TABLE test.material_line ADD CONSTRAINT material_line_bom_fk " +
                        "FOREIGN KEY (bom_id) REFERENCES test.bill_of_materials (bom_id) ON DELETE CASCADE");
                stmt.execute("ALTER TABLE test.material_line ADD CONSTRAINT material_line_material_fk " +
                        "FOREIGN KEY (material_id) REFERENCES test.material (material_id)");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }

        bomMapper = new BomMapper(connectionPool);
    }

    @BeforeEach
    void setUp()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DELETE FROM test.material_line");
                stmt.execute("DELETE FROM test.bill_of_materials");
                stmt.execute("DELETE FROM test.offer");
                stmt.execute("DELETE FROM test.material_variant");
                stmt.execute("DELETE FROM test.material");

                stmt.execute("INSERT INTO test.material (material_id, name, category, type, material_width, material_height, unit, usage) VALUES " +
                        "(1, 'trykimp. Stolpe', 'WOOD_AND_ROOFING', 'POST', 97, 97, 'stk', 'Stolper nedgraves 90 cm. i jord'), " +
                        "(2, 'spærtræ ubh.', 'WOOD_AND_ROOFING', 'BEAM', 45, 195, 'stk', 'Remme i sider, sadles ned i stolper'), " +
                        "(3, 'spærtræ ubh.', 'WOOD_AND_ROOFING', 'RAFTER', 45, 195, 'stk', 'Spær, monteres på rem'), " +
                        "(4, 'Plastmo Ecolite blåtonet', 'ROOFING', 'ROOF', 109, 5, 'stk', 'Tagplader monteres på spær')");

                stmt.execute("INSERT INTO test.material_variant (material_variant_id, material_id, variant_length, unit_price) VALUES " +
                        "(1, 1, 300, 221.85), " +
                        "(2, 1, 360, 266.21), " +
                        "(3, 2, 360, 190.61), " +
                        "(4, 2, 600, 479.70), " +
                        "(5, 3, 600, 479.70), " +
                        "(6, 4, 360, 199.00)");

                stmt.execute("INSERT INTO test.offer (offer_id, customer_id, carport_id, offer_status, request_created_at) VALUES " +
                        "(1, 1, 1, 'PENDING', CURRENT_TIMESTAMP)");

                stmt.execute("INSERT INTO test.bill_of_materials (bom_id, offer_id, cost_price, coverage_percentage, price_without_vat, total_price) VALUES " +
                        "(1, 1, 10000.00, 0.25, 12500.00, 15625.00)");

                stmt.execute("INSERT INTO test.material_line (material_line_id, bom_id, material_id, quantity, line_total) VALUES " +
                        "(1, 1, 1, 8, 1774.80), " +
                        "(2, 1, 2, 2, 381.22), " +
                        "(3, 1, 3, 12, 5756.40)");

                stmt.execute("SELECT setval('test.material_material_id_seq', COALESCE((SELECT MAX(material_id) + 1 FROM test.material), 1), false)");
                stmt.execute("SELECT setval('test.material_variant_material_variant_id_seq', COALESCE((SELECT MAX(material_variant_id) + 1 FROM test.material_variant), 1), false)");
                stmt.execute("SELECT setval('test.offer_offer_id_seq', COALESCE((SELECT MAX(offer_id) + 1 FROM test.offer), 1), false)");
                stmt.execute("SELECT setval('test.bill_of_materials_bom_id_seq', COALESCE((SELECT MAX(bom_id) + 1 FROM test.bill_of_materials), 1), false)");
                stmt.execute("SELECT setval('test.material_line_material_line_id_seq', COALESCE((SELECT MAX(material_line_id) + 1 FROM test.material_line), 1), false)");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @Test
    void testConnection() throws SQLException
    {
        assertNotNull(connectionPool.getConnection());
    }

    @Test
    void testCreateBillOfMaterials() throws DatabaseException, SQLException
    {

        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        BillOfMaterials bom = bomMapper.createBillOfMaterials(
                connection,
                1,
                15000.00,
                0.30,
                19500.00,
                24375.00
        );

        connection.commit();
        connection.close();

        assertNotNull(bom);
        assertEquals(2, bom.getBomId());
        assertEquals(1, bom.getOfferId());
        assertEquals(0.30, bom.getCoveragePercentage(), 0.001);
        assertEquals(15000.00, bom.getPricingDetails().getCostPrice(), 0.01);
        assertEquals(19500.00, bom.getPricingDetails().getPriceWithOutVat(), 0.01);
        assertEquals(24375.00, bom.getPricingDetails().getTotalPrice(), 0.01);
    }

    @Test
    void testGetBillOfMaterialsById() throws DatabaseException
    {
        BillOfMaterials bom = bomMapper.getBillOfMaterialsById(1);

        assertNotNull(bom);
        assertEquals(1, bom.getBomId());
        assertEquals(1, bom.getOfferId());
        assertEquals(0.25, bom.getCoveragePercentage(), 0.001);
        assertEquals(10000.00, bom.getPricingDetails().getCostPrice(), 0.01);
        assertEquals(12500.00, bom.getPricingDetails().getPriceWithOutVat(), 0.01);
        assertEquals(15625.00, bom.getPricingDetails().getTotalPrice(), 0.01);

        assertNotNull(bom.getMaterialLines());
        assertEquals(3, bom.getMaterialLines().size());

        MaterialLine firstLine = bom.getMaterialLines().get(0);
        assertEquals(1, firstLine.getMaterialLineId());
        assertEquals(1, firstLine.getBomId());
        assertEquals(8, firstLine.getQuantity());
        assertEquals(1774.80, firstLine.getLineTotal(), 0.01);
    }

    @Test
    void testGetBillOfMaterialsByOfferId() throws DatabaseException
    {
        BillOfMaterials bom = bomMapper.getBillOfMaterialsByOfferId(1);

        assertNotNull(bom);
        assertEquals(1, bom.getBomId());
        assertEquals(1, bom.getOfferId());
        assertEquals(3, bom.getMaterialLines().size());
    }

    @Test
    void testGetBillOfMaterialsByIdNotFound()
    {
        DatabaseException exception = assertThrows(DatabaseException.class, () -> {
            bomMapper.getBillOfMaterialsById(999);
        });

        assertTrue(exception.getMessage().contains("ikke fundet"));
    }

    @Test
    void testGetBillOfMaterialsByOfferIdNotFound()
    {
        DatabaseException exception = assertThrows(DatabaseException.class, () -> {
            bomMapper.getBillOfMaterialsByOfferId(999);
        });

        assertTrue(exception.getMessage().contains("ikke fundet"));
    }

    @Test
    void testUpdatePriceInBillOfMaterials() throws DatabaseException
    {
        BillOfMaterials bom = bomMapper.getBillOfMaterialsById(1);

        PricingDetails newPricing = new PricingDetails(
                11000.00,
                13750.00,
                17187.50
        );
        bom.setPricingDetails(newPricing);
        bom.setCoveragePercentage(0.30);

        boolean updated = bomMapper.updateBillOfMaterials(bom);

        assertTrue(updated);

        BillOfMaterials updatedBom = bomMapper.getBillOfMaterialsById(1);
        assertEquals(0.30, updatedBom.getCoveragePercentage(), 0.001);
        assertEquals(11000.00, updatedBom.getPricingDetails().getCostPrice(), 0.01);
        assertEquals(13750.00, updatedBom.getPricingDetails().getPriceWithOutVat(), 0.01);
        assertEquals(17187.50, updatedBom.getPricingDetails().getTotalPrice(), 0.01);
    }

    @Test
    void testUpdatePriceInBillOfMaterialsWithNotFoundWithIdNotExisting() throws DatabaseException
    {
        PricingDetails pricing = new PricingDetails(10000.00, 12500.00, 15625.00);
        BillOfMaterials fakeBom = new BillOfMaterials(
                999,
                1,
                List.of(),
                pricing,
                0.25
        );

        boolean updated = bomMapper.updateBillOfMaterials(fakeBom);

        assertFalse(updated);
    }

    @Test
    void testDeleteBillOfMaterials() throws DatabaseException
    {
        boolean deleted = bomMapper.deleteBillOfMaterials(1);

        assertTrue(deleted);

        assertThrows(DatabaseException.class, () -> {
            bomMapper.getBillOfMaterialsById(1);
        });
    }

    @Test
    void testDeleteBillOfMaterialsNotFound() throws DatabaseException
    {
        boolean deleted = bomMapper.deleteBillOfMaterials(999);

        assertFalse(deleted);
    }

    @Test
    void testFirstLineOfMaterialLineDataInBom() throws DatabaseException
    {
        BillOfMaterials bom = bomMapper.getBillOfMaterialsById(1);

        assertEquals(3, bom.getMaterialLines().size());

        MaterialLine line1 = bom.getMaterialLines().get(0);

        assertEquals("trykimp. Stolpe", line1.getMaterial().getName());
        assertEquals(MaterialType.POST, line1.getMaterial().getMaterialType());
        assertEquals(8, line1.getQuantity());
        assertEquals(1774.80, line1.getLineTotal(), 0.01);
        assertEquals(MaterialCategory.WOOD_AND_ROOFING, line1.getMaterial().getMaterialCategory());
        assertEquals("Stolper nedgraves 90 cm. i jord", line1.getMaterial().getUsage());
    }

    @Test
    void testMaterialDimensionsAreCorrectInBom() throws DatabaseException
    {
        BillOfMaterials bom = bomMapper.getBillOfMaterialsById(1);

        MaterialLine stolpeLine = bom.getMaterialLines().get(0);
        assertEquals(97, stolpeLine.getMaterial().getMaterialWidth());
        assertEquals(97, stolpeLine.getMaterial().getMaterialHeight());

        MaterialLine remmeLine = bom.getMaterialLines().get(1);
        assertEquals(45, remmeLine.getMaterial().getMaterialWidth());
        assertEquals(195, remmeLine.getMaterial().getMaterialHeight());
    }
}