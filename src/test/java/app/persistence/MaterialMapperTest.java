package app.persistence;

import app.entities.Material;
import app.entities.MaterialVariant;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaterialMapperTest
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "carport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static MaterialMapper materialMapper;

    @BeforeAll
    static void setupClass()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DROP TABLE IF EXISTS test.material_line CASCADE");
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

        materialMapper = new MaterialMapper(connectionPool);
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
                        "INSERT INTO test.material (material_id, name, category, type, material_width, material_height, unit, usage) " +
                                "VALUES " +
                                "(1, 'trykimp. Stolpe', 'WOOD_AND_ROOFING', 'POST', 97, 97, 'stk', 'Stolper nedgraves 90 cm. i jord'), " +
                                "(2, 'spærtræ ubh.', 'WOOD_AND_ROOFING', 'BEAM', 45, 195, 'stk', 'Remme i sider, sadles ned i stolper'), " +
                                "(3, 'spærtræ ubh.', 'WOOD_AND_ROOFING', 'RAFTER', 45, 195, 'stk', 'Spær, monteres på rem'), " +
                                "(4, 'Plastmo Ecolite blåtonet', 'WOOD_AND_ROOFING', 'ROOF', 109, 5, 'stk', 'Tagplader monteres på spær'), " +
                                "(5, 'Plastmo Bundskruer', 'FITTINGS_AND_FASTENERS', 'FASTENER', null, null, 'pakke', 'Skruer til tagplader'), " +
                                "(6, 'Universal højre', 'FITTINGS_AND_FASTENERS', 'FITTING', null, null, 'stk', 'Beslag til montering')"
                );

                stmt.execute(
                        "INSERT INTO test.material_variant (material_variant_id, material_id, variant_length, unit_price) " +
                                "VALUES " +
                                "(1, 1, 300, 221.85), " +
                                "(2, 1, 360, 266.21), " +
                                "(3, 2, 360, 190.61), " +
                                "(4, 2, 600, 479.70), " +
                                "(5, 3, 600, 479.70), " +
                                "(6, 4, 360, 199.00), " +
                                "(7, 5, null, 149.00), " +
                                "(8, 6, null, 9.45)"
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
        try (Connection connection = connectionPool.getConnection())
        {
            assertNotNull(connection);
        }
    }

    @Test
    void testCreateMaterial() throws DatabaseException, SQLException
    {
        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            Material material = materialMapper.createMaterial(
                    connection,
                    "Testmateriale",
                    MaterialCategory.WOOD_AND_ROOFING,
                    MaterialType.POST,
                    100,
                    100,
                    "stk",
                    "Test beskrivelse"
            );

            connection.commit();

            assertNotNull(material);
            assertEquals(7, material.getMaterialId());
            assertEquals("Testmateriale", material.getName());
            assertEquals(MaterialCategory.WOOD_AND_ROOFING, material.getCategory());
            assertEquals(MaterialType.POST, material.getType());
            assertEquals(100, material.getMaterialWidth());
            assertEquals(100, material.getMaterialHeight());
            assertEquals("stk", material.getUnit());
            assertEquals("Test beskrivelse", material.getUsage());
        }
    }

    @Test
    void testGetMaterialById() throws DatabaseException
    {
        Material material = materialMapper.getMaterialById(1);

        assertNotNull(material);
        assertEquals(1, material.getMaterialId());
        assertEquals("trykimp. Stolpe", material.getName());
        assertEquals(MaterialCategory.WOOD_AND_ROOFING, material.getCategory());
        assertEquals(MaterialType.POST, material.getType());
        assertEquals(97, material.getMaterialWidth());
        assertEquals(97, material.getMaterialHeight());
        assertEquals("stk", material.getUnit());
        assertEquals("Stolper nedgraves 90 cm. i jord", material.getUsage());
    }

    @Test
    void testGetMaterialByIdNotFound()
    {
        assertThrows(DatabaseException.class, () -> materialMapper.getMaterialById(999));
    }

    @Test
    void testGetAllMaterials() throws DatabaseException
    {
        List<Material> materials = materialMapper.getAllMaterials();

        assertNotNull(materials);
        assertEquals(6, materials.size());
    }

    @Test
    void testGetMaterialsByType() throws DatabaseException
    {
        List<Material> posts = materialMapper.getMaterialsByType(MaterialType.POST);

        assertNotNull(posts);
        assertEquals(1, posts.size());
        assertEquals("trykimp. Stolpe", posts.get(0).getName());

        List<Material> beams = materialMapper.getMaterialsByType(MaterialType.BEAM);
        assertEquals(1, beams.size());
        assertEquals("spærtræ ubh.", beams.get(0).getName());
    }

    @Test
    void testGetMaterialsByCategory() throws DatabaseException
    {
        List<Material> woodMaterials = materialMapper.getMaterialsByCategory(MaterialCategory.WOOD_AND_ROOFING);

        assertNotNull(woodMaterials);
        assertEquals(4, woodMaterials.size());

        List<Material> fastenersAndFittings = materialMapper.getMaterialsByCategory(MaterialCategory.FITTINGS_AND_FASTENERS);
        assertEquals(2, fastenersAndFittings.size());

    }

    @Test
    void testUpdateMaterial() throws DatabaseException, SQLException
    {
        Material material = materialMapper.getMaterialById(1);
        material.setUsage("Opdateret beskrivelse");
        material.setMaterialWidth(100);

        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            boolean updated = materialMapper.updateMaterial(connection, material);

            connection.commit();

            assertTrue(updated);
        }

        Material updatedMaterial = materialMapper.getMaterialById(1);
        assertEquals("Opdateret beskrivelse", updatedMaterial.getUsage());
        assertEquals(100, updatedMaterial.getMaterialWidth());
    }

    @Test
    void testUpdateMaterialNotFound() throws DatabaseException, SQLException
    {
        Material fakeMaterial = new Material(
                999,
                "Fake",
                MaterialCategory.WOOD_AND_ROOFING,
                MaterialType.POST,
                100,
                100,
                "stk",
                "Test"
        );

        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);
            boolean updated = materialMapper.updateMaterial(connection, fakeMaterial);

            connection.commit();
            assertFalse(updated);
        }
    }

    @Test
    void testDeleteMaterial() throws DatabaseException
    {
        boolean deleted = materialMapper.deleteMaterial(1);

        assertTrue(deleted);

        assertThrows(DatabaseException.class, () -> materialMapper.getMaterialById(1));
    }

    @Test
    void testDeleteMaterialCascadesVariants() throws DatabaseException
    {
        MaterialVariantMapper variantMapper = new MaterialVariantMapper(connectionPool);

        List<MaterialVariant> variantsBefore = variantMapper.getAllMaterialVariantsWithMaterialByMaterialId(1);
        assertEquals(2, variantsBefore.size());

        boolean deleted = materialMapper.deleteMaterial(1);
        assertTrue(deleted);
        assertThrows(DatabaseException.class, () -> materialMapper.getMaterialById(1));

        List<MaterialVariant> variantsAfter = variantMapper.getAllMaterialVariantsWithMaterialByMaterialId(1);
        assertEquals(0, variantsAfter.size());
    }

    @Test
    void testDeleteMaterialNotFound() throws DatabaseException
    {
        boolean deleted = materialMapper.deleteMaterial(999);

        assertFalse(deleted);
    }

    @Test
    void testCreateMaterialWithNullableDimensions() throws DatabaseException, SQLException
    {
        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            Material material = materialMapper.createMaterial(
                    connection,
                    "Skruer",
                    MaterialCategory.FITTINGS_AND_FASTENERS,
                    MaterialType.FASTENER,
                    null,
                    null,
                    "pakke",
                    "Forskellige skruer"
            );

            connection.commit();

            assertNotNull(material);
            assertNull(material.getMaterialWidth());
            assertNull(material.getMaterialHeight());
        }
    }
}