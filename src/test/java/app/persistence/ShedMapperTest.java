package app.persistence;

import app. entities.Shed;
import app.enums.ShedPlacement;
import app.exceptions.DatabaseException;
import org.junit. jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter. api.Assertions.*;

class ShedMapperTest
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "carport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static ShedMapper shedMapper;

    @BeforeAll
    static void setupClass()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DROP TABLE IF EXISTS test.carport CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.shed CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.carport_carport_id_seq CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.shed_shed_id_seq CASCADE");

                stmt.execute("CREATE TABLE test.shed AS (SELECT * FROM public.shed) WITH NO DATA");
                stmt.execute("CREATE TABLE test.carport AS (SELECT * FROM public.carport) WITH NO DATA");

                stmt.execute("ALTER TABLE test.shed ADD PRIMARY KEY (shed_id)");
                stmt.execute("ALTER TABLE test.carport ADD PRIMARY KEY (carport_id)");

                stmt.execute("CREATE SEQUENCE test.shed_shed_id_seq");
                stmt.execute("ALTER TABLE test.shed ALTER COLUMN shed_id SET DEFAULT nextval('test.shed_shed_id_seq')");

                stmt.execute("CREATE SEQUENCE test.carport_carport_id_seq");
                stmt.execute("ALTER TABLE test.carport ALTER COLUMN carport_id SET DEFAULT nextval('test.carport_carport_id_seq')");

                stmt.execute("ALTER TABLE test.carport ADD CONSTRAINT carport_shed_fk " +
                        "FOREIGN KEY (shed_id) REFERENCES test.shed (shed_id) ON DELETE SET NULL");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }

        shedMapper = new ShedMapper(connectionPool);
    }

    @BeforeEach
    void setUp()
    {
        try (Connection connection = connectionPool.getConnection();
             Statement stmt = connection.createStatement())
        {
            stmt.execute("DELETE FROM test.shed");
            stmt.execute("INSERT INTO test.shed (shed_id, length, width, shed_placement) VALUES " +
                    "(1, 300, 600, 'FULL_WIDTH'), " +
                    "(2, 250, 500, 'LEFT')");
            stmt.execute("SELECT setval('test.shed_shed_id_seq', COALESCE((SELECT MAX(shed_id) + 1 FROM test.shed), 1), false)");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @Test
    void testCreateShed() throws DatabaseException, SQLException
    {
        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            Shed shed = shedMapper.createShed(connection, 200, 400, ShedPlacement.LEFT);

            connection.commit();

            assertNotNull(shed);
            assertEquals(3, shed.getShedId());
            assertEquals(200, shed.getLength());
            assertEquals(400, shed.getWidth());
            assertEquals(ShedPlacement.LEFT, shed.getShedPlacement());
        }
    }

    @Test
    void testGetShedById() throws DatabaseException
    {
        Shed shed = shedMapper.getShedById(1);

        assertNotNull(shed);
        assertEquals(1, shed.getShedId());
        assertEquals(300, shed.getLength());
        assertEquals(600, shed.getWidth());
        assertEquals(ShedPlacement.FULL_WIDTH, shed.getShedPlacement());
    }

    @Test
    void testGetShedByIdNotFound()
    {
        assertThrows(DatabaseException.class, () -> shedMapper.getShedById(999));
    }

    @Test
    void testUpdateShed() throws DatabaseException, SQLException
    {
        Shed shed = shedMapper.getShedById(1);
        shed.setLength(350);
        shed.setWidth(650);
        shed.setShedPlacement(ShedPlacement.LEFT);

        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            boolean updated = shedMapper.updateShed(connection, shed);

            connection.commit();

            assertTrue(updated);
        }

        Shed updatedShed = shedMapper.getShedById(1);
        assertEquals(350, updatedShed.getLength());
        assertEquals(650, updatedShed.getWidth());
        assertEquals(ShedPlacement.LEFT, updatedShed. getShedPlacement());
    }

    @Test
    void testUpdateShedNotFound() throws DatabaseException, SQLException
    {
        Shed shed = new Shed(999, 300, 600, ShedPlacement.FULL_WIDTH);

        try (Connection connection = connectionPool. getConnection())
        {
            connection.setAutoCommit(false);

            boolean updated = shedMapper.updateShed(connection, shed);

            connection.commit();

            assertFalse(updated);
        }
    }

    @Test
    void testDeleteShed() throws DatabaseException, SQLException
    {
        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            boolean deleted = shedMapper.deleteShed(connection, 1);

            connection.commit();

            assertTrue(deleted);
        }

        assertThrows(DatabaseException.class, () -> shedMapper.getShedById(1));
    }

    @Test
    void testDeleteShedNotFound() throws DatabaseException, SQLException
    {
        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            boolean deleted = shedMapper.deleteShed(connection, 999);

            connection.commit();

            assertFalse(deleted);
        }
    }
}