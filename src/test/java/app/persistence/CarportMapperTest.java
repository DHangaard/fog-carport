package app.persistence;

import app.entities.Carport;
import app.entities.Shed;
import app.enums.RoofType;
import app.enums.ShedPlacement;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;


class CarportMapperTest
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "carport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static CarportMapper carportMapper;
    private static ShedMapper shedMapper;

    @BeforeAll
    static void setupClass() {
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
        carportMapper = new CarportMapper(connectionPool);
    }

    @BeforeEach
    void setUp()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement()) {

                stmt.execute("DELETE FROM test.carport");
                stmt.execute("DELETE FROM test.shed");

                stmt.execute("INSERT INTO test.shed (shed_id, length, width, shed_placement) VALUES " +
                        "(1, 300, 600, 'FULL_WIDTH')");

                stmt.execute("INSERT INTO test.carport (carport_id, length, width, shed_id, roof_type) VALUES " +
                        "(1, 600, 780, 1, 'FLAT'), " +
                        "(2, 540, 690, NULL, 'FLAT')");

                stmt.execute("SELECT setval('test.shed_shed_id_seq', COALESCE((SELECT MAX(shed_id) + 1 FROM test.shed), 1), false)");
                stmt.execute("SELECT setval('test.carport_carport_id_seq', COALESCE((SELECT MAX(carport_id) + 1 FROM test.carport), 1), false)");
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
    void testCreateCarportWithoutShed() throws DatabaseException, SQLException
    {
        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            Carport createdCarport = carportMapper.createCarport(connection, 600, 500, null, RoofType.FLAT);

            connection.commit();

            assertNotNull(createdCarport);
            assertTrue(createdCarport.getCarportId() == 3);
            assertEquals(600, createdCarport.getLength());
            assertEquals(500, createdCarport.getWidth());
            assertNull(createdCarport.getShed());
            assertEquals(RoofType.FLAT, createdCarport.getRoofType());
        }
    }

    @Test
    void testCreateCarportWithShed() throws DatabaseException, SQLException
    {
        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);
            Shed createdShed = shedMapper.createShed(connection, 250, 500, ShedPlacement.LEFT);
            Carport createdCarport = carportMapper.createCarport(connection, 690, 600, null, RoofType.FLAT);

            connection.commit();

            assertNotNull(createdShed);
            assertNotNull(createdCarport);
            createdCarport.setShed(createdShed);

            assertTrue(createdCarport.getCarportId() == 3);
            assertEquals(690, createdCarport.getLength());
            assertEquals(600, createdCarport.getWidth());
            assertEquals(RoofType.FLAT, createdCarport.getRoofType());

            assertNotNull(createdCarport.getShed());
            assertTrue(createdCarport.getShed().getShedId() > 0);
            assertEquals(250, createdCarport.getShed().getLength());
            assertEquals(500, createdCarport.getShed().getWidth());
            assertEquals(ShedPlacement.LEFT, createdCarport.getShed().getShedPlacement());

        }
    }

    @Test
    void testGetCarportById() throws DatabaseException
    {
        Carport carport = carportMapper.getCarportById(1);

        assertNotNull(carport);
        assertEquals(1, carport.getCarportId());
        assertEquals(600, carport.getLength());
        assertEquals(780, carport.getWidth());
        assertEquals(RoofType.FLAT, carport.getRoofType());

        assertNotNull(carport.getShed());
        assertEquals(1, carport.getShed().getShedId());
        assertEquals(300, carport.getShed().getLength());
        assertEquals(600, carport.getShed().getWidth());
        assertEquals(ShedPlacement.FULL_WIDTH, carport.getShed().getShedPlacement());
    }

    @Test
    void testGetCarportByIdWithoutShed() throws DatabaseException
    {
        Carport carport = carportMapper.getCarportById(2);

        assertNotNull(carport);
        assertEquals(2, carport.getCarportId());
        assertEquals(540, carport.getLength());
        assertEquals(690, carport.getWidth());
        assertEquals(RoofType.FLAT, carport.getRoofType());
        assertNull(carport.getShed());
    }

    @Test
    void testGetCarportByIdNotFound()
    {
        assertThrows(DatabaseException.class, () -> {
            carportMapper.getCarportById(999);
        });
    }

    @Test
    void testUpdateCarportWithoutChangingShed() throws DatabaseException, SQLException
    {
        Carport carport = carportMapper.getCarportById(1);
        carport.setLength(650);
        carport.setWidth(800);
        carport.setRoofType(RoofType.FLAT);

        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            boolean updated = carportMapper.updateCarport(connection, carport);

            connection.commit();

            assertTrue(updated);

            Carport updatedCarport = carportMapper.getCarportById(1);
            assertEquals(650, updatedCarport.getLength());
            assertEquals(800, updatedCarport.getWidth());
            assertEquals(RoofType.FLAT, updatedCarport.getRoofType());
            assertNotNull(updatedCarport.getShed());
            assertEquals(1, updatedCarport.getShed().getShedId());
        }
    }

    @Test
    void testUpdateCarportWithAddedShed() throws DatabaseException, SQLException
    {
        Carport carport = carportMapper.getCarportById(2);

        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            Shed newShed = shedMapper.createShed(connection, 200, 400, ShedPlacement.LEFT);

            connection.commit();

            carport.setShed(newShed);
        }

        try(Connection newConnection = connectionPool.getConnection())
        {
            newConnection.setAutoCommit(false);

            boolean updated = carportMapper.updateCarport(newConnection, carport);

            newConnection.commit();

            assertTrue(updated);
        }

        Carport updatedCarport = carportMapper.getCarportById(2);
        assertNotNull(updatedCarport.getShed());
        assertEquals(200, updatedCarport.getShed().getLength());
        assertEquals(400, updatedCarport.getShed().getWidth());
        assertEquals(ShedPlacement.LEFT, updatedCarport.getShed().getShedPlacement());
    }

    @Test
    void updateCarportRemoveShed() throws DatabaseException, SQLException
    {
        Carport carport = carportMapper.getCarportById(1);
        carport.setShed(null);

        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            boolean updated = carportMapper.updateCarport(connection, carport);

            connection.commit();
            assertTrue(updated);
        }
        Carport updatedCarport = carportMapper.getCarportById(1);
        assertNull(updatedCarport.getShed());
    }

    @Test
    void testUpdateCarportNotFound() throws DatabaseException, SQLException
    {
        Carport carport = new Carport(999, 600, 500, RoofType.FLAT, null);

        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            boolean updated = carportMapper.updateCarport(connection, carport);

            connection.commit();
            assertFalse(updated);
        }
    }
}