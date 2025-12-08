package app.persistence;

import app.entities.Material;
import app.entities.MaterialVariant;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialVariantMapper
{
    private ConnectionPool connectionPool;

    public MaterialVariantMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public MaterialVariant createMaterialVariant(Connection connection, int materialId, Integer variantLength, double unitPrice, Integer piecesPerUnit) throws DatabaseException
    {
        String sql = """
                INSERT INTO material_variant (material_id, variant_length, unit_price, pieces_per_unit)
                VALUES (?, ?, ?, ?)
                RETURNING material_variant_id
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialId);

            if (variantLength != null)
            {
                ps.setInt(2, variantLength);
            }
            else
            {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setDouble(3, unitPrice);

            if (piecesPerUnit != null)
            {
                ps.setInt(4, piecesPerUnit);
            }
            else
            {
                ps.setNull(4, Types.INTEGER);
            }

            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return new MaterialVariant(
                        rs.getInt("material_variant_id"),
                        materialId,
                        variantLength,
                        unitPrice,
                        piecesPerUnit,
                        null
                );
            }

            throw new DatabaseException("Kunne ikke oprette materialvariant");

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved oprettelse af materialvariant: " + e.getMessage());
        }
    }

    public MaterialVariant getVariantWithMaterialById(int variantId) throws DatabaseException {

        String sql = """
            SELECT 
            mv.material_variant_id,
            mv.material_id,
            mv.variant_length,
            mv.unit_price,
            mv.pieces_per_unit,
             
            m.material_id AS m_material_id,
            m.name AS m_name,
            m.category AS m_category,
            m.type AS m_type,
            m.material_width AS m_material_width,
            m.material_height AS m_material_height,
            m.unit AS m_unit,
            m.usage AS m_usage
                
            FROM material_variant mv
            JOIN material m ON mv.material_id = m.material_id
            WHERE mv.material_variant_id = ?
            """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, variantId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return buildMaterialVariantFromResultSet(rs);
            }

            throw new DatabaseException("MaterialVariant med ID " + variantId + " blev ikke fundet");

        } catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materialvariant: " + e.getMessage());
        }
    }

    public List<MaterialVariant> getAllMaterialVariantsWithMaterialByMaterialId(int materialId)
            throws DatabaseException {

        String sql = """
            SELECT 
            mv.material_variant_id,
            mv.material_id,
            mv.variant_length,
            mv.unit_price,
            mv.pieces_per_unit,
                
            m.material_id AS m_material_id,
            m.name AS m_name,
            m.category AS m_category,
            m.type AS m_type,
            m.material_width AS m_material_width,
            m.material_height AS m_material_height,
            m.unit AS m_unit,
            m.usage AS m_usage
                
            FROM material_variant mv
            JOIN material m ON mv.material_id = m.material_id
            WHERE mv.material_id = ?
            ORDER BY mv.variant_length NULLS FIRST
            """;

        List<MaterialVariant> variants = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, materialId);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                variants.add(buildMaterialVariantFromResultSet(rs));
            }

            return variants;

        } catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materialvarianter: " + e.getMessage());
        }
    }

    public List<MaterialVariant> getAllVariantsByType(MaterialType materialType) throws DatabaseException
    {
        String sql = """
            SELECT 
            mv.material_variant_id,
            mv.material_id,
            mv.variant_length,
            mv.unit_price,
            mv.pieces_per_unit,
                
            m.material_id AS m_material_id,
            m.name AS m_name,
            m.category AS m_category,
            m.type AS m_type,
            m.material_width AS m_material_width,
            m.material_height AS m_material_height,
            m.unit AS m_unit,
            m.usage AS m_usage
                
            FROM material_variant mv
            JOIN material m ON mv.material_id = m.material_id
            WHERE m.type = ?
            """;

        List<MaterialVariant> variants = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setString(1, materialType.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                variants.add(buildMaterialVariantFromResultSet(rs));
            }

            return variants;

        } catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materialvarianter efter kategori: " + e.getMessage());
        }
    }

    public List<MaterialVariant> getAllMaterialVariants() throws DatabaseException
    {
        String sql = """
            SELECT 
            mv.material_variant_id,
            mv.material_id,
            mv.variant_length,
            mv.unit_price,
            mv.pieces_per_unit,
                
            m.material_id AS m_material_id,
            m.name AS m_name,
            m.category AS m_category,
            m.type AS m_type,
            m.material_width AS m_material_width,
            m.material_height AS m_material_height,
            m.unit AS m_unit,
            m.usage AS m_usage
                
            FROM material_variant mv
            JOIN material m ON mv.material_id = m.material_id
            """;

        List<MaterialVariant> variants = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                variants.add(buildMaterialVariantFromResultSet(rs));
            }

            return variants;

        } catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af alle materialvarianter " + e.getMessage());
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

    public boolean updateMaterialVariant(Connection connection, MaterialVariant materialVariant) throws DatabaseException
    {
        String sql = """
                UPDATE material_variant
                SET variant_length = ?,
                unit_price = ?,
                pieces_per_unit = ?
                WHERE material_variant_id = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {
            if (materialVariant.getVariantLength() != null)
            {
                ps.setInt(1, materialVariant.getVariantLength());
            }
            else
            {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setDouble(2, materialVariant.getUnitPrice());

            if (materialVariant.getPiecesPerUnit() != null)
            {
                ps.setInt(3, materialVariant.getPiecesPerUnit());
            }
            else
            {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setInt(4, materialVariant.getMaterialVariantId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af materiale: " + e.getMessage());
        }
    }

    public List<MaterialVariant> searchById(int materialVariantId) throws DatabaseException
    {
        String sql = """
            SELECT 
            mv.material_variant_id,
            mv.material_id,
            mv.variant_length,
            mv.unit_price,
            mv.pieces_per_unit,
                
            m.material_id AS m_material_id,
            m.name AS m_name,
            m.category AS m_category,
            m.type AS m_type,
            m.material_width AS m_material_width,
            m.material_height AS m_material_height,
            m.unit AS m_unit,
            m.usage AS m_usage
                
            FROM material_variant mv
            JOIN material m ON mv.material_id = m.material_id
            WHERE mv.material_id = ?
            """;

        List<MaterialVariant> variants = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, materialVariantId);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                variants.add(buildMaterialVariantFromResultSet(rs));
            }

            return variants;

        } catch (SQLException e)
        {
            throw new DatabaseException("Database fejl ved søgning efter ID: " + e.getMessage());
        }
    }

    public List<MaterialVariant> searchByName(String name) throws DatabaseException
    {
        String sql = """
            SELECT 
            mv.material_variant_id,
            mv.material_id,
            mv.variant_length,
            mv.unit_price,
            mv.pieces_per_unit,
                
            m.material_id AS m_material_id,
            m.name AS m_name,
            m.category AS m_category,
            m.type AS m_type,
            m.material_width AS m_material_width,
            m.material_height AS m_material_height,
            m.unit AS m_unit,
            m.usage AS m_usage
                
            FROM material_variant mv
            JOIN material m ON mv.material_id = m.material_id
            WHERE m.name 
            LIKE = ?
            """;

        List<MaterialVariant> variants = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                variants.add(buildMaterialVariantFromResultSet(rs));
            }

            return variants;

        } catch (SQLException e)
        {
            throw new DatabaseException("Database fejl ved søgning efter navn: " + e.getMessage());
        }
    }

    public List<MaterialVariant> searchByCategory(String category) throws DatabaseException
    {
        String sql = """
            SELECT 
            mv.material_variant_id,
            mv.material_id,
            mv.variant_length,
            mv.unit_price,
            mv.pieces_per_unit,
                
            m.material_id AS m_material_id,
            m.name AS m_name,
            m.category AS m_category,
            m.type AS m_type,
            m.material_width AS m_material_width,
            m.material_height AS m_material_height,
            m.unit AS m_unit,
            m.usage AS m_usage
                
            FROM material_variant mv
            JOIN material m ON mv.material_id = m.material_id
            WHERE m.category 
            LIKE = ?
            """;

        List<MaterialVariant> variants = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setString(1, "%" + category + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                variants.add(buildMaterialVariantFromResultSet(rs));
            }

            return variants;

        } catch (SQLException e)
        {
            throw new DatabaseException("Database fejl ved søgning efter kategori: " + e.getMessage());
        }
    }

    public List<MaterialVariant> searchByType(String type) throws DatabaseException
    {
        String sql = """
            SELECT 
            mv.material_variant_id,
            mv.material_id,
            mv.variant_length,
            mv.unit_price,
            mv.pieces_per_unit,
                
            m.material_id AS m_material_id,
            m.name AS m_name,
            m.category AS m_category,
            m.type AS m_type,
            m.material_width AS m_material_width,
            m.material_height AS m_material_height,
            m.unit AS m_unit,
            m.usage AS m_usage
                
            FROM material_variant mv
            JOIN material m ON mv.material_id = m.material_id
            WHERE m.type 
            LIKE = ?
            """;

        List<MaterialVariant> variants = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setString(1, "%" + type + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                variants.add(buildMaterialVariantFromResultSet(rs));
            }

            return variants;

        } catch (SQLException e)
        {
            throw new DatabaseException("Database fejl ved søgning efter type: " + e.getMessage());
        }
    }

    private MaterialVariant buildMaterialVariantFromResultSet(ResultSet rs) throws SQLException
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

        return new MaterialVariant(
                rs.getInt("material_variant_id"),
                rs.getInt("material_id"),
                (Integer) rs.getObject("variant_length"),
                rs.getDouble("unit_price"),
                (Integer) rs.getObject("pieces_per_unit"),
                material
        );
    }
}
