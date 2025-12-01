package app.services;

import app.dto.CreateOrderRequest;
import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.entities.Carport;
import app.entities.Order;
import app.entities.OrderDetail;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;

import java.util.List;

public interface IOrderService
{
    public boolean saveOrderRequest(Carport carport, UserDTO userDTO) throws DatabaseException;
    public Order createPendingOrder(CreateOrderRequest createOrderRequest) throws DatabaseException;
    public boolean updateOrderStatus(Order order, OrderStatus orderStatus) throws DatabaseException;
    public boolean confirmOrder(int orderId);
    public OrderDetail getOrderDetailByCustomerId(int customerId) throws DatabaseException;
    public OrderDetail getOrderById(int orderId) throws DatabaseException;
    public List<OrderOverviewDTO>  getAllOrdersByStatus(OrderStatus orderStatus) throws DatabaseException;
}
