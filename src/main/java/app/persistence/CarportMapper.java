package app.persistence;

import app.entities.Carport;
import app.entities.Shed;
import app.enums.RoofType;
import app.enums.ShedPlacement;
import app.exceptions.DatabaseException;

import java.sql.*;

public class CarportMapper
{
    private ConnectionPool connectionPool;
    private ShedMapper shedMapper;

    public CarportMapper (ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
        this.shedMapper = new ShedMapper(connectionPool);
    }

    public Carport createCarport(Carport carport) throws DatabaseException
    {
        Integer shedId = null;

        if (carport.getShed() != null)
        {
            Shed createdShed = shedMapper.createShed(carport.getShed());
            shedId = createdShed.getShedId();
        }

        String sql  = """
                INSERT INTO carport (length, width, shed_id, roof_type)
                VALUES (?, ?, ?, ?)
                RETURNING carport_id;
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, carport.getLength());
            ps.setInt(2, carport.getWidth());

            if (shedId != null)
            {
                ps.setInt(3, shedId);
            }
            else
            {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setString(4, carport.getRoofType().name());

            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                int carportId = rs.getInt("carport_id");
                return getCarportById(carportId);
            }
            else
            {
                throw new DatabaseException("Kunne ikke oprette carport");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af carport: " + e.getMessage());
        }
    }

    public Carport getCarportById(int carportId) throws DatabaseException
    {
        String sql = """
                SELECT c.carport_id, c.length, c.width, c.shed_id, c.roof_type,
                       s.shed_id, s.length AS shed_length, s.width AS shed_width, s.shed_placement
                FROM carport c
                LEFT JOIN shed s ON c.shed_id = s.shed_id
                WHERE c.carport_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, carportId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return buildCarportFromResultSet(rs);
            }
            else
            {
                throw new DatabaseException("Carport ikke fundet med id: " + carportId);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af carport: " + e.getMessage());
        }
    }

    public boolean updateCarport(Carport carport) throws DatabaseException
    {
        Integer shedId = null;


        if (carport.getShed() != null)
        {
            if (carport.getShed().getShedId() > 0)
            {
                shedMapper.updateShed(carport.getShed());
                shedId = carport.getShed().getShedId();
            }
            else
            {
                Shed createdShed = shedMapper.createShed(carport.getShed());
                shedId = createdShed.getShedId();
            }
        }

        String sql = """
                UPDATE carport 
                SET length = ?, 
                width = ?, 
                shed_id = ?, 
                roof_type = ?
                WHERE carport_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, carport.getLength());
            ps.setInt(2, carport.getWidth());

            if (shedId != null)
            {
                ps.setInt(3, shedId);
            }
            else
            {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setString(4, carport.getRoofType().name());
            ps.setInt(5, carport.getCarportId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af carport: " + e.getMessage());
        }
    }


    private Carport buildCarportFromResultSet(ResultSet rs) throws SQLException
    {
        Shed shed = null;
        Integer shedId = (Integer) rs.getObject("shed_id");

        if (shedId != null)
        {
            shed = new Shed(
                    shedId,
                    rs.getInt("shed_length"),
                    rs.getInt("shed_width"),
                    ShedPlacement.valueOf(rs.getString("shed_placement"))
            );
        }

        return new Carport(
                rs.getInt("carport_id"),
                rs.getInt("length"),
                rs.getInt("width"),
                RoofType.valueOf(rs.getString("roof_type")),
                shed
        );
    }
}
