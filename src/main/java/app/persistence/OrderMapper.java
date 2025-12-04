package app.persistence;

import app.dto.OrderOverviewDTO;
import app.entities.Order;
import app.entities.PricingDetails;
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

    public Order createOrder(Connection connection, int customerId, int carportId, String customerComment, PricingDetails pricingDetails) throws DatabaseException
    {
        String sql = """
               INSERT INTO "orders" (customer_id, carport_id, customer_comment, order_status, coverage_percentage, cost_price)
               VALUES (?, ?, ?, 'PENDING', ?, ?)
               RETURNING order_id, request_created_at, order_status
               """;

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, customerId);
            ps.setInt(2, carportId);

            if (customerComment != null)
            {
                ps.setString(3, customerComment);
            }
            else
            {
                ps.setNull(3, Types.VARCHAR);
            }

            ps.setDouble(4,pricingDetails.getCoveragePercentage());
            ps.setDouble(5,pricingDetails.getCostPrice());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new Order(
                        rs.getInt("order_id"),
                        customerId,
                        null,
                        carportId,
                        rs.getTimestamp("request_created_at"),
                        null,
                        null,
                        customerComment,
                        OrderStatus.valueOf(rs.getString("order_status")),
                        pricingDetails
                        );
            }

            throw new DatabaseException("Kunne ikke oprette ordren");
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af ordre " + e.getMessage());
        }
    }

    public Order getOrderById(int orderId) throws DatabaseException
    {
        String sql = """
                SELECT order_id, customer_id, seller_id, carport_id, request_created_at, created_at, offer_valid_days, order_status, customer_comment, coverage_percentage, cost_price
                FROM orders 
                WHERE order_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return buildOrderFromResultSet(rs);
            }
            else
            {
                throw new DatabaseException("Ordren blev ikke fundet for ordre: " + orderId);
            }

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af ordre " + e.getMessage());
        }
    }

    public List<Order> getAllOrders() throws DatabaseException
    {
        String sql = """
                SELECT order_id, customer_id, seller_id, carport_id, request_created_at, created_at, offer_valid_days, order_status, customer_comment, coverage_percentage, cost_price
                FROM orders
                ORDER BY request_created_at DESC
                """;

        List<Order> orders = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                orders.add(buildOrderFromResultSet(rs));
            }

            return orders;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af alle ordre: " + e.getMessage());
        }
    }

    public List<Order> getAllOrdersByUserId(int userId) throws DatabaseException
    {
        String sql = """
                SELECT order_id, customer_id, seller_id, carport_id, request_created_at, created_at, offer_valid_days, order_status, customer_comment, coverage_percentage, cost_price
                FROM orders
                WHERE customer_id = ?
                ORDER BY request_created_at DESC
                """;

        List<Order> orders = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                orders.add(buildOrderFromResultSet(rs));
            }

            return orders;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af alle ordre: " + e.getMessage());
        }
    }


    public List<Order> getAllOrdersByStatus(OrderStatus offerStatus) throws DatabaseException
    {
        String sql = """
                SELECT order_id, customer_id, seller_id, carport_id, request_created_at, created_at, offer_valid_days, order_status, customer_comment, coverage_percentage, cost_price
                FROM orders
                WHERE order_status = ?
                ORDER BY request_created_at DESC
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
            throw new DatabaseException("Fejl ved hentning af alle tilbud: " + e.getMessage());
        }
    }

    public List<OrderOverviewDTO> getAllOrderOverviewsByStatus(OrderStatus status) throws DatabaseException
    {
        List<OrderOverviewDTO> orderOverviewDTOS = new ArrayList<>();

        String sql = """
               SELECT o.order_id, u.first_name, u.last_name, u.email, o.request_created_at, o.order_status
               FROM orders o
               JOIN users u ON o.customer_id = u.user_id
               WHERE o.order_status = ?
               """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                orderOverviewDTOS.add(new OrderOverviewDTO(
                        rs.getInt("order_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getTimestamp("request_created_at"),
                        OrderStatus.valueOf(rs.getString("order_status"))
                ));
            }

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af ordrer" + e.getMessage());
        }
        return orderOverviewDTOS;
    }

    public List<OrderOverviewDTO> getAllOrderOverviewsByUserId(int userId) throws DatabaseException
    {
        List<OrderOverviewDTO> orderOverviewDTOS = new ArrayList<>();

        String sql = """
               SELECT o.order_id, u.first_name, u.last_name, u.email, o.request_created_at, o.order_status
               FROM orders o
               JOIN users u ON o.customer_id = u.user_id
               WHERE u.user_id = ?
               """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                orderOverviewDTOS.add(new OrderOverviewDTO(
                        rs.getInt("order_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getTimestamp("request_created_at"),
                        OrderStatus.valueOf(rs.getString("order_status"))
                ));
            }

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af ordrer" + e.getMessage());
        }
        return orderOverviewDTOS;
    }

    public List<OrderOverviewDTO> getAllOrderOverviewsByUserIdAndStatus(int userId, OrderStatus orderStatus) throws DatabaseException
    {
        List<OrderOverviewDTO> orderOverviewDTOS = new ArrayList<>();

        String sql = """
               SELECT o.order_id, u.first_name, u.last_name, u.email, o.request_created_at, o.order_status
               FROM orders o
               JOIN users u ON o.customer_id = u.user_id
               WHERE u.user_id = ? AND o.order_status = ?
               ORDER BY o.request_created_at DESC
               """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ps.setString(2, orderStatus.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                orderOverviewDTOS.add(new OrderOverviewDTO(
                        rs.getInt("order_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getTimestamp("request_created_at"),
                        OrderStatus.valueOf(rs.getString("order_status"))
                ));
            }

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af ordrer" + e.getMessage());
        }
        return orderOverviewDTOS;
    }

    public boolean updateOrder(Connection connection, Order order) throws DatabaseException
    {
        String sql = """
               UPDATE orders
               SET seller_id = ?, created_at = ?, offer_valid_days = ?, customer_comment = ?, order_status = ?, coverage_percentage = ?, cost_price = ?
               WHERE order_id = ?
               """;

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {
            if (order.getSellerId() != null)
            {
                ps.setInt(1, order.getSellerId());
            }
            else
            {
                ps.setNull(1, Types.INTEGER);
            }

            if (order.getCreatedAt() != null)
            {
                ps.setTimestamp(2, order.getCreatedAt());
            }
            else
            {
                ps.setNull(2, Types.TIMESTAMP);
            }

            if (order.getOfferValidDays() != null)
            {
                ps.setInt(3, order.getOfferValidDays());
            }
            else
            {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setString(4, order.getCustomerComment());
            ps.setString(5, order.getOrderStatus().name());
            ps.setDouble(6, order.getPricingDetails().getCoveragePercentage());
            ps.setDouble(7, order.getPricingDetails().getCostPrice());
            ps.setInt(8, order.getOrderId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af tilbuddet: " + e.getMessage());
        }
    }

    public boolean updateOrder(int orderId, double newCostPrice) throws DatabaseException
    {
        String sql = """
               UPDATE orders
               SET cost_price = ?
               WHERE order_id = ?
               """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setDouble(1, newCostPrice);
            ps.setInt(2, orderId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af kostprisen: " + e.getMessage());
        }
    }

    public boolean deleteOrder(int orderId) throws DatabaseException
    {
        String sql = """
               DELETE FROM orders WHERE order_id = ?
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
            throw new DatabaseException("Fejl ved sletning af tilbuddet: " + e.getMessage());
        }
    }

    private Order buildOrderFromResultSet(ResultSet rs) throws SQLException
    {
        Double coveragePercentage = (Double) rs.getObject("coverage_percentage");
        Double costPrice = (Double) rs.getObject("cost_price");

        PricingDetails pricingDetails = null;

        if (coveragePercentage != null && costPrice != null)
        {
            pricingDetails = new PricingDetails(costPrice, coveragePercentage);
        }

        return new Order(
                rs.getInt("order_id"),
                rs.getInt("customer_id"),
                (Integer) rs.getObject("seller_id"),
                rs.getInt("carport_id"),
                rs.getTimestamp("request_created_at"),
                rs.getTimestamp("created_at"),
                (Integer) rs.getObject("offer_valid_days"),
                rs.getString("customer_comment"),
                OrderStatus.valueOf(rs.getString("order_status")),
                pricingDetails
        );
    }
}
