package app.services;

import app.dto.CreateOrderRequest;
import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.entities.*;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;
import app.exceptions.MaterialNotFoundException;
import app.persistence.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrderService implements IOrderService
{
    private UserMapper userMapper;
    private MaterialLineMapper materialLineMapper;
    private ShedMapper shedMapper;
    private CarportMapper carportMapper;
    private OrderMapper orderMapper;
    private IBomService bomService;
    private ConnectionPool connectionPool;

    public OrderService(UserMapper userMapper, MaterialLineMapper materialLineMapper, ShedMapper shedMapper, CarportMapper carportMapper, OrderMapper orderMapper, IBomService bomService, ConnectionPool connectionPool)
    {
        this.userMapper = userMapper;
        this.materialLineMapper = materialLineMapper;
        this.shedMapper = shedMapper;
        this.carportMapper = carportMapper;
        this.orderMapper = orderMapper;
        this.bomService = bomService;
        this.connectionPool = connectionPool;
    }

    @Override
    public boolean saveOrderRequest(Carport carport, UserDTO userDTO) throws DatabaseException
    {
        return false;
    }

    @Override
    public Order createPendingOrder(CreateOrderRequest createOrderRequest) throws DatabaseException
    {
        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            try
            {
                Integer shedId = null;
                Shed shed = createOrderRequest.carport().getShed();

                if(shed != null)
                {
                    Shed savedShed = shedMapper.createShed(
                            connection, shed.getLength(),
                            shed.getWidth(),
                            shed.getShedPlacement()
                    );
                    shedId = savedShed.getShedId();
                }

                Carport carport = createOrderRequest.carport();
                Carport savedCarport = carportMapper.createCarport(
                        connection,
                        carport.getLength(),
                        carport.getWidth(),
                        shedId,
                        carport.getRoofType()
                );

                List<MaterialLine> bom = bomService.getBillOfMaterialByCarport(savedCarport);
                PricingDetails pricingDetails = bomService.calculateCarportPrice(bom);

                int customerId = createOrderRequest.userId();
                String customerComment = createOrderRequest.customerComment();

                Order savedOrder = orderMapper.createOrder(
                        connection,
                        customerId,
                        savedCarport.getCarportId(),
                        customerComment,
                        pricingDetails
                );

                for(MaterialLine line: bom)
                {
                    materialLineMapper.createMaterialLine(
                            connection,
                            savedOrder.getOrderId(),
                            line.getMaterialVariant().getMaterialVariantId(),
                            line.getQuantity()
                    );
                }

                connection.commit();
                return savedOrder;
            }
            catch (DatabaseException e)
            {
                connection.rollback();

                throw new DatabaseException("Fejl ved oprettelse af ordre" + e.getMessage());
            }
            catch (MaterialNotFoundException e)
            {
                connection.rollback();

                throw new DatabaseException("Fejl ved oprettelse af ordre" + e.getMessage());
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af ordre: " + e.getMessage());
        }
    }

    @Override
    public boolean updateOrderStatus(Order order, OrderStatus orderStatus) throws DatabaseException
    {
        return false;
    }

    @Override
    public boolean confirmOrder(int orderId)
    {
        return false;
    }

    @Override
    public OrderDetail getOrderDetailByCustomerId(int customerId) throws DatabaseException
    {
        return null;
    }

    @Override
    public OrderDetail getOrderDetailByOrderId(int orderId) throws DatabaseException
    {
        Order order = orderMapper.getOrderById(orderId);
        User customer = userMapper.getUserById(order.getCustomerId());

        User seller = null;
        if(order.getSellerId() != null)
        {
            seller = userMapper.getUserById(order.getSellerId());
        }

        Carport carport = carportMapper.getCarportById(order.getCarportId());
        List<MaterialLine> materialLines = materialLineMapper.getMaterialLinesByOrderId(orderId);

        return buildOrderDetail(order, customer, seller, carport, materialLines);
    }

    @Override
    public Order getOrderById(int orderId) throws DatabaseException
    {
        return orderMapper.getOrderById(orderId);
    }


    @Override
    public List<OrderOverviewDTO> getAllOrdersByStatus(OrderStatus orderStatus) throws DatabaseException
    {
        return orderMapper.getAllOrderOverviewsByStatus(orderStatus);
    }

    private OrderDetail buildOrderDetail(Order order, User customer, User seller, Carport carport, List<MaterialLine> materialLines)
    {
        OrderTimeLine orderTimeLine = new OrderTimeLine();

        orderTimeLine.setCustomerRequestCreatedAt(order.getCustomerRequestCreatedAt());

        Integer offerValidDays = order.getOfferValidDays();
        Timestamp createdAt = order.getCreatedAt();

        if (offerValidDays != null && createdAt != null)
        {
            orderTimeLine.setOfferValidDays(offerValidDays);
            orderTimeLine.setCreatedAt(createdAt);
        }
        else
        {
            orderTimeLine.setOfferValidDays(null);
            orderTimeLine.setCreatedAt(null);
        }

        return new OrderDetail(
                order.getOrderId(),
                seller,
                customer,
                carport,
                orderTimeLine,
                materialLines,
                order.getCustomerComment(),
                order.getPricingDetails(),
                order.getOrderStatus()
        );
    }

}
