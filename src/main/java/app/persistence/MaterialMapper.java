package app.persistence;

import app.entities.Material;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

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
                    materialId = rs.getInt(1);
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
                    materialVariantId = rs.getInt(1);
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

    public Material createMaterialVariant(int materialId, Integer variantLength, double unitPrice) throws DatabaseException
    {
        String materialVariantSql = """
                INSERT INTO material_variant (material_id, variant_length, unit_price) 
                VALUES (?, ?, ?)
                RETURNING material_variant_id
                """;

        Material material = getMaterialById(materialId);

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(materialVariantSql))
        {
            ps.setInt(1, materialId);
            ps.setObject(2, variantLength);
            ps.setDouble(3, unitPrice);

            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                int materialVariantId = rs.getInt(1);

                return new Material(
                        material.getMaterialId(),
                        material.getName(),
                        material.getMaterialCategory(),
                        material.getMaterialType(),
                        material.getMaterialWidth(),
                        material.getMaterialHeight(),
                        material.getUnit(),
                        material.getUsage(),
                        materialVariantId,
                        variantLength,
                        unitPrice
                );
            }
            throw new DatabaseException("Fejl ved oprettelse af materiale variant.");
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af materiale variant: " + e.getMessage());
        }
    }

    public Material getMaterialById(int materialId) throws DatabaseException
    {
        String sql = """
                SELECT m.material_id, m.name, m.category, m.type, m.material_width, m.material_height, 
                       m.unit, m.usage, mv.material_variant_id, mv.variant_length, mv.unit_price
                FROM material m
                JOIN material_variant mv ON m.material_id = mv.material_id
                WHERE m.material_id = ?
                ORDER BY mv.material_variant_id
                LIMIT 1
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return buildMaterialFromResultSet(rs);
            }
            else
            {
                throw new DatabaseException("Materiale med ID " + materialId + " blev ikke fundet i databasen.");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materiale: " + e.getMessage());
        }
    }

    public Material getMaterialById(int materialId, int materialVariantId) throws DatabaseException
    {
        String sql = """
                SELECT m.material_id, m.name, m.category, m.type, m.material_width, m.material_height, 
                       m.unit, m.usage, mv.material_variant_id, mv.variant_length, mv.unit_price
                FROM material m
                JOIN material_variant mv ON m.material_id = mv.material_id
                WHERE m.material_id = ? AND mv.material_variant_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialId);
            ps.setInt(2, materialVariantId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return buildMaterialFromResultSet(rs);
            }
            else
            {
                throw new DatabaseException("Materiale med ID " + materialId + " blev ikke fundet i databasen.");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materiale: " + e.getMessage());
        }
    }

    public List<Material> getMaterialsByType(MaterialType materialType) throws DatabaseException
    {
        String sql = """
                SELECT m.material_id, m.name, m.category, m.type, m.material_width, m.material_height, 
                       m.unit, m.usage, mv.material_variant_id, mv.variant_length, mv.unit_price
                FROM material m
                JOIN material_variant mv ON m.material_id = mv.material_id
                WHERE m.type = ?
                """;

        List<Material> materials = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, materialType.name());
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    materials.add(buildMaterialFromResultSet(rs));
                }
            }
            return materials;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke hente materialer med typen " + materialType.getDisplayCategory() + ": " + e.getMessage());
        }
    }

    public List<Material> getMaterialsByCategory(MaterialCategory materialCategory) throws DatabaseException
    {
        String sql = """
                SELECT m.material_id, m.name, m.category, m.type, m.material_width, m.material_height, 
                       m.unit, m.usage, mv.material_variant_id, mv.variant_length, mv.unit_price
                FROM material m
                JOIN material_variant mv ON m.material_id = mv.material_id
                WHERE m.category = ?
                """;

        List<Material> materials = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, materialCategory.name());
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    materials.add(buildMaterialFromResultSet(rs));
                }
            }
            return materials;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke hente materialer med kategorien " + materialCategory.getDisplayCategory() + ": " + e.getMessage());
        }
    }

    public List<Material> getAllMaterials() throws DatabaseException
    {
        String sql = """
                SELECT m.material_id, m.name, m.category, m.type, m.material_width, m.material_height, 
                       m.unit, m.usage, mv.material_variant_id, mv.variant_length, mv.unit_price
                FROM material m
                JOIN material_variant mv ON m.material_id = mv.material_id
                """;

        List<Material> materials = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    materials.add(buildMaterialFromResultSet(rs));
                }
            }
            return materials;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke hente alle materialer fra databasen: " + e.getMessage());
        }
    }

    public boolean updateMaterial(Material material) throws DatabaseException
    {
        String sql = """
                UPDATE material
                SET name = ?,
                category = ?,
                type = ?,
                material_width = ?,
                material_height = ?,
                unit = ?,
                usage = ?
                WHERE material_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, material.getName());
            ps.setString(2, material.getMaterialCategory().name());
            ps.setString(3, material.getMaterialType().name());
            ps.setInt(4, material.getMaterialWidth());
            ps.setInt(5, material.getMaterialHeight());
            ps.setString(6, material.getUnit());
            ps.setString(7, material.getUsage());
            ps.setInt(8, material.getMaterialId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af materiale: " + e.getMessage());
        }
    }

    public boolean updateMaterialVariant(Material material) throws DatabaseException
    {
        String sql = """
                UPDATE material
                SET variant_length = ?,
                unit_price = ?
                WHERE material_variant_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, material.getVariantLength());
            ps.setDouble(2, material.getUnitPrice());
            ps.setInt(3, material.getMaterialVariantId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af materiale: " + e.getMessage());
        }
    }

    public boolean deleteMaterial(int materialId) throws DatabaseException
    {
        String sql = """
                DELETE FROM material
                WHERE material_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af materiale med id: " + materialId + e.getMessage());
        }
    }

    public boolean deleteMaterialVariant(int materialVariantId) throws DatabaseException
    {
        String sql = """
                DELETE FROM material_variant
                WHERE material_variant_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialVariantId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af materiale variant med id: " + materialVariantId + e.getMessage());
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