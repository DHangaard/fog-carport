package app.persistence;

import app.entities.MaterialVariant;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaterialVariantMapperTest
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "carport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static MaterialVariantMapper materialVariantMapper;

    @BeforeAll
    static void setupClass()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DROP TABLE IF EXISTS test.material_variant CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.material CASCADE");

                stmt.execute("DROP SEQUENCE IF EXISTS test.material_variant_material_variant_id_seq CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.material_material_id_seq CASCADE");

                stmt.execute("CREATE TABLE test.material AS (SELECT * FROM public.material) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.material_material_id_seq");
                stmt.execute(
                        "ALTER TABLE test.material " +
                                "ALTER COLUMN material_id SET DEFAULT nextval('test.material_material_id_seq')"
                );
                stmt.execute("ALTER TABLE test.material ADD PRIMARY KEY (material_id)");

                stmt.execute("CREATE TABLE test.material_variant AS (SELECT * FROM public.material_variant) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.material_variant_material_variant_id_seq");
                stmt.execute(
                        "ALTER TABLE test.material_variant " +
                                "ALTER COLUMN material_variant_id SET DEFAULT nextval('test.material_variant_material_variant_id_seq')"
                );
                stmt.execute("ALTER TABLE test.material_variant ADD PRIMARY KEY (material_variant_id)");
                stmt.execute(
                        "ALTER TABLE test.material_variant ADD CONSTRAINT material_variant_material_fk " +
                                "FOREIGN KEY (material_id) REFERENCES test.material (material_id) ON DELETE CASCADE"
                );
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }

        materialVariantMapper = new MaterialVariantMapper(connectionPool);
    }

    @BeforeEach
    void setUp()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DELETE FROM test.material_variant");
                stmt.execute("DELETE FROM test.material");

                stmt.execute(
                        "INSERT INTO test.material (material_id, name, category, type, material_width, material_height, unit, usage) VALUES " +
                                "(1, 'trykimp. Stolpe', 'WOOD_AND_ROOFING', 'POST', 97, 97, 'stk', 'Stolper nedgraves 90 cm. i jord'), " +
                                "(2, 'spærtræ ubh.', 'WOOD_AND_ROOFING', 'BEAM', 45, 195, 'stk', 'Remme i sider, sadles ned i stolper'), " +
                                "(3, 'Plastmo Bundskruer', 'FITTINGS_AND_FASTENERS', 'FASTENER', null, null, 'pakke', 'Skruer til tagplader'), " +
                                "(4, 'Universal højre', 'FITTINGS_AND_FASTENERS', 'FITTING', null, null, 'stk', 'Beslag til montering')"
                );

                stmt.execute(
                        "INSERT INTO test.material_variant (material_variant_id, material_id, variant_length, unit_price, pieces_per_unit) VALUES " +
                                "(1, 1, 300, 221.85, 1), " +
                                "(2, 1, 360, 266.21, 1), " +
                                "(3, 2, 360, 190.61, 1), " +
                                "(4, 2, 600, 479.70, 1), " +
                                "(5, 3, null, 149.00, 200), " +
                                "(6, 4, null, 9.45, 1)"
                );

                stmt.execute(
                        "SELECT setval('test.material_material_id_seq', " +
                                "COALESCE((SELECT MAX(material_id) + 1 FROM test.material), 1), false)"
                );
                stmt.execute(
                        "SELECT setval('test.material_variant_material_variant_id_seq', " +
                                "COALESCE((SELECT MAX(material_variant_id) + 1 FROM test.material_variant), 1), false)"
                );
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
    void testCreateMaterialVariant() throws DatabaseException, SQLException
    {
        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        MaterialVariant variant = materialVariantMapper.createMaterialVariant(connection, 1, 480, 350.50, 1);

        connection.commit();

        assertNotNull(variant);
        assertEquals(7, variant.getMaterialVariantId());
        assertEquals(1, variant.getMaterialId());
        assertEquals(480, variant.getVariantLength());
        assertEquals(350.50, variant.getUnitPrice());
        assertEquals(1, variant.getPiecesPerUnit());
        assertNull(variant.getMaterial());

        connection.close();

    }

    @Test
    void testCreateMaterialVariantWithoutLength() throws DatabaseException, SQLException
    {
        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        MaterialVariant variant = materialVariantMapper.createMaterialVariant(connection, 3, null, 199.00, 1);

        connection.commit();

        assertNotNull(variant);
        assertEquals(7, variant.getMaterialVariantId());
        assertEquals(3, variant.getMaterialId());
        assertNull(variant.getVariantLength());
        assertEquals(199.00, variant.getUnitPrice());

        connection.close();
    }

    @Test
    void testGetVariantWithMaterialById() throws DatabaseException
    {
        MaterialVariant variant = materialVariantMapper.getVariantWithMaterialById(1);

        assertNotNull(variant);
        assertEquals(1, variant.getMaterialVariantId());
        assertEquals(1, variant.getMaterialId());
        assertEquals(300, variant.getVariantLength());
        assertEquals(221.85, variant.getUnitPrice());
        assertNotNull(variant.getMaterial());
        assertEquals("trykimp. Stolpe", variant.getMaterial().getName());
        assertEquals(97, variant.getMaterial().getMaterialWidth());
        assertEquals(97, variant.getMaterial().getMaterialHeight());
    }

    @Test
    void testGetVariantWithMaterialByIdNotFound()
    {
        assertThrows(DatabaseException.class, () -> materialVariantMapper.getVariantWithMaterialById(999));
    }

    @Test
    void testCreateVariantWithMultiplePiecies() throws DatabaseException, SQLException
    {
        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        MaterialVariant variant = materialVariantMapper.createMaterialVariant(connection, 3, null, 149.00, 300);

        connection.commit();

        assertNotNull(variant);
        assertEquals(7, variant.getMaterialVariantId());
        assertEquals(3, variant.getMaterialId());
        assertNull(variant.getVariantLength());
        assertEquals(149.00, variant.getUnitPrice());
        assertEquals(300, variant.getPiecesPerUnit());

        connection.close();


    }

    @Test
    void testGetAllMaterialVariantsWithMaterialByMaterialId() throws DatabaseException
    {
        List<MaterialVariant> variants = materialVariantMapper.getAllMaterialVariantsWithMaterialByMaterialId(1);

        assertNotNull(variants);
        assertEquals(2, variants.size());

        assertEquals(1, variants.get(0).getMaterialVariantId());
        assertEquals(300, variants.get(0).getVariantLength());
        assertEquals(221.85, variants.get(0).getUnitPrice());

        assertEquals(2, variants.get(1).getMaterialVariantId());
        assertEquals(360, variants.get(1).getVariantLength());
        assertEquals(266.21, variants.get(1).getUnitPrice());

        for (MaterialVariant variant : variants)
        {
            assertNotNull(variant.getMaterial());
            assertEquals("trykimp. Stolpe", variant.getMaterial().getName());
        }
    }

    @Test
    void testGetAllMaterialVariantsEmpty() throws DatabaseException
    {
        List<MaterialVariant> variants = materialVariantMapper.getAllMaterialVariantsWithMaterialByMaterialId(999);

        assertNotNull(variants);
        assertEquals(0, variants.size());
    }

    @Test
    void testGetAllVariantsByType() throws DatabaseException
    {
        List<MaterialVariant> postVariants = materialVariantMapper.getAllVariantsByType(MaterialType.POST);

        assertNotNull(postVariants);
        assertEquals(2, postVariants.size());

        for (MaterialVariant variant : postVariants)
        {
            assertEquals(MaterialType.POST, variant.getMaterial().getType());
        }
    }

    @Test
    void testGetVariantsWithNullLength() throws DatabaseException
    {
        MaterialVariant variant = materialVariantMapper.getVariantWithMaterialById(5);

        assertNotNull(variant);
        assertNull(variant.getVariantLength());
        assertEquals(149.00, variant.getUnitPrice());
        assertEquals("Plastmo Bundskruer", variant.getMaterial().getName());
    }

    @Test
    void testUpdateMaterialVariant() throws DatabaseException, SQLException
    {
        MaterialVariant variant = materialVariantMapper.getVariantWithMaterialById(1);
        assertEquals(221.85, variant.getUnitPrice());

        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        variant.setUnitPrice(249.95);
        boolean updated = materialVariantMapper.updateMaterialVariant(connection, variant);

        connection.commit();
        assertTrue(updated);

        connection.close();

        MaterialVariant updatedVariant = materialVariantMapper.getVariantWithMaterialById(1);
        assertEquals(249.95, updatedVariant.getUnitPrice());
    }

    @Test
    void testUpdateMaterialVariantLength() throws DatabaseException, SQLException
    {
        MaterialVariant variant = materialVariantMapper.getVariantWithMaterialById(1);
        assertEquals(300, variant.getVariantLength());

        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        variant.setVariantLength(330);
        boolean updated = materialVariantMapper.updateMaterialVariant(connection, variant);

        connection.commit();
        assertTrue(updated);

        connection.close();

        MaterialVariant updatedVariant = materialVariantMapper.getVariantWithMaterialById(1);
        assertEquals(330, updatedVariant.getVariantLength());
    }

    @Test
    void testDeleteMaterialVariant() throws DatabaseException
    {
        boolean deleted = materialVariantMapper.deleteMaterialVariant(1);

        assertTrue(deleted);

        assertThrows(DatabaseException.class, () -> materialVariantMapper.getVariantWithMaterialById(1));

        List<MaterialVariant> remainingVariants = materialVariantMapper.getAllMaterialVariantsWithMaterialByMaterialId(1);
        assertEquals(1, remainingVariants.size());
    }

    @Test
    void testDeleteMaterialVariantNotFound() throws DatabaseException
    {
        boolean deleted = materialVariantMapper.deleteMaterialVariant(999);

        assertFalse(deleted);
    }

    @Test
    void testVariantsDontHaveSameLength() throws DatabaseException
    {
        List<MaterialVariant> materialVariants = materialVariantMapper.getAllMaterialVariantsWithMaterialByMaterialId(1);

        assertEquals(2, materialVariants.size());

        assertFalse(materialVariants.get(0).getVariantLength() == materialVariants.get(1).getVariantLength());
    }
}