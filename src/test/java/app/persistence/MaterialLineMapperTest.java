package app.persistence;

import app.entities.MaterialLine;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaterialLineMapperTest
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "carport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static MaterialLineMapper materialLineMapper;

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

                stmt.execute("DROP SEQUENCE IF EXISTS test.material_line_material_line_id_seq CASCADE");
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

                stmt.execute("CREATE TABLE test.material_line AS (SELECT * FROM public.material_line) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.material_line_material_line_id_seq");
                stmt.execute(
                        "ALTER TABLE test.material_line " +
                                "ALTER COLUMN material_line_id SET DEFAULT nextval('test.material_line_material_line_id_seq')"
                );
                stmt.execute("ALTER TABLE test.material_line ADD PRIMARY KEY (material_line_id)");
                stmt.execute(
                        "ALTER TABLE test.material_line ADD CONSTRAINT material_line_variant_fk " +
                                "FOREIGN KEY (material_variant_id) REFERENCES test.material_variant (material_variant_id) ON DELETE CASCADE"
                );
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }

        materialLineMapper = new MaterialLineMapper(connectionPool);
    }

    @BeforeEach
    void setUp()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DELETE FROM test.material_line");
                stmt.execute("DELETE FROM test.material_variant");
                stmt.execute("DELETE FROM test.material");

                stmt.execute(
                        "INSERT INTO test.material (material_id, name, category, type, material_width, material_height, unit, usage) VALUES " +
                                "(1, 'trykimp. Stolpe', 'WOOD_AND_ROOFING', 'POST', 97, 97, 'stk', 'Stolper nedgraves 90 cm. i jord'), " +
                                "(2, 'spærtræ ubh.', 'WOOD_AND_ROOFING', 'BEAM', 45, 195, 'stk', 'Remme i sider, sadles ned i stolper'), " +
                                "(3, 'spærtræ ubh.', 'WOOD_AND_ROOFING', 'RAFTER', 45, 195, 'stk', 'Spær, monteres på rem'), " +
                                "(4, 'Plastmo Ecolite blåtonet', 'WOOD_AND_ROOFING', 'ROOF', 109, 5, 'stk', 'Tagplader monteres på spær')"
                );

                stmt.execute(
                        "INSERT INTO test.material_variant (material_variant_id, material_id, variant_length, unit_price) VALUES " +
                                "(1, 1, 300, 221.85), " +
                                "(2, 1, 360, 266.21), " +
                                "(3, 2, 360, 190.61), " +
                                "(4, 2, 600, 479.70), " +
                                "(5, 3, 600, 479.70), " +
                                "(6, 4, 360, 199.00)"
                );

                stmt.execute(
                        "INSERT INTO test.material_line (material_line_id, order_id, material_variant_id, quantity) VALUES " +
                                "(1, 1, 1, 8), " +
                                "(2, 1, 3, 2), " +
                                "(3, 1, 5, 12), " +
                                "(4, 2, 2, 6), " +
                                "(5, 2, 4, 3)"
                );

                stmt.execute(
                        "SELECT setval('test.material_material_id_seq', " +
                                "COALESCE((SELECT MAX(material_id) + 1 FROM test.material), 1), false)"
                );
                stmt.execute(
                        "SELECT setval('test.material_variant_material_variant_id_seq', " +
                                "COALESCE((SELECT MAX(material_variant_id) + 1 FROM test.material_variant), 1), false)"
                );
                stmt.execute(
                        "SELECT setval('test.material_line_material_line_id_seq', " +
                                "COALESCE((SELECT MAX(material_line_id) + 1 FROM test.material_line), 1), false)"
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
    void testCreateMaterialLine() throws DatabaseException, SQLException
    {
        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        try
        {
            MaterialLine line = materialLineMapper.createMaterialLine(connection, 1, 6, 10);

            connection.commit();

            assertNotNull(line);
            assertEquals(6, line.getMaterialLineId());
            assertEquals(1, line.getOrderId());
            assertNull(line.getMaterialVariant());
            assertEquals(10, line.getQuantity());
        }
        finally
        {
            connection.close();
        }
    }

    @Test
    void testGetMaterialLineById() throws DatabaseException
    {
        MaterialLine line = materialLineMapper.getMaterialLineById(1);

        assertNotNull(line);
        assertEquals(1, line.getMaterialLineId());
        assertEquals(1, line.getOrderId());
        assertEquals(8, line.getQuantity());
        assertNotNull(line.getMaterialVariant());
        assertEquals(1, line.getMaterialVariant().getMaterialVariantId());
        assertEquals(300, line.getMaterialVariant().getVariantLength());
        assertEquals(221.85, line.getMaterialVariant().getUnitPrice());
        assertNotNull(line.getMaterialVariant().getMaterial());
        assertEquals("trykimp. Stolpe", line.getMaterialVariant().getMaterial().getName());
    }

    @Test
    void testGetMaterialLineByIdNotFound()
    {
        assertThrows(DatabaseException.class, () -> materialLineMapper.getMaterialLineById(999));
    }

    @Test
    void testGetMaterialLinesByOrderId() throws DatabaseException
    {
        List<MaterialLine> lines = materialLineMapper.getMaterialLinesByOrderId(1);

        assertNotNull(lines);
        assertEquals(3, lines.size());

        assertEquals(1, lines.get(0).getMaterialLineId());
        assertEquals(8, lines.get(0).getQuantity());
        assertEquals("trykimp. Stolpe", lines.get(0).getMaterialVariant().getMaterial().getName());

        assertEquals(2, lines.get(1).getMaterialLineId());
        assertEquals(2, lines.get(1).getQuantity());
        assertEquals("spærtræ ubh.", lines.get(1).getMaterialVariant().getMaterial().getName());

        assertEquals(3, lines.get(2).getMaterialLineId());
        assertEquals(12, lines.get(2).getQuantity());
        assertEquals("spærtræ ubh.", lines.get(2).getMaterialVariant().getMaterial().getName());
    }

    @Test
    void testGetMaterialLinesByOrderIdEmpty() throws DatabaseException
    {
        List<MaterialLine> lines = materialLineMapper.getMaterialLinesByOrderId(999);

        assertNotNull(lines);
        assertEquals(0, lines.size());
    }

    @Test
    void testUpdateMaterialLine() throws DatabaseException, SQLException
    {
        MaterialLine line = materialLineMapper.getMaterialLineById(1);
        assertEquals(8, line.getQuantity());

        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        try
        {
            line.setQuantity(12);
            boolean updated = materialLineMapper.updateMaterialLine(connection, line);

            connection.commit();
            assertTrue(updated);
        }
        finally
        {
            connection.close();
        }

        MaterialLine updatedLine = materialLineMapper.getMaterialLineById(1);
        assertEquals(12, updatedLine.getQuantity());
    }

    @Test
    void testUpdateMaterialLineChangeVariant() throws DatabaseException, SQLException
    {
        MaterialLine line = materialLineMapper.getMaterialLineById(1);
        assertEquals(1, line.getMaterialVariant().getMaterialVariantId());

        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        try
        {
            line.getMaterialVariant().setMaterialVariantId(2);
            line.setQuantity(10);

            boolean updated = materialLineMapper.updateMaterialLine(connection, line);
            connection.commit();
            assertTrue(updated);
        }
        finally
        {
            connection.close();
        }

        MaterialLine result = materialLineMapper.getMaterialLineById(1);
        assertEquals(2, result.getMaterialVariant().getMaterialVariantId());
        assertEquals(10, result.getQuantity());
    }

    @Test
    void testDeleteMaterialLine() throws DatabaseException
    {
        boolean deleted = materialLineMapper.deleteMaterialLine(1);

        assertTrue(deleted);

        assertThrows(DatabaseException.class, () -> materialLineMapper.getMaterialLineById(1));

        List<MaterialLine> remainingLines = materialLineMapper.getMaterialLinesByOrderId(1);
        assertEquals(2, remainingLines.size());
    }

    @Test
    void testDeleteMaterialLineNotFound() throws DatabaseException
    {
        boolean deleted = materialLineMapper.deleteMaterialLine(999);

        assertFalse(deleted);
    }

    @Test
    void testMultipleOrdersIndependentLines() throws DatabaseException
    {
        List<MaterialLine> order1Lines = materialLineMapper.getMaterialLinesByOrderId(1);
        List<MaterialLine> order2Lines = materialLineMapper.getMaterialLinesByOrderId(2);

        assertEquals(3, order1Lines.size());
        assertEquals(2, order2Lines.size());

        assertNotEquals(order1Lines.get(0).getMaterialLineId(), order2Lines.get(0).getMaterialLineId());
    }

    @Test
    void testCascadeDeleteMaterial() throws DatabaseException, SQLException
    {
        List<MaterialLine> linesBefore = materialLineMapper.getMaterialLinesByOrderId(1);
        assertEquals(3, linesBefore.size());

        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        try
        {
            Statement stmt = connection.createStatement();
            stmt.execute("DELETE FROM test.material WHERE material_id = 1");
            connection.commit();
        }
        finally
        {
            connection.close();
        }

        List<MaterialLine> linesAfter = materialLineMapper.getMaterialLinesByOrderId(1);
        assertEquals(2, linesAfter.size());
    }
}