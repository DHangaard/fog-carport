package app.persistence;

import app.entities.MaterialLine;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
                stmt.execute("ALTER TABLE test.material ALTER COLUMN material_id SET DEFAULT nextval('test.material_material_id_seq')");
                stmt.execute("ALTER TABLE test.material ADD PRIMARY KEY (material_id)");

                stmt.execute("CREATE TABLE test.material_variant AS (SELECT * FROM public.material_variant) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.material_variant_material_variant_id_seq");
                stmt.execute("ALTER TABLE test.material_variant ALTER COLUMN material_variant_id SET DEFAULT nextval('test.material_variant_material_variant_id_seq')");
                stmt.execute("ALTER TABLE test.material_variant ADD PRIMARY KEY (material_variant_id)");
                stmt.execute("ALTER TABLE test.material_variant ADD CONSTRAINT material_variant_material_fk " +
                        "FOREIGN KEY (material_id) REFERENCES test.material (material_id)");

                stmt.execute("CREATE TABLE test.material_line AS (SELECT * FROM public.material_line) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.material_line_material_line_id_seq");
                stmt.execute("ALTER TABLE test.material_line ALTER COLUMN material_line_id SET DEFAULT nextval('test.material_line_material_line_id_seq')");
                stmt.execute("ALTER TABLE test.material_line ADD PRIMARY KEY (material_line_id)");
                stmt.execute("ALTER TABLE test.material_line ADD CONSTRAINT material_line_material_fk " +
                        "FOREIGN KEY (material_id) REFERENCES test.material (material_id)");
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

                stmt.execute("INSERT INTO test.material_line (material_line_id, bom_id, material_id, quantity, line_total) VALUES " +
                        "(1, 1, 1, 8, 1774.80), " +
                        "(2, 1, 2, 2, 381.22), " +
                        "(3, 1, 3, 12, 5756.40)");

                stmt.execute("SELECT setval('test.material_material_id_seq', COALESCE((SELECT MAX(material_id) + 1 FROM test.material), 1), false)");
                stmt.execute("SELECT setval('test.material_variant_material_variant_id_seq', COALESCE((SELECT MAX(material_variant_id) + 1 FROM test.material_variant), 1), false)");
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
    void getMaterialLineById()
    {

    }

    @Test
    void getMaterialLinesByBomId()
    {
    }

    @Test
    void updateMaterialLine()
    {
    }

    @Test
    void deleteMaterialLine()
    {
    }
}