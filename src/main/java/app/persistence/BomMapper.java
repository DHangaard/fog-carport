package app.persistence;

import app.entities.*;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import com.fasterxml.jackson.databind.ext.SqlBlobSerializer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BomMapper
{
    private ConnectionPool connectionPool;

    public BomMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public BillOfMaterials createBillOfMaterials(Connection connection, int offerId, double costPrice, double coveragePercentage, double priceWithoutVat, double totalPrice) throws DatabaseException
    {
        String sql = """
                    INSERT INTO bill_of_materials (offer_id, cost_price, coverage_percentage, price_without_vat, total_price)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING bom_id
                    """;

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, offerId);
            ps.setDouble(2, costPrice);
            ps.setDouble(3, coveragePercentage);
            ps.setDouble(4, priceWithoutVat);
            ps.setDouble(5, totalPrice);

            ResultSet rs = ps.executeQuery();


            if (rs.next())
            {
                int bomId = rs.getInt("bom_id");

                PricingDetails pricingDetails = new PricingDetails(
                        costPrice,
                        priceWithoutVat,
                        totalPrice
                );

                return new BillOfMaterials(
                        bomId,
                        offerId,
                        new ArrayList<>(),
                        pricingDetails,
                        coveragePercentage
                );
            }

            throw new DatabaseException("Kunne ikke oprette stykliste");

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af stykliste: " + e.getMessage());
        }
    }

    public BillOfMaterials getBillOfMaterialsByOfferId(int offerId) throws DatabaseException
    {

        String sql = """
                SELECT b.bom_id, b.offer_id, b.cost_price, b.coverage_percentage, b.price_without_vat, b.total_price, ml.*, m.*, mv.*
                FROM bill_of_materials b
                LEFT JOIN material_line ml ON b.bom_id = ml.bom_id
                LEFT JOIN material m ON ml.material_id = m.material_id
                LEFT JOIN material_variant mv ON m.material_id = mv.material_id
                WHERE b.offer_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, offerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return buildBomFromResultSet(rs);
            }
            else
            {
                throw new DatabaseException("Stykliste ikke fundet for offer_id: " + offerId);
            }

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af stykliste: " + e.getMessage());
        }
    }

    public BillOfMaterials getBillOfMaterialsById(int bomId) throws DatabaseException
    {

        String sql = """
                SELECT b.bom_id, b.offer_id, b.cost_price, b.coverage_percentage, b.price_without_vat, b.total_price, ml.*, m.*, mv.*
                FROM bill_of_materials b
                LEFT JOIN material_line ml ON b.bom_id = ml.bom_id
                LEFT JOIN material m ON ml.material_id = m.material_id
                LEFT JOIN material_variant mv ON m.material_id = mv.material_id
                WHERE b.bom_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, bomId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return buildBomFromResultSet(rs);
            }
            else
            {
                throw new DatabaseException("Stykliste ikke fundet med id: " + bomId);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af stykliste: " + e.getMessage());
        }
    }

    public boolean updateBillOfMaterials(BillOfMaterials bom) throws DatabaseException
    {

        String sql = """
            UPDATE bill_of_materials
            SET cost_price = ?, coverage_percentage = ?, price_without_vat = ?, total_price = ?
            WHERE bom_id = ?
            """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setDouble(1, bom.getPricingDetails().getCostPrice());
            ps.setDouble(2, bom.getCoveragePercentage());
            ps.setDouble(3, bom.getPricingDetails().getPriceWithOutVat());
            ps.setDouble(4, bom.getPricingDetails().getTotalPrice());
            ps.setInt(5, bom.getBomId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af stykliste: " + e.getMessage());
        }
    }

    public boolean deleteBillOfMaterials(int bomId) throws DatabaseException
    {

        String sql = """
               DELETE FROM bill_of_materials WHERE bom_id = ?
               """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, bomId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af stykliste: " + e.getMessage());
        }
    }

    private BillOfMaterials buildBomFromResultSet(ResultSet rs) throws SQLException
    {
        List<MaterialLine> materialLines = new ArrayList<>();
        int bomId = rs.getInt("bom_id");
        int offerId = rs.getInt("offer_id");
        double costPrice = rs.getDouble("cost_price");
        double coveragePercentage = rs.getDouble("coverage_percentage");
        double priceWithoutVat = rs.getDouble("price_without_vat");
        double totalPrice = rs.getDouble("total_price");

        int lastMaterialLineId = -1;

        do
        {
            if (rs.getObject("material_line_id") != null) {

                int currentMaterialLineId = rs.getInt("material_line_id");

                if (currentMaterialLineId != lastMaterialLineId) {

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
                            rs.getInt("variant_length"),
                            rs.getDouble("unit_price")
                    );

                    MaterialLine materialLine = new MaterialLine(
                            currentMaterialLineId,
                            rs.getInt("bom_id"),
                            material,
                            rs.getInt("quantity"),
                            rs.getDouble("line_total")
                    );

                    materialLines.add(materialLine);

                    lastMaterialLineId = currentMaterialLineId;
                }
            }

        } while (rs.next());

        PricingDetails pricingDetails = new PricingDetails(
                costPrice,
                priceWithoutVat,
                totalPrice
        );

        BillOfMaterials billOfMaterials = new BillOfMaterials(
                bomId,
                offerId,
                materialLines,
                pricingDetails,
                coveragePercentage
        );

        return billOfMaterials;
    }
}
