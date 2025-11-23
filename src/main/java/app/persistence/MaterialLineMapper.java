package app.persistence;

import app.entities.Material;
import app.entities.MaterialLine;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MaterialLineMapper
{
    private ConnectionPool connectionPool;

    public MaterialLineMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public MaterialLine createMaterialLine(Connection connection, int bomId, int materialId, int quantity, double lineTotal) throws DatabaseException
    {
        String sql = """
                INSERT INTO material_line (bom_id, material_id, quantity, line_total)
                VALUES  (?, ?, ?, ?)
                RETURNING material_line_id
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, bomId);
            ps.setInt(2, materialId);
            ps.setInt(3, quantity);
            ps.setDouble(4, lineTotal);

            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                int materialLineId = rs.getInt(1);
                return new MaterialLine(materialLineId, bomId, null, quantity, lineTotal);
            }

            throw new DatabaseException("Kunne ikke oprette ordrelinje");
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af materialelinje" + e.getMessage());
        }
    }

    public MaterialLine getMaterialLineById(int materialLineId) throws DatabaseException
    {
        String sql = """
                SELECT ml.material_line_id, ml.bom_id, ml.quantity, ml.line_total, m.*
                FROM material_line ml
                JOIN material m
                ON ml.material_id = m.material_id 
                WHERE material_line_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialLineId);
            ResultSet rs = ps.executeQuery();;

            if (rs.next())
            {
                return buildMaterialLineFromResultSet(rs);
            }

            throw new DatabaseException("Materialelinje med ID " materialLineId + " blev ikke fundet i databasen.");
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materialelinje med ID " + materialLineId + e.getMessage());
        }
    }

    public List<MaterialLine> getMaterialLinesByBomId(int bomId) throws DatabaseException
    {
        String sql = """
                SELECT ml.material_line_id, ml.bom_id, ml.quantity, ml.line_total, m.*
                FROM material_line ml
                JOIN material m
                ON ml.material_id = m.material_id 
                WHERE bom_id = ?
                """;

        List<MaterialLine> materialLines = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, bomId);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    materialLines.add(buildMaterialLineFromResultSet(rs));
                }
            }
            return materialLines;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materialelinjer" + e.getMessage());
        }
    }

    public boolean updateMaterialLine(MaterialLine materialLineId) throws DatabaseException
    {
        return false;
    }

    public boolean deleteMaterialLine(int materialLineId) throws DatabaseException
    {
        return false;
    }

    private MaterialLine buildMaterialLineFromResultSet(ResultSet rs) throws SQLException
    {
        Material material = new Material(
            rs.getInt("material_id"),
            rs.getString("name"),
            MaterialCategory.valueOf(rs.getString("category")),
            MaterialType.valueOf(rs.getString("type")),
            (Integer) rs.getObject("material_width"),
            (Integer) rs.getObject("material_height"),
            rs.getString("unit"),
            rs.getString("usage"),
            rs.getInt("material_variant_id"),
            (Integer) rs.getObject("variant_length"),
            rs.getDouble("unit_price")
    );
        return new MaterialLine(rs.getInt("material_line_id"),
                rs.getInt("bom_id"),
                material,
                rs.getInt("quantity"),
                rs.getDouble("line_total"));
    }
}
