package app.persistence;

import app.entities.Material;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
                stmt.execute("ALTER TABLE test.material_variant ADD CONSTRAINT material_variant_material_fk " +
                        "FOREIGN KEY (material_id) REFERENCES test.material (material_id)");
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

                stmt.execute("INSERT INTO test.material (material_id, name, category, type, material_width, material_height, unit, usage) VALUES " +
                        "(1, 'trykimp. Stolpe', 'WOOD_AND_ROOFING', 'POST', 97, 97, 'stk', 'Stolper nedgraves 90 cm. i jord'), " +
                        "(2, 'spærtræ ubh.', 'WOOD_AND_ROOFING', 'BEAM', 45, 195, 'stk', 'Remme i sider, sadles ned i stolper'), " +
                        "(3, 'spærtræ ubh.', 'WOOD_AND_ROOFING', 'RAFTER', 45, 195, 'stk', 'Spær, monteres på rem'), " +
                        "(4, 'Plastmo Ecolite blåtonet', 'WOOD_AND_ROOFING', 'ROOF', 109, 5, 'stk', 'Tagplader monteres på spær'), " +
                        "(5, 'Plastmo Bundskruer', 'FITTINGS_AND_FASTENERS', 'FASTENER', null, null, 'stk', 'Skruer til tagplader'), " +
                        "(6, 'trykimp. Stolpe', 'WOOD_AND_ROOFING', 'POST', 45, 45, 'stk', 'Stolper nedgraves 90 cm. i jord')");

                stmt.execute("INSERT INTO test.material_variant (material_variant_id, material_id, variant_length, unit_price) VALUES " +
                        "(1, 1, 300, 221.85), " +
                        "(2, 1, 360, 266.21), " +
                        "(3, 2, 360, 190.61), " +
                        "(4, 2, 600, 479.70), " +
                        "(5, 3, 600, 479.70), " +
                        "(6, 4, 360, 199.00), " +
                        "(7, 5, null, 199.00), " +
                        "(8, 6, 300, 179.95)");

                stmt.execute("SELECT setval('test.material_material_id_seq', COALESCE((SELECT MAX(material_id) + 1 FROM test.material), 1), false)");
                stmt.execute("SELECT setval('test.material_variant_material_variant_id_seq', COALESCE((SELECT MAX(material_variant_id) + 1 FROM test.material_variant), 1), false)");
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
    void testCreateMaterial() throws DatabaseException
    {
       Material material = materialMapper.createMaterial(
               "Test",
               MaterialCategory.FITTINGS_AND_FASTENERS,
               MaterialType.FASTENER,
               200,
               18,
               "mm",
               "Test description",
               600,
               74.95
       );

       assertNotNull(material);
       assertEquals(7, material.getMaterialId());
       assertEquals("Test", material.getName());
       assertEquals(MaterialCategory.FITTINGS_AND_FASTENERS, material.getMaterialCategory());
       assertEquals(MaterialType.FASTENER, material.getMaterialType());
       assertEquals(200, material.getMaterialWidth());
       assertEquals(18, material.getMaterialHeight());
       assertEquals("mm", material.getUnit());
       assertEquals("Test description", material.getUsage());
       assertEquals(9, material.getMaterialVariantId());
       assertEquals(600, material.getVariantLength());
       assertEquals(74.95, material.getUnitPrice());
    }

    @Test
    void testCreateMaterialVariant() throws DatabaseException
    {
        Material material = materialMapper.createMaterialVariant(
                1,
                600,
                419.95
        );

        assertNotNull(material);
        assertEquals(1, material.getMaterialId());
        assertEquals("trykimp. Stolpe", material.getName());
        assertEquals(MaterialCategory.WOOD_AND_ROOFING, material.getMaterialCategory());
        assertEquals(MaterialType.POST, material.getMaterialType());
        assertEquals(97, material.getMaterialWidth());
        assertEquals(97, material.getMaterialHeight());
        assertEquals("stk", material.getUnit());
        assertEquals("Stolper nedgraves 90 cm. i jord", material.getUsage());
        assertEquals(9, material.getMaterialVariantId());
        assertEquals(600, material.getVariantLength());
        assertEquals(419.95, material.getUnitPrice());
    }

    @Test
    void testGetMaterialByIdWithDefaultVariant() throws DatabaseException
    {
        Material material = materialMapper.getMaterialById(2);

        assertNotNull(material);
        assertEquals(2, material.getMaterialId());
        assertEquals("spærtræ ubh.", material.getName());
        assertEquals(MaterialCategory.WOOD_AND_ROOFING, material.getMaterialCategory());
        assertEquals(MaterialType.BEAM, material.getMaterialType());
        assertEquals(45, material.getMaterialWidth());
        assertEquals(195, material.getMaterialHeight());
        assertEquals("stk", material.getUnit());
        assertEquals("Remme i sider, sadles ned i stolper", material.getUsage());
        assertEquals(3, material.getMaterialVariantId());
        assertEquals(360, material.getVariantLength());
        assertEquals(190.61, material.getUnitPrice());
    }

    @Test
    void testGetMaterialByIdWithSpecificVariant() throws DatabaseException
    {
        Material material = materialMapper.getMaterialById(2, 4);

        assertNotNull(material);
        assertEquals(2, material.getMaterialId());
        assertEquals("spærtræ ubh.", material.getName());
        assertEquals(MaterialCategory.WOOD_AND_ROOFING, material.getMaterialCategory());
        assertEquals(MaterialType.BEAM, material.getMaterialType());
        assertEquals(45, material.getMaterialWidth());
        assertEquals(195, material.getMaterialHeight());
        assertEquals("stk", material.getUnit());
        assertEquals("Remme i sider, sadles ned i stolper", material.getUsage());
        assertEquals(4, material.getMaterialVariantId());
        assertEquals(600, material.getVariantLength());
        assertEquals(479.70, material.getUnitPrice());
    }

    @Test
    void testGetMaterialsByType() throws DatabaseException
    {
        List<Material> materials = materialMapper.getMaterialsByType(MaterialType.POST);

        assertNotNull(materials);
        assertEquals(3, materials.size());
        assertEquals(1, materials.get(0).getMaterialId()); // Variant 1
        assertEquals(1, materials.get(1).getMaterialId()); // Variant 2
        assertEquals(6, materials.get(2).getMaterialId());
    }

    @Test
    void testGetMaterialsByCategory() throws DatabaseException
    {
        List<Material> materials = materialMapper.getMaterialsByCategory(MaterialCategory.WOOD_AND_ROOFING);

        List<Integer> materialIds = new ArrayList<>();
        for (Material material : materials)
        {
            materialIds.add(material.getMaterialId());
        }

        assertNotNull(materials);
        assertEquals(7, materials.size());
        assertTrue(materialIds.contains(1)); // Contains two variants
        assertTrue(materialIds.contains(2)); // Contains two variants
        assertTrue(materialIds.contains(3));
        assertTrue(materialIds.contains(4));
        assertTrue(materialIds.contains(6));
    }

    @Test
    void testGetAllMaterials() throws DatabaseException
    {
        List<Material> materials = materialMapper.getAllMaterials();

        List<Integer> materialIds = new ArrayList<>();
        for (Material material : materials)
        {
            materialIds.add(material.getMaterialId());
        }

        assertNotNull(materials);
        assertEquals(8, materials.size());
        assertTrue(materialIds.contains(1)); // Contains two variants
        assertTrue(materialIds.contains(2)); // Contains two variants
        assertTrue(materialIds.contains(3));
        assertTrue(materialIds.contains(4));
        assertTrue(materialIds.contains(5));
        assertTrue(materialIds.contains(6));
    }

    @Test
    void testUpdateMaterial() throws DatabaseException
    {
        Material material = new Material(
                1,
                "Trykimp. Stolpe",
                MaterialCategory.WOOD_AND_ROOFING,
                MaterialType.POST,
                95,
                95,
                "stk",
                "Opdateret beskrivelse",
                1,
                300,
                221.85);

        boolean updated = materialMapper.updateMaterial(material);

        assertTrue(updated);
        Material updatedMaterial = materialMapper.getMaterialById(1);
        assertEquals("Opdateret beskrivelse", updatedMaterial.getUsage());
    }

    @Test
    void testUpdateMaterialVariant() throws DatabaseException
    {
        Material material = new Material(
                1,
                "Trykimp. Stolpe",
                MaterialCategory.WOOD_AND_ROOFING,
                MaterialType.POST,
                95,
                95,
                "stk",
                "Stolper nedgraves 90 cm. i jord",
                1,
                300,
                249.95);

        boolean updated = materialMapper.updateMaterialVariant(material);

        assertTrue(updated);
        Material updatedMaterial = materialMapper.getMaterialById(1);
        assertEquals(249.95, updatedMaterial.getUnitPrice());
    }

    @Test
    void testDeleteMaterial() throws DatabaseException
    {
        boolean deleted = materialMapper.deleteMaterial(1);

        assertTrue(deleted);
        assertThrows(DatabaseException.class, () -> {
            materialMapper.getMaterialById(1);
        });
    }

    @Test
    void testDeleteMaterialVariant() throws DatabaseException
    {
        boolean deleted = materialMapper.deleteMaterialVariant(1);
        Material material = materialMapper.getMaterialById(1);

        assertTrue(deleted);
        assertEquals(2, material.getMaterialVariantId());
        assertThrows(DatabaseException.class, () -> {
            materialMapper.getMaterialById(1, 1);
        });
    }
}