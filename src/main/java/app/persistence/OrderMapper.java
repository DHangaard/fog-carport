package app.persistence;

import app.entities.Order;
import app.exceptions.DatabaseException;

public class OrderMapper
{
    private ConnectionPool connectionPool;

    public OrderMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Order createOrder(Order order) throws DatabaseException
    {
        return null;
    }

    public Order getOrderByOrderId(int orderId) throws DatabaseException
    {
        return null;
    }

    public boolean updateOrder(Order order) throws DatabaseException
    {
        return false;
    }

    public boolean deleteOrder(int orderId) throws DatabaseException
    {
        return false;
    }

}
