package app.persistence;

import app.entities.User;
import app.enums.Role;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserMapper
{
    private ConnectionPool connectionPool;

    public UserMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public User createUser(String firstName, String lastName, String email, String hashedPassword, String phoneNumber, String street, int zipcode) throws DatabaseException
    {
        String sql = """
                INSERT INTO users (first_name, last_name, email, hashed_password, phone_number, street, zip_code, role)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?) 
                RETURNING user_id
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email.toLowerCase());
            ps.setString(4, hashedPassword);
            ps.setString(5, phoneNumber);
            ps.setString(6, street);
            ps.setInt(7, zipcode);
            ps.setString(8, "CUSTOMER");

            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                int userId = rs.getInt(1);
                return getUserById(userId);
            }

            throw new DatabaseException("Kunne ikke oprette bruger");
        }
        catch (SQLException e)
        {
            if (e.getMessage().toLowerCase().contains("duplicate") || e.getMessage().toLowerCase().contains("email_unique"))
            {
                throw new DatabaseException("Email findes allerede, vælg en anden eller log ind");
            }
            else
            {
                throw new DatabaseException("Fejl ved oprettelse af bruger: " + e.getMessage());
            }
        }
    }

    public User getUserById(int userId) throws DatabaseException
    {
        String sql = """
                SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone_number, u.hashed_password, u.street, u.zip_code, z.city, u.role
                FROM users u
                JOIN zip_code z ON u.zip_code = z.zip_code
                WHERE u.user_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return buildUserFromResultSet(rs);
            }
            else
            {
                throw new DatabaseException("Der blev ikke fundet en bruger med id: " + userId);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af bruger: " + e.getMessage());
        }
    }

    public User getUserByEmail(String email) throws DatabaseException
    {
        String sql = """
                SELECT * 
                FROM users u 
                JOIN zip_code z ON u.zip_code = z.zip_code 
                WHERE email = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, email.toLowerCase());
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return buildUserFromResultSet(rs);
            }
            else
            {
                throw new DatabaseException("Bruger ikke fundet med email: " + email);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af bruger: " + e.getMessage());
        }
    }

    public boolean deleteUser(int userId) throws DatabaseException
    {
        String sql = """
                DELETE 
                FROM users 
                WHERE user_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af bruger med id: " + userId);
        }
    }

    public List<User> getAllUsers() throws DatabaseException
    {
        String sql = """
                SELECT users.*, zip_code.city 
                FROM users 
                JOIN zip_code 
                ON users.zip_code = zip_code.zip_code
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                users.add(
                        buildUserFromResultSet(rs)
                );
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl under hentning af alle brugere");
        }
        return users;
    }

    private User buildUserFromResultSet(ResultSet rs) throws SQLException
    {
        return new User(
                rs.getInt("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("hashed_password"),
                rs.getString("street"),
                rs.getInt("zip_code"),
                rs.getString("city"),
                rs.getString("email"),
                rs.getString("phone_number"),
                Role.valueOf(rs.getString("role"))
        );
    }
}
