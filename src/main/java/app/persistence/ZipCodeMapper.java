package app.persistence;

import app.exceptions.DatabaseException;
import java.sql.*;

public class ZipCodeMapper
{
    private ConnectionPool connectionPool;

    public ZipCodeMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public boolean zipCodeExists(int zipCode) throws DatabaseException
    {
        String sql = "SELECT FROM zip_code WHERE zip_code = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, zipCode);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af postnummer");
        }
    }

    public String getCityByZipCode(int zipCode) throws DatabaseException
    {
        String sql = "SELECT city FROM zip_code WHERE zip_code = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, zipCode);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return rs.getString("city");
            }
            else
            {
                return null;
            }

        } catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af by");
        }
    }
}