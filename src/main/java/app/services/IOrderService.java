package app.services;

import app.dto.CreateOrderRequestDTO;
import app.dto.CustomerOfferDTO;
import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.entities.Carport;
import app.entities.Order;
import app.entities.OrderDetail;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;

import java.util.List;
import java.util.Map;

public interface IOrderService
{
    public Order createPendingOrder(CreateOrderRequestDTO createOrderRequestDTO) throws DatabaseException;
    public Order getOrderById(int orderId) throws DatabaseException;
    public boolean updateOrder(Order order) throws DatabaseException;
    public boolean updateCarportAndBillOfMaterials(int orderId, Carport carport) throws DatabaseException;
    public boolean updateOrderCostPrice(int orderId, double newCostPrice) throws DatabaseException;
    public boolean deleteOrder(int orderId) throws DatabaseException;
    public boolean confirmAndSendOffer(Order order) throws DatabaseException;
    public int getTotalNumberOfOrdersByStatus(OrderStatus orderStatus) throws DatabaseException;
    public OrderDetail getOrderDetailByOrderId(int orderId) throws DatabaseException;
    public List<OrderOverviewDTO> getAllOrdersByStatus(OrderStatus orderStatus) throws DatabaseException;
    public List<OrderOverviewDTO> getAllOrdersByUserIdAndStatus(int userId, OrderStatus orderStatus) throws DatabaseException;
    public Map<OrderStatus, List<OrderOverviewDTO>> getOrderOverViewsByStatus(List<OrderStatus> orderStatuses) throws DatabaseException;
    public CustomerOfferDTO getCustomerOfferByOrderId(int orderId) throws DatabaseException;

}
