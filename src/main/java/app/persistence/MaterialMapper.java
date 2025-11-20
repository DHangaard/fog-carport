package app.persistence;

import app.entities.Material;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;

import java.sql.*;

public class MaterialMapper
{
    private ConnectionPool connectionPool;

    public MaterialMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Material createMaterial(String name,
                                   MaterialCategory materialCategory,
                                   MaterialType materialType,
                                   Integer materialWidth,
                                   Integer materialHeight,
                                   String unit,
                                   String usage,
                                   Integer variantLength,
                                   double unitPrice) throws DatabaseException
    {
        String materialSql = """
                INSERT INTO material (name, category, type, material_width, material_height, unit, usage)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING material_id
                """;

        String materialVariantSql = """
                INSERT INTO material_variant (material_id, variant_length, unit_price) 
                VALUES (?, ?, ?)
                RETURNING material_variant_id
                """;
        Connection connection = null;
        int materialId = 0;
        int materialVariantId = 0;

        try
        {
            connection = connectionPool.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(materialSql))
            {
                ps.setString(1, name);
                ps.setString(2, materialCategory.name());
                ps.setString(3, materialType.name());
                ps.setObject(4, materialWidth);
                ps.setObject(5, materialHeight);
                ps.setString(6, unit);
                ps.setString(7, usage);

                ResultSet rs = ps.executeQuery();
                if (rs.next())
                {
                    materialId = rs.getInt("material_id");
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(materialVariantSql))
            {
                ps.setInt(1, materialId);
                ps.setObject(2, variantLength);
                ps.setDouble(3, unitPrice);

                ResultSet rs = ps.executeQuery();
                if (rs.next())
                {
                    materialVariantId = rs.getInt("material_variant_id");
                }
            }

            connection.commit();

            return new Material(
                    materialId,
                    name,
                    materialCategory,
                    materialType,
                    materialWidth,
                    materialHeight,
                    unit,
                    usage,
                    materialVariantId,
                    variantLength,
                    unitPrice
                    );
        }
        catch (SQLException e)
        {
            if (connection != null)
            {
                try
                {
                    connection.rollback();
                }
                catch (SQLException rollbackEx)
                {
                    throw new DatabaseException("Fejl ved oprettelse af materiale: " + rollbackEx.getMessage());
                }
            }
            throw new DatabaseException("Fejl ved oprettelse af materiale: " + e.getMessage());
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.setAutoCommit(true);
                    connection.close();
                }
                catch (SQLException e)
                {
                    throw new DatabaseException("Fejl ved oprettelse af materiale: " + e.getMessage());
                }
            }
        }
    }

    private Material buildMaterialFromResultSet(ResultSet rs) throws SQLException
    {
        return new Material(
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
    }
}