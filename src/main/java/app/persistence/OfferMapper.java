package app.persistence;

import app.entities.Offer;
import app.entities.OfferDate;
import app.enums.OfferStatus;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class OfferMapper
{
    private ConnectionPool connectionPool;

    public OfferMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Offer createOffer(Connection connection, int customerId, int carportId, String customerComment) throws DatabaseException
    {
        String sql = """
                INSERT INTO offer (customer_id, carport_id, customer_comment)
                VALUES (?, ?, ?)
                RETURNING offer_id
                """;
        try
        {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setInt(1, customerId);

            ResultSet rs = ps.executeQuery();
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke oprette tilbudet");
        }
        return null;
    }

    public Offer getOfferById(int offerId)
    {
        return null;
    }

    public List<Offer> getAllOffersByOfferId(int offerId)
    {
        return null;
    }

    public List<Offer> getAllOffersByCustomerId(int customerId)
    {
        return null;
    }

    public List<Offer> getAllOffersBySellerId(int sellerId)
    {
        return null;
    }

    public List<Offer> getAllOffersByStatus(OfferStatus offerStatus)
    {
        return null;
    }

    public boolean updateOffer(Offer offer)
    {
        return false;
    }

    public boolean deleteOffer(int offerId)
    {
        return false;
    }

    private Offer buildOfferFromResultSet(ResultSet rs)
    {
        return null;
    }
}
