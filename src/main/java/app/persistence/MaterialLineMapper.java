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

public class MaterialLineMapper
{
    private ConnectionPool connectionPool;

    public MaterialLineMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public MaterialLine createMaterialLine(MaterialLine materialLine) throws DatabaseException
    {
        String sql = """
                INSERT INTO material_line (bom_id, material_id, quantity, line_total)
                VALUES  (?, ?, ?, ?)
                RETURNING material_line_id
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialLine.getBomId());
            ps.setInt(2, materialLine.getMaterial().getMaterialId());
            ps.setInt(3, materialLine.getQuantity());
            ps.setDouble(4, materialLine.getLineTotal());

            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                int materialLineId = rs.getInt(1);
                return getMaterialLineById(materialLineId);
            }

            throw new DatabaseException("Kunne ikke oprette ordrelinje");
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af materialelinje" + e.getMessage());
        }
    }

    // TODO Make this private
    public MaterialLine getMaterialLineById(int materialLineId) throws DatabaseException
    {
        String sql = """
                SELECT ml.*, m.*
                FROM material_line ml
                JOIN material m
                ON ml.material_id = m.material_id 
                WHERE material_line_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialLineId);
            // TODO Finish method
        }
        return null;
    }

    public MaterialLine getMaterialLinesByBomId(int bomId) throws DatabaseException
    {

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
