package app.persistence;

import app.entities.BillOfMaterials;
import app.entities.MaterialLine;
import app.entities.Offer;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.List;

public class BomMapper
{
    private ConnectionPool connectionPool;
    private MaterialLineMapper materialLineMapper;


    public BomMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
        this.materialLineMapper = new MaterialLineMapper(connectionPool);
    }

    public BillOfMaterials createBillOfMaterials(BillOfMaterials billOfMaterials) throws DatabaseException {
        Connection connection = null;
        int bomId = 0;

        try
        {
            connection = connectionPool.getConnection();
            connection.setAutoCommit(false);

            String sql = """
                    INSERT INTO bill_of_materials (offer_id, cost_price, coverage_percentage, price_without_vat, total_price) " +
                    VALUES (?, ?, ?, ?, ?) " +
                    RETURNING bom_id
                    """;

            try (PreparedStatement ps = connection.prepareStatement(sql))
            {
                ps.setInt(1, billOfMaterials.getOfferId());
                ps.setDouble(2, billOfMaterials.getPricingDetails().getCostPrice());
                ps.setDouble(3, billOfMaterials.getCoveragePercentage());
                ps.setDouble(4, billOfMaterials.getPricingDetails().getPriceWithOutVat());
                ps.setDouble(5, billOfMaterials.getPricingDetails().getTotalPrice());

                ResultSet rs = ps.executeQuery();
                if (rs.next())
                {
                    bomId = rs.getInt("bom_id");
                }
                else
                {
                    throw new DatabaseException("Kunne ikke oprette stykliste");
                }
            }


            for (MaterialLine materialLine : billOfMaterials.getMaterialLines())
            {
                materialLine.setBomId(bomId);
                materialLineMapper.createMaterialLine(materialLine, connection);
            }

            connection.commit();

            return getBillOfMaterialsById(bomId);

        }
        catch (SQLException e)
        {
            if (connection != null)
            {
                try
                {
                    connection.rollback();
                } catch (SQLException rollbackEx)
                {
                    throw new DatabaseException("Fejl ved rollback: " + rollbackEx.getMessage());
                }
            }
            else
            {
                throw new DatabaseException("Fejl ved oprettelse af stykliste: " + e.getMessage());
            }

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
                }
            }
        }
    }

    public BillOfMaterials getBillOfMaterialsByOfferId(int offerId) throws DatabaseException
    {
        return null;
    }

    public BillOfMaterials getBillOfMaterialsById(int bomId) throws DatabaseException {

        String sql = """
                SELECT b.bom_id, b.offer_id, b.cost_price, b.coverage_percentage, b.price_without_vat, b.total_price
                FROM bill_of_materials b " +
                WHERE b.bom_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, bomId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                //List<MaterialLine> materialLines = materialLineMapper.getMaterialLinesByBomId(bomId);
                return buildBomFromResultSet(rs, null);
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

    public boolean updateBillOfMaterials(BillOfMaterials billOfMaterials) throws DatabaseException
    {
        return false;
    }

    public boolean deleteBillOfMaterials(int bomId) throws DatabaseException
    {
        return false;
    }

    private BillOfMaterials buildBomFromResultSet(ResultSet rs, List<MaterialLine> materialLines) throws SQLException
    {

        return null;
    }



}
