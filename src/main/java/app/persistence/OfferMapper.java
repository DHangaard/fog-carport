package app.persistence;

import app.entities.Offer;
import app.entities.OrderTimeLine;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
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
            ps.setInt(2, carportId);
            ps.setString(3, customerComment);


            ResultSet rs = ps.executeQuery();

            if(rs.next())
            {
                int offerId = rs.getInt("offer_id");

                OrderTimeLine orderTimeLine = new OrderTimeLine(
                        new Timestamp(System.currentTimeMillis()),
                        null
                );

                return new Offer(
                        offerId,
                        customerId,
                        null,
                        carportId,
                        orderTimeLine,
                        customerComment,
                        OrderStatus.PENDING
                );
            }

            throw new DatabaseException("Kunne ikke oprette tilbuddet");
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af tilbuddet " + e.getMessage());
        }
    }

    public Offer getOfferById(int offerId) throws DatabaseException
    {
        String sql = """
                SELECT offer_id, customer_id, seller_id, carport_id, request_created_at, created_date, expiration_date,
                customer_comment, offer_status
                FROM offer 
                WHERE offer_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, offerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return buildOfferFromResultSet(rs);
            }
            else
            {
                throw new DatabaseException("Tilbuddet blev ikke fundet for id: " + offerId);
            }

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af tilbuddet " + e.getMessage());
        }
    }

    public List<Offer> getAllOffers() throws DatabaseException
    {
        String sql = """
                SELECT offer_id, customer_id, seller_id, carport_id, request_created_at, created_date, expiration_date,
                customer_comment, offer_status
                FROM offer 
                ORDER BY request_created_at DESC
                """;

        List<Offer> offers = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                offers.add(buildOfferFromResultSet(rs));
            }

            return offers;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af alle tilbud: " + e.getMessage());
        }
    }

    public List<Offer> getAllOffersByCustomerId(int customerId) throws DatabaseException
    {
        String sql = """
                SELECT offer_id, customer_id, seller_id, carport_id, request_created_at, created_date, expiration_date,
                customer_comment, offer_status
                FROM offer
                WHERE customer_id = ?
                ORDER BY request_created_at DESC
                """;

        List<Offer> offers = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, customerId);

            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                offers.add(buildOfferFromResultSet(rs));
            }

            return offers;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af alle tilbud: " + e.getMessage());
        }
    }


    public List<Offer> getAllOffersByStatus(OrderStatus offerStatus) throws DatabaseException
    {
        String sql = """
                SELECT offer_id, customer_id, seller_id, carport_id, request_created_at, created_date, expiration_date,
                customer_comment, offer_status
                FROM offer
                WHERE offer_status = ?
                ORDER BY request_created_at DESC
                """;

        List<Offer> offers = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, offerStatus.name());

            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                offers.add(buildOfferFromResultSet(rs));
            }

            return offers;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af alle tilbud: " + e.getMessage());
        }
    }


    public boolean updateOffer(Offer offer) throws DatabaseException
    {
        String sql = """
        UPDATE offer
        SET seller_id = ?, carport_id = ?, created_date = ?, expiration_date = ?, customer_comment = ?, offer_status = ?
        WHERE offer_id = ?
            """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            Integer sellerId = offer.getSellerId();
            if (sellerId != null)
            {
                ps.setInt(1, sellerId);
            }
            else
            {
                ps.setNull(1, Types.INTEGER);
            }

            ps.setInt(2, offer.getCarportId());
            ps.setTimestamp(3, offer.getOrderTimeLine().getCreatedAt());
            ps.setTimestamp(4, offer.getOrderTimeLine().getExpirationDate());
            ps.setString(5, offer.getCustomerComment());
            ps.setString(6, offer.getOrderStatus().name());
            ps.setInt(7, offer.getOfferId());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af tilbuddet: " + e.getMessage());
        }
    }

    public boolean deleteOffer(int offerId) throws DatabaseException
    {
        String sql = """
               DELETE FROM offer WHERE offer_id = ?
               """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, offerId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af tilbuddet: " + e.getMessage());
        }
    }

    private Offer buildOfferFromResultSet(ResultSet rs) throws SQLException
    {
        OrderTimeLine orderTimeLine = new OrderTimeLine(
                rs.getTimestamp("request_created_at"),
                rs.getTimestamp("created_date"),
                rs.getTimestamp("expiration_date")
        );

        return new Offer(
                rs.getInt("offer_id"),
                rs.getInt("customer_id"),
                (Integer) rs.getObject("seller_id"),
                rs.getInt("carport_id"),
                orderTimeLine,
                rs.getString("customer_comment"),
                OrderStatus.valueOf(rs.getString("offer_status"))
        );
    }
}
