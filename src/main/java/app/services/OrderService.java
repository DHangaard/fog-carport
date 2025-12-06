package app.services;

import app.dto.CreateOrderRequest;
import app.dto.CustomerOfferDTO;
import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.entities.*;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;
import app.exceptions.MaterialNotFoundException;
import app.persistence.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService implements IOrderService
{
    private UserMapper userMapper;
    private MaterialLineMapper materialLineMapper;
    private ShedMapper shedMapper;
    private CarportMapper carportMapper;
    private OrderMapper orderMapper;
    private IBomService bomService;
    private IEmailService emailService;
    private ConnectionPool connectionPool;

    public OrderService(UserMapper userMapper, MaterialLineMapper materialLineMapper, ShedMapper shedMapper, CarportMapper carportMapper, OrderMapper orderMapper, IBomService bomService, IEmailService emailService, ConnectionPool connectionPool)
    {
        this.userMapper = userMapper;
        this.materialLineMapper = materialLineMapper;
        this.shedMapper = shedMapper;
        this.carportMapper = carportMapper;
        this.orderMapper = orderMapper;
        this.bomService = bomService;
        this.emailService = emailService;
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
                Shed savedShed = null;
                Shed shed = createOrderRequest.carport().getShed();

                if(shed != null)
                {
                    savedShed = shedMapper.createShed(
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

                if(savedShed != null)
                {
                    savedCarport.setShed(savedShed);
                }

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
    public boolean updateOrder(Order order) throws DatabaseException
    {
        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);

            try
            {
                orderMapper.updateOrder(connection, order);
                connection.commit();
                return true;
            }
            catch (DatabaseException e)
            {
                connection.rollback();
                throw new DatabaseException("Fejl ved updatering af ordre: " + e.getMessage());
            }
            catch (SQLException e)
            {
                connection.rollback();
                throw new DatabaseException("Fejl ved updatering af ordre: " + e.getMessage());
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl i forbindelse til databasen: " + e.getMessage());
        }
    }

    @Override
    public boolean updateOrderCostPrice(int orderId, double newCostPrice) throws DatabaseException
    {
        return orderMapper.updateOrder(orderId, newCostPrice);
    }

    @Override
    public boolean deleteOrder(int orderId) throws DatabaseException
    {
        return orderMapper.deleteOrder(orderId);
    }

    @Override
    public boolean confirmAndSendOffer(Order order) throws DatabaseException
    {
        boolean isOfferConfirmed = false;

        order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        if(updateOrder(order))
        {
            User user = userMapper.getUserById(order.getCustomerId());
            UserDTO userDTO = buildAndGetUserDTO(user);

            emailService.sendOfferReady(userDTO);
            isOfferConfirmed = true;
        }
        else
        {
            isOfferConfirmed= false;
        }
        return isOfferConfirmed;
    }

    @Override
    public int getTotalNumberOfOrdersByStatus(OrderStatus orderStatus) throws DatabaseException
    {
        return orderMapper.getNumberOfOrdersByStatus(orderStatus);
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


    @Override
    public List<OrderOverviewDTO> getAllOrdersByUserId(int userId) throws DatabaseException
    {
        return orderMapper.getAllOrderOverviewsByUserId(userId);
    }

    @Override
    public List<OrderOverviewDTO> getAllOrdersByUserIdAndStatus(int userId, OrderStatus orderStatus) throws DatabaseException
    {
        return orderMapper.getAllOrderOverviewsByUserIdAndStatus(userId, orderStatus);
    }

    @Override
    public Map<OrderStatus, List<OrderOverviewDTO>> getOrderOverViewsByStatus(List<OrderStatus> orderStatuses) throws DatabaseException
    {
        Map<OrderStatus, List<OrderOverviewDTO>> orderOverviews = new HashMap<>();

        for(OrderStatus status: orderStatuses)
        {
            orderOverviews.put(status, orderMapper.getAllOrderOverviewsByStatus(status));
        }
        return orderOverviews;
    }

    @Override
    public CustomerOfferDTO getCustomerOfferByOrderId(int orderId) throws DatabaseException
    {
        Order order = orderMapper.getOrderById(orderId);
        Carport carport = carportMapper.getCarportById(order.getCarportId());
        User customer = userMapper.getUserById(order.getCustomerId());
        User seller = userMapper.getUserById(order.getSellerId());

        CustomerOfferDTO customerOfferDTO = buildAndGetCustomerOfferDTO(order, carport, customer, seller);

        return customerOfferDTO;
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

    UserDTO buildAndGetUserDTO(User user)
    {
       return new UserDTO(
               user.getUserId(),
               user.getFirstName(),
               user.getLastName(),
               user.getStreet(),
               user.getZipCode(),
               user.getCity(),
               user.getEmail(),
               user.getPhoneNumber(),
               user.getRole()
       );
    }

    private CustomerOfferDTO buildAndGetCustomerOfferDTO(Order order, Carport carport, User customer, User seller)
    {
        return new CustomerOfferDTO(
                order.getOrderId(),
                carport,
                buildAndGetUserDTO(customer),
                buildAndGetUserDTO(seller),
                new OrderTimeLine(order.getCustomerRequestCreatedAt(),
                        order.getCreatedAt(),
                        order.getOfferValidDays()),
                order.getPricingDetails()
                        .getTotalPrice(),
                order.getOrderStatus(),
                order.getCustomerComment()
        );
    }

}
