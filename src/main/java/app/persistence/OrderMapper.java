package app.persistence;

import app.entities.Order;
import app.enums.OfferStatus;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper
{
    private ConnectionPool connectionPool;

    public OrderMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Order createOrder(int offerId, OrderStatus orderStatus) throws DatabaseException
    {
        String sql = """
                INSERT INTO order (offer_id, status)
                VALUES (?, ?)
                RETURNING order_id, order_date
                """;

        int orderId;
        Timestamp orderDate;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, offerId);
            ps.setString(2, orderStatus.name());

            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                orderId = rs.getInt("order_id");
                orderDate = rs.getTimestamp("order_date");

                return new Order(
                        orderId,
                        offerId,
                        orderDate,
                        orderStatus
                );
            }
            throw new DatabaseException("Fejl ved oprettelse af ordre");
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af ordre: " + e.getMessage());
        }
    }

    public List<Order> getAllOrdersByStatus(OfferStatus offerStatus) throws DatabaseException
    {
        String sql = """
                SELECT *
                FROM order
                WHERE offer_status = ?
                """;

        List<Order> orders = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, offerStatus.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                orders.add(buildOrderFromResultSet(rs));
            }
            return orders;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af ordre: " + e.getMessage());
        }
    }

    public Order getOrderByOfferId(int offerId) throws DatabaseException
    {
        String sql = """
                SELECT *
                FROM order 
                WHERE offer_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, offerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return buildOrderFromResultSet(rs);
            }
            else
            {
                throw new DatabaseException("Ordren blev ikke fundet for Tilbuds id: " + offerId);
            }

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af ordren " + e.getMessage());
        }
    }

    public boolean updateOrder(Order order) throws DatabaseException
    {
        String sql = """
                UPDATE order
                SET offer_id = ?, status = ?
                WHERE order_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, order.getOrderId());
            ps.setString(2, order.getStatus().name());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af ordre: " + e.getMessage());
        }
    }

    public boolean deleteOrder(int orderId) throws DatabaseException
    {
        String sql = """
                DELETE FROM order WHERE order_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, orderId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af ordre: " + e.getMessage());
        }
    }

    private Order buildOrderFromResultSet(ResultSet rs) throws SQLException
    {
        return new Order(
                rs.getInt("order_id"),
                rs.getInt("offer_id"),
                rs.getTimestamp("order_date"),
                OrderStatus.valueOf(rs.getString("status"))
        );
    }
}
