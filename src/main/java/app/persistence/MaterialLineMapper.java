package app.persistence;

import app.entities.Material;
import app.entities.MaterialLine;
import app.entities.MaterialVariant;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;

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

    public MaterialLine createMaterialLine(Connection connection, int orderId, int materialVariantId, int quantity) throws DatabaseException
    {
        String sql = """
            INSERT INTO material_line (order_id, material_variant_id, quantity)
            VALUES (?, ?, ?)
            RETURNING material_line_id
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            ps.setInt(2, materialVariantId);
            ps.setInt(3, quantity);

            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                int materialLineId = rs.getInt(1);

                return new MaterialLine(
                        materialLineId,
                        orderId,
                        null,
                        quantity
                );
            }

            throw new DatabaseException("Kunne ikke oprette ordrelinje");
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af materialelinje: " + e.getMessage());
        }
    }

    public MaterialLine getMaterialLineById(int materialLineId) throws DatabaseException
    {
        String sql = """
            SELECT 
                ml.material_line_id, ml.order_id, ml.material_variant_id, ml.quantity, mv.material_variant_id AS mv_variant_id,
                mv.material_id AS mv_material_id,
                mv.variant_length AS mv_variant_length,
                mv.unit_price AS mv_unit_price,
                
                m.material_id AS m_material_id,
                m.name AS m_name,
                m.category AS m_category,
                m.type AS m_type,
                m.material_width AS m_material_width,
                m.material_height AS m_material_height,
                m.unit AS m_unit,
                m.usage AS m_usage
                
            FROM material_line ml
            JOIN material_variant mv ON ml.material_variant_id = mv.material_variant_id
            JOIN material m ON mv.material_id = m.material_id
            WHERE ml.material_line_id = ?
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

            throw new DatabaseException("Materialelinje med ID " + materialLineId + " blev ikke fundet i databasen.");
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materialelinje: " + e.getMessage());
        }
    }

    public List<MaterialLine> getMaterialLinesByOrderId(int orderId) throws DatabaseException
    {
        String sql = """
            SELECT 
                ml.material_line_id,
                ml.order_id,
                ml.material_variant_id,
                ml.quantity,
                
                mv.material_variant_id AS mv_variant_id,
                mv.material_id AS mv_material_id,
                mv.variant_length AS mv_variant_length,
                mv.unit_price AS mv_unit_price,
                
                m.material_id AS m_material_id,
                m.name AS m_name,
                m.category AS m_category,
                m.type AS m_type,
                m.material_width AS m_material_width,
                m.material_height AS m_material_height,
                m.unit AS m_unit,
                m.usage AS m_usage
                
            FROM material_line ml
            JOIN material_variant mv ON ml.material_variant_id = mv.material_variant_id
            JOIN material m ON mv.material_id = m.material_id
            WHERE ml.order_id = ?
            """;

        List<MaterialLine> materialLines = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
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
            throw new DatabaseException("Fejl ved hentning af materialelinjer: " + e.getMessage());
        }
    }

    public boolean updateMaterialLine(Connection connection, MaterialLine materialLine) throws DatabaseException
    {
        String sql = """
                UPDATE material_line
                SET material_variant_id = ?, quantity = ?
                WHERE material_line_id = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialLine.getMaterialVariant().getMaterialVariantId());
            ps.setInt(2, materialLine.getQuantity());
            ps.setInt(3, materialLine.getMaterialLineId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af materialelinje: " + e.getMessage());
        }
    }

    public boolean deleteMaterialLine(int materialLineId) throws DatabaseException
    {
        String sql = """
                DELETE FROM material_line
                WHERE material_line_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialLineId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af materialeline: " + e.getMessage());
        }
    }

    private MaterialLine buildMaterialLineFromResultSet(ResultSet rs) throws SQLException
    {
        Material material = new Material(
                rs.getInt("m_material_id"),
                rs.getString("m_name"),
                MaterialCategory.valueOf(rs.getString("m_category")),
                MaterialType.valueOf(rs.getString("m_type")),
                (Integer) rs.getObject("m_material_width"),
                (Integer) rs.getObject("m_material_height"),
                rs.getString("m_unit"),
                rs.getString("m_usage")
        );

        MaterialVariant materialVariant = new MaterialVariant(
                rs.getInt("mv_variant_id"),
                rs.getInt("mv_material_id"),
                (Integer) rs.getObject("mv_variant_length"),
                rs.getDouble("mv_unit_price"),
                material
        );

        return new MaterialLine(
                rs.getInt("material_line_id"),
                rs.getInt("order_id"),
                materialVariant,
                rs.getInt("quantity")
        );
    }
}
