package app.persistence;

import app.entities.Shed;
import app.enums.ShedPlacement;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShedMapper
{
    private ConnectionPool connectionPool;

    public ShedMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Shed createShed(Connection connection, int length, int width, ShedPlacement shedPlacement) throws DatabaseException
    {
        String sql = """
                INSERT INTO shed (length, width, shed_placement)
                VALUES (?, ?, ?)
                RETURNING shed_id;
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, length);
            ps.setInt(2, width);
            ps.setString(3, shedPlacement.name());

            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                int shedId = rs.getInt("shed_id");
                return new Shed(
                        shedId,
                        length,
                        width,
                        shedPlacement
                );
            }
            else
            {
                throw new DatabaseException("Kunne ikke oprette skur");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af skur: " + e.getMessage());
        }
    }

    public Shed getShedById(int shedId) throws DatabaseException
    {
        String sql = """
                SELECT * 
                FROM shed 
                WHERE shed_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, shedId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return new Shed(
                        rs.getInt("shed_id"),
                        rs.getInt("length"),
                        rs.getInt("width"),
                        ShedPlacement.valueOf(rs.getString("shed_placement"))
                );
            }
            else
            {
                throw new DatabaseException("Skur ikke fundet");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af skur: " + e.getMessage());
        }
    }

    public boolean updateShed(Shed shed) throws DatabaseException
    {
        String sql = """
                UPDATE shed 
                SET length = ?, 
                width = ?, 
                shed_placement = ?
                WHERE shed_id = ?
            """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, shed.getLength());
            ps.setInt(2, shed.getWidth());
            ps.setString(3, shed.getShedPlacement().name());
            ps.setInt(4, shed.getShedId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af skur: " + e.getMessage());
        }
    }
}
