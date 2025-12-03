package app.services;

import app.dto.CreateOrderRequest;
import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.entities.Carport;
import app.entities.Order;
import app.entities.OrderDetail;
import app.entities.PricingDetails;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;

import java.util.List;

public interface IOrderService
{
    public boolean saveOrderRequest(Carport carport, UserDTO userDTO) throws DatabaseException;
    public Order createPendingOrder(CreateOrderRequest createOrderRequest) throws DatabaseException;
    public Order getOrderById(int orderId) throws DatabaseException;
    public boolean updateOrder(Order order) throws DatabaseException;
    public boolean confirmAndSendOffer(Order order) throws DatabaseException;
    public OrderDetail getOrderDetailByCustomerId(int customerId) throws DatabaseException;
    public OrderDetail getOrderDetailByOrderId(int orderId) throws DatabaseException;
    public List<OrderOverviewDTO>  getAllOrdersByStatus(OrderStatus orderStatus) throws DatabaseException;
}
