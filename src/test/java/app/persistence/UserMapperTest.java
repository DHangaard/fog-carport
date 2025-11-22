package app.persistence;

import app.entities.User;
import app.enums.Role;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest
{

    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "carport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static UserMapper userMapper;

    @BeforeAll
    static void setupClass()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DROP TABLE IF EXISTS test.users CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.zip_code CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.users_user_id_seq CASCADE");

                stmt.execute("CREATE TABLE test.zip_code AS (SELECT * FROM public.zip_code) WITH NO DATA");
                stmt.execute("ALTER TABLE test.zip_code ADD PRIMARY KEY (zip_code)");
                stmt.execute("CREATE TABLE test.users AS (SELECT * FROM public.users) WITH NO DATA");

                stmt.execute("CREATE SEQUENCE test.users_user_id_seq");
                stmt.execute("ALTER TABLE test.users ALTER COLUMN user_id SET DEFAULT nextval('test.users_user_id_seq')");

                stmt.execute("ALTER TABLE test.users ADD CONSTRAINT users_email_key UNIQUE (email)");
                stmt.execute("ALTER TABLE test.users ADD CONSTRAINT users_zip_code_fk " +
                        "FOREIGN KEY (zip_code) REFERENCES test.zip_code (zip_code)");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }

        userMapper = new UserMapper(connectionPool);
    }

    @BeforeEach
    void setUp()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DELETE FROM test.users");
                stmt.execute("DELETE FROM test.zip_code");

                stmt.execute("INSERT INTO test.zip_code (zip_code, city) VALUES " +
                        "(2800, 'Kongens Lyngby'), " +
                        "(2900, 'Hellerup'), " +
                        "(1000, 'København K')");

                stmt.execute("INSERT INTO test.users (user_id, first_name, last_name, email, hashed_password, " +
                        "zip_code, street, role, phone_number) VALUES " +
                        "(1, 'John', 'Jensen', 'john@gmail.com', '$2a$10$hashedpassword1', " +
                        "2800, 'Testvej 1', 'CUSTOMER', '12345678'), " +
                        "(2, 'Anna', 'Nielsen', 'anna@fog.com', '$2a$10$hashedpassword2', " +
                        "2900, 'Strandvej 5', 'SALESREP', '87654321')");

                stmt.execute("SELECT setval('test.users_user_id_seq', COALESCE((SELECT MAX(user_id) + 1 FROM test.users), 1), false)");
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
    void testCreateUser() throws DatabaseException
    {
        String firstName = "Peter";
        String lastName = "Hansen";
        String email = "peter@gmail.com";
        String hashedPassword = "$2a$10$newhashedpassword";
        String phoneNumber = "11223344";
        String street = "Nygade 10";
        int zipcode = 1000;

        User user = userMapper.createUser(firstName, lastName, email, hashedPassword,
                phoneNumber, street, zipcode);

        assertNotNull(user);
        assertTrue(user.getUserId() ==  3);
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(email.toLowerCase(), user.getEmail());
        assertEquals(phoneNumber, user.getPhoneNumber());
        assertEquals(street, user.getStreet());
        assertEquals(zipcode, user.getZipCode());
        assertEquals("København K", user.getCity());
        assertEquals(Role.CUSTOMER, user.getRole());
    }

    @Test
    void testCreateUserWithDuplicateEmail()
    {
        String email = "john@gmail.com";

        DatabaseException exception = assertThrows(DatabaseException.class, () ->
        {
            userMapper.createUser("Test", "User", email, "password",
                    "12345678", "Street 1", 2800);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void testGetUserById() throws DatabaseException
    {
        User user = userMapper.getUserById(1);

        assertNotNull(user);
        assertEquals(1, user.getUserId());
        assertEquals("John", user.getFirstName());
        assertEquals("Jensen", user.getLastName());
        assertEquals("john@gmail.com", user.getEmail());
        assertEquals("12345678", user.getPhoneNumber());
        assertEquals("Testvej 1", user.getStreet());
        assertEquals(2800, user.getZipCode());
        assertEquals("Kongens Lyngby", user.getCity());
        assertEquals(Role.CUSTOMER, user.getRole());
    }

    @Test
    void testUserWithNonExistingId()
    {
        assertThrows(DatabaseException.class, () -> {
            userMapper.getUserById(999);
        });
    }

    @Test
    void testGetUserByEmail() throws DatabaseException
    {

        User user = userMapper.getUserByEmail("anna@fog.com");

        assertNotNull(user);
        assertEquals(2, user.getUserId());
        assertEquals("Anna", user.getFirstName());
        assertEquals("Nielsen", user.getLastName());
        assertEquals("anna@fog.com", user.getEmail());
        assertEquals(Role.SALESREP, user.getRole());
    }

    @Test
    void testGetUserByEmailIsNotCaseInsensitive() throws DatabaseException
    {

        User user1 = userMapper.getUserByEmail("JOHN@GMAIL.COM");
        User user2 = userMapper.getUserByEmail("john@gmail.com");

        assertNotNull(user1);
        assertNotNull(user2);
        assertEquals(user1.getUserId(), user2.getUserId());
    }

    @Test
    void testGetUserByEmailNotFound()
    {

        assertThrows(DatabaseException.class, () -> {
            userMapper.getUserByEmail("notfound@example.com");
        });
    }


    @Test
    void testDeleteUser() throws DatabaseException
    {
        int userId = 1;

        boolean deleted = userMapper.deleteUser(userId);

        assertTrue(deleted);
        assertThrows(DatabaseException.class, () -> {
            userMapper.getUserById(userId);
        });
    }

    @Test
    void testGetAllUsers() throws DatabaseException
    {
        List<User> users = userMapper.getAllUsers();

        assertNotNull(users);
        assertEquals(2, users.size());
    }
}