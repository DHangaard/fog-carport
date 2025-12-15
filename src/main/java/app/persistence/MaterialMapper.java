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

    public Material createMaterial(Connection connection, String name, MaterialCategory materialCategory, MaterialType materialType, Integer materialWidth, Integer materialHeight, String unit, String usage) throws DatabaseException
    {
        String sql = """
               INSERT INTO material (name, category, type, material_width, material_height, unit, usage)
               VALUES (?, ?, ?, ?, ?, ?, ?)
               RETURNING material_id
               """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, materialCategory.name());
            ps.setString(3, materialType.name());

            if (materialWidth != null)
            {
                ps.setInt(4, materialWidth);
            }
            else
            {
                ps.setNull(4, Types.INTEGER);
            }

            if (materialHeight != null)
            {
                ps.setInt(5, materialHeight);
            }
            else
            {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setString(6, unit);
            ps.setString(7, usage);

            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return new Material(
                        rs.getInt("material_id"),
                        name,
                        materialCategory,
                        materialType,
                        materialWidth,
                        materialHeight,
                        unit,
                        usage
                );
            }

            throw new DatabaseException("Kunne ikke oprette materiale");

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved oprettelse af materiale: " + e.getMessage());
        }
    }

    public Material getMaterialById(int materialId) throws DatabaseException
    {
        String sql = """
               SELECT material_id, name, category, type, material_width, material_height, unit, usage
               FROM material
               WHERE material_id = ?
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

    public List<Material> getAllMaterials() throws DatabaseException
    {
        String sql = """
               SELECT material_id, name, category, type, material_width, material_height, unit, usage
               FROM material
               """;

        List<Material> materials = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                materials.add(buildMaterialFromResultSet(rs));
            }

            return materials;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materialer: " + e.getMessage());
        }
    }


    public List<Material> getMaterialsByType(MaterialType materialType) throws DatabaseException
    {
        String sql = """
               SELECT material_id, name, category, type, material_width, material_height, unit, usage
               FROM material
               WHERE type = ?
               """;

        List<Material> materials = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, materialType.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                materials.add(buildMaterialFromResultSet(rs));
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
               SELECT material_id, name, category, type, material_width, material_height, unit, usage
               FROM material
               WHERE category = ?
               ORDER BY type, name
               """;

        List<Material> materials = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, materialCategory.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                materials.add(buildMaterialFromResultSet(rs));
            }

            return materials;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke hente materialer med kategorien " + materialCategory.getDisplayCategory() + ": " + e.getMessage());
        }
    }

    public boolean updateMaterial(Connection connection, Material material) throws DatabaseException
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

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setString(1, material.getName());
            ps.setString(2, material.getCategory().name());
            ps.setString(3, material.getType().name());

            if (material.getMaterialWidth() != null) {
                ps.setInt(4, material.getMaterialWidth());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            if (material.getMaterialHeight() != null) {
                ps.setInt(5, material.getMaterialHeight());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setString(6, material.getUnit());
            ps.setString(7, material.getUsage());
            ps.setInt(8, material.getMaterialId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;

        } catch (SQLException e)
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
                rs.getString("usage")
        );
    }
}