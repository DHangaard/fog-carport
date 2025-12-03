package app.persistence;

import app.dto.OrderOverviewDTO;
import app.entities.Order;
import app.entities.PricingDetails;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "carport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static OrderMapper orderMapper;

    @BeforeAll
    static void setupClass()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DROP TABLE IF EXISTS test.material_line CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.orders CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.carport CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.shed CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.users CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.zip_code CASCADE");

                stmt.execute("DROP SEQUENCE IF EXISTS test.orders_order_id_seq CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.carport_carport_id_seq CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.shed_shed_id_seq CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.users_user_id_seq CASCADE");

                stmt.execute("CREATE TABLE test.zip_code AS (SELECT * FROM public.zip_code) WITH NO DATA");
                stmt.execute("ALTER TABLE test.zip_code ADD PRIMARY KEY (zip_code)");

                stmt.execute("CREATE TABLE test.users AS (SELECT * FROM public.users) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.users_user_id_seq");
                stmt.execute(
                        "ALTER TABLE test.users " +
                                "ALTER COLUMN user_id SET DEFAULT nextval('test.users_user_id_seq')"
                );
                stmt.execute("ALTER TABLE test.users ADD PRIMARY KEY (user_id)");
                stmt.execute("ALTER TABLE test.users ADD CONSTRAINT users_email_key UNIQUE (email)");
                stmt.execute(
                        "ALTER TABLE test.users ADD CONSTRAINT users_zip_code_fk " +
                                "FOREIGN KEY (zip_code) REFERENCES test.zip_code (zip_code)"
                );

                stmt.execute("CREATE TABLE test.shed AS (SELECT * FROM public.shed) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.shed_shed_id_seq");
                stmt.execute(
                        "ALTER TABLE test.shed " +
                                "ALTER COLUMN shed_id SET DEFAULT nextval('test.shed_shed_id_seq')"
                );
                stmt.execute("ALTER TABLE test.shed ADD PRIMARY KEY (shed_id)");

                stmt.execute("CREATE TABLE test.carport AS (SELECT * FROM public.carport) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.carport_carport_id_seq");
                stmt.execute(
                        "ALTER TABLE test.carport " +
                                "ALTER COLUMN carport_id SET DEFAULT nextval('test.carport_carport_id_seq')"
                );
                stmt.execute("ALTER TABLE test.carport ADD PRIMARY KEY (carport_id)");
                stmt.execute(
                        "ALTER TABLE test.carport ADD CONSTRAINT carport_shed_fk " +
                                "FOREIGN KEY (shed_id) REFERENCES test.shed (shed_id)"
                );

                stmt.execute("CREATE TABLE test.orders AS (SELECT * FROM public.orders) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.orders_order_id_seq");
                stmt.execute(
                        "ALTER TABLE test.orders " +
                                "ALTER COLUMN order_id SET DEFAULT nextval('test.orders_order_id_seq')"
                );
                stmt.execute("ALTER TABLE test.orders ADD PRIMARY KEY (order_id)");
                stmt.execute("ALTER TABLE test.orders ALTER COLUMN seller_id DROP NOT NULL");

                stmt.execute("ALTER TABLE test.orders " +
                                "ALTER COLUMN request_created_at SET DEFAULT CURRENT_TIMESTAMP"
                );

                stmt.execute(
                        "ALTER TABLE test.orders ADD CONSTRAINT orders_customer_fk " +
                                "FOREIGN KEY (customer_id) REFERENCES test.users (user_id)"
                );
                stmt.execute(
                        "ALTER TABLE test.orders ADD CONSTRAINT orders_seller_fk " +
                                "FOREIGN KEY (seller_id) REFERENCES test.users (user_id)"
                );
                stmt.execute(
                        "ALTER TABLE test.orders ADD CONSTRAINT orders_carport_fk " +
                                "FOREIGN KEY (carport_id) REFERENCES test.carport (carport_id)"
                );
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }

        orderMapper = new OrderMapper(connectionPool);
    }

    @BeforeEach
    void setUp()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DELETE FROM test.orders");
                stmt.execute("DELETE FROM test.carport");
                stmt.execute("DELETE FROM test.shed");
                stmt.execute("DELETE FROM test.users");
                stmt.execute("DELETE FROM test.zip_code");

                stmt.execute(
                        "INSERT INTO test.zip_code (zip_code, city) VALUES " +
                                "(1000, 'København K'), " +
                                "(2100, 'København Ø'), " +
                                "(2200, 'København N')"
                );

                stmt.execute(
                        "INSERT INTO test.users (user_id, first_name, last_name, email, hashed_password, " +
                                "zip_code, street, role, phone_number) VALUES " +
                                "(1, 'Mads', 'Nielsen', 'mads.nielsen@gmail.com', '$2a$10$hash1', 1000, 'Bredgade 25', 'CUSTOMER', '20345678'), " +
                                "(2, 'Sofie', 'Jensen', 'sofie.jensen@fog.dk', '$2a$10$hash2', 2100, 'Østerbrogade 112', 'SALESREP', '30456789'), " +
                                "(3, 'Lars', 'Andersen', 'lars.andersen@gmail.com', '$2a$10$hash3', 2200, 'Nørrebrogade 45', 'CUSTOMER', '40567890')"
                );

                stmt.execute(
                        "INSERT INTO test.carport (carport_id, length, width, roof_type, shed_id) VALUES " +
                                "(1, 600, 500, 'FLAT', NULL), " +
                                "(2, 780, 600, 'FLAT', NULL), " +
                                "(3, 500, 400, 'FLAT', NULL)"
                );

                stmt.execute(
                        "INSERT INTO test.orders (order_id, customer_id, seller_id, carport_id, " +
                                "request_created_at, created_at, offer_valid_days, customer_comment, order_status, coverage_percentage, cost_price) " +
                                "VALUES (1, 1, NULL, 1, CURRENT_TIMESTAMP - INTERVAL '1 day', NULL, NULL, " +
                                "'Jeg ønsker et tilbud på en carport 600x500 cm', 'PENDING', 40.0, 15000.00)"
                );

                stmt.execute(
                        "INSERT INTO test.orders (order_id, customer_id, seller_id, carport_id, " +
                                "request_created_at, created_at, offer_valid_days, customer_comment, order_status, coverage_percentage, cost_price) " +
                                "VALUES (2, 3, 2, 2, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '2 days', " +
                                "14, 'Carport med rejsning til 20 grader', 'READY', 45.0, 22000.00)"
                );

                stmt.execute(
                        "INSERT INTO test.orders (order_id, customer_id, seller_id, carport_id, " +
                                "request_created_at, created_at, offer_valid_days, customer_comment, order_status, coverage_percentage, cost_price) " +
                                "VALUES (3, 1, 2, 3, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '9 days', " +
                                "14, 'Lille carport til motorcykel', 'ACCEPTED', 40.0, 12000.00)"
                );

                stmt.execute(
                        "SELECT setval('test.users_user_id_seq', " +
                                "COALESCE((SELECT MAX(user_id) + 1 FROM test.users), 1), false)"
                );
                stmt.execute(
                        "SELECT setval('test.carport_carport_id_seq', " +
                                "COALESCE((SELECT MAX(carport_id) + 1 FROM test.carport), 1), false)"
                );
                stmt.execute(
                        "SELECT setval('test.orders_order_id_seq', " +
                                "COALESCE((SELECT MAX(order_id) + 1 FROM test.orders), 1), false)"
                );
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @Test
    void testConnection() throws SQLException
    {
        assertNotNull(connectionPool.getConnection());
    }

    @Test
    void testCreateOrder() throws DatabaseException, SQLException
    {
        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        Order order = orderMapper.createOrder(
                    connection,
                    1,
                    1,
                    "Jeg vil gerne have en carport til min nye bil",
                    new PricingDetails(18000.00, 40.0)
            );

        connection.commit();

        assertNotNull(order);
        assertEquals(4, order.getOrderId());
        assertEquals(1, order.getCustomerId());
        assertNull(order.getSellerId());
        assertEquals(1, order.getCarportId());
        assertEquals("Jeg vil gerne have en carport til min nye bil", order.getCustomerComment());
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertNotNull(order.getCustomerRequestCreatedAt());
        assertNull(order.getCreatedAt());
        assertNull(order.getOfferValidDays());
        assertNotNull(order.getPricingDetails());
        assertEquals(40.0, order.getPricingDetails().getCoveragePercentage());
        assertEquals(18000.00, order.getPricingDetails().getCostPrice());

        connection.close();
    }

    @Test
    void testGetOrderById() throws DatabaseException
    {
        Order order = orderMapper.getOrderById(1);

        assertNotNull(order);
        assertEquals(1, order.getOrderId());
        assertEquals(1, order.getCustomerId());
        assertNull(order.getSellerId());
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertNotNull(order.getPricingDetails());
        assertEquals(40.0, order.getPricingDetails().getCoveragePercentage());
        assertEquals(15000.00, order.getPricingDetails().getCostPrice());
    }

    @Test
    void testGetOrderByIdNotFound()
    {
        assertThrows(DatabaseException.class, () -> orderMapper.getOrderById(999));
    }

    @Test
    void testGetAllOrders() throws DatabaseException
    {
        List<Order> orders = orderMapper.getAllOrders();

        assertNotNull(orders);
        assertEquals(3, orders.size());
    }

    @Test
    void testGetAllOrdersByUserId() throws DatabaseException
    {
        List<Order> customerOrders = orderMapper.getAllOrdersByUserId(1);

        assertNotNull(customerOrders);
        assertEquals(2, customerOrders.size());
    }

    @Test
    void testGetAllOrdersByStatus() throws DatabaseException
    {
        List<Order> pending = orderMapper.getAllOrdersByStatus(OrderStatus.PENDING);
        assertEquals(1, pending.size());

        List<Order> ready = orderMapper.getAllOrdersByStatus(OrderStatus.READY);
        assertEquals(1, ready.size());

        List<Order> accepted = orderMapper.getAllOrdersByStatus(OrderStatus.ACCEPTED);
        assertEquals(1, accepted.size());

        List<Order> rejected = orderMapper.getAllOrdersByStatus(OrderStatus.REJECTED);
        assertEquals(0, rejected.size());
    }

    @Test
    void testUpdateOrderFromPendingToReady() throws DatabaseException, SQLException
    {
        Order order = orderMapper.getOrderById(1);
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertNull(order.getSellerId());

        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        order.setSellerId(2);
        order.setOrderStatus(OrderStatus.READY);
        order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        order.setOfferValidDays(14);
        order.setPricingDetails(new PricingDetails(16000.00, 45.0));

        boolean updated = orderMapper.updateOrder(connection, order);
        connection.commit();
        assertTrue(updated);

        connection.close();

        Order updatedOrder = orderMapper.getOrderById(1);
        assertEquals(2, updatedOrder.getSellerId());
        assertEquals(OrderStatus.READY, updatedOrder.getOrderStatus());
        assertNotNull(updatedOrder.getCreatedAt());
        assertEquals(14, updatedOrder.getOfferValidDays());
        assertEquals(45.0, updatedOrder.getPricingDetails().getCoveragePercentage());
        assertEquals(16000.00, updatedOrder.getPricingDetails().getCostPrice());
    }

    @Test
    void testUpdateOrderFromReadyToAccepted() throws DatabaseException, SQLException
    {
        Order order = orderMapper.getOrderById(2);
        assertEquals(OrderStatus.READY, order.getOrderStatus());

        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        order.setOrderStatus(OrderStatus.ACCEPTED);
        boolean updated = orderMapper.updateOrder(connection, order);
        connection.commit();
        assertTrue(updated);

        connection.close();

        Order acceptedOrder = orderMapper.getOrderById(2);
        assertEquals(OrderStatus.ACCEPTED, acceptedOrder.getOrderStatus());
    }

    @Test
    void testUpdateOrderFromReadyToRejected() throws DatabaseException, SQLException
    {
        Order order = orderMapper.getOrderById(2);
        assertEquals(OrderStatus.READY, order.getOrderStatus());

        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        order.setOrderStatus(OrderStatus.REJECTED);
        order.setCustomerComment("Prisen er desværre for høj");
        boolean updated = orderMapper.updateOrder(connection, order);
        connection.commit();
        assertTrue(updated);

        connection.close();

        Order rejectedOrder = orderMapper.getOrderById(2);
        assertEquals(OrderStatus.REJECTED, rejectedOrder.getOrderStatus());
        assertEquals("Prisen er desværre for høj", rejectedOrder.getCustomerComment());
    }

    @Test
    void testUpdateOrderNotFound() throws DatabaseException, SQLException
    {
        PricingDetails pricingDetails = new PricingDetails(15000.00, 40.0);

        Order fakeOrder = new Order(
                999,
                1,
                null,
                1,
                Timestamp.valueOf(LocalDateTime.now()),
                null,
                null,
                "Comment",
                OrderStatus.PENDING,
                pricingDetails
        );

        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        boolean updated = orderMapper.updateOrder(connection, fakeOrder);
        connection.commit();
        assertFalse(updated);

        connection.close();

    }

    @Test
    void testDeleteOrder() throws DatabaseException
    {
        boolean deleted = orderMapper.deleteOrder(1);

        assertTrue(deleted);

        assertThrows(DatabaseException.class, () -> orderMapper.getOrderById(1));
    }

    @Test
    void testDeleteOrderNotFound() throws DatabaseException
    {
        boolean deleted = orderMapper.deleteOrder(999);

        assertFalse(deleted);
    }

    @Test
    void testCompleteOrderWorkflow() throws DatabaseException, SQLException
    {
        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        Order request = orderMapper.createOrder(
                    connection,
                    1,
                    1,
                    "Ny carport ønskes",
                    new PricingDetails(17000.00, 40.0)
            );
        connection.commit();

        assertEquals(OrderStatus.PENDING, request.getOrderStatus());
        assertNull(request.getSellerId());
        assertEquals(40.0, request.getPricingDetails().getCoveragePercentage());
        assertEquals(17000.00, request.getPricingDetails().getCostPrice());

        connection.setAutoCommit(false);
        request.setSellerId(2);
        request.setOrderStatus(OrderStatus.READY);
        request.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        request.setOfferValidDays(14);
        request.setPricingDetails(new PricingDetails(18500.00, 45.0));

        boolean updated = orderMapper.updateOrder(connection, request);
        connection.commit();
        assertTrue(updated);

        Order readyOrder = orderMapper.getOrderById(request.getOrderId());
        assertEquals(OrderStatus.READY, readyOrder.getOrderStatus());
        assertEquals(2, readyOrder.getSellerId());
        assertEquals(45.0, readyOrder.getPricingDetails().getCoveragePercentage());

        connection.setAutoCommit(false);
        readyOrder.setOrderStatus(OrderStatus.ACCEPTED);
        updated = orderMapper.updateOrder(connection, readyOrder);
        connection.commit();
        assertTrue(updated);

        Order acceptedOrder = orderMapper.getOrderById(request.getOrderId());
        assertEquals(OrderStatus.ACCEPTED, acceptedOrder.getOrderStatus());

        connection.close();
    }
}