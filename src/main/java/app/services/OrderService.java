package app.services;

import app.dto.CreateOrderRequest;
import app.dto.UserDTO;
import app.entities.*;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;
import app.persistence.*;

import java.sql.Connection;
import java.sql.SQLException;
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
                throw e;
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
    public OrderDetail getOrderById(int orderId) throws DatabaseException
    {
        return null;
    }

    @Override
    public List<OrderDetail> getAllOrdersByStatus(OrderStatus orderStatus) throws DatabaseException
    {
        return List.of();
    }
}
