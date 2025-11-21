package app.persistence;

import app.entities.Offer;
import app.enums.OfferStatus;

import java.sql.ResultSet;
import java.util.List;

public class OfferMapper
{
    private ConnectionPool connectionPool;
    private BomMapper bomMapper;
    private CarportMapper carportMapper;

    public OfferMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
        this.bomMapper = new BomMapper(connectionPool);
        this.carportMapper = new CarportMapper(connectionPool);
    }

    public Offer createOffer(Offer offer)
    {
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
