package app;

import app.config.ThymeleafConfig;
import app.controllers.*;
import app.persistence.*;
import app.services.*;
import app.util.BeforeHandlersUtil;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.logging.Logger;

public class Main
{
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String USER = System.getenv("JDBC_USER") != null ?
            System.getenv("JDBC_USER") : "postgres";

    private static final String PASSWORD = System.getenv("JDBC_PASSWORD") != null ?
            System.getenv("JDBC_PASSWORD") : "postgres";

    private static final String URL = System.getenv("JDBC_CONNECTION_STRING") != null ?
            System.getenv("JDBC_CONNECTION_STRING") : "jdbc:postgresql://localhost:5432/%s?currentSchema=public";

    private static final String DB = System.getenv("JDBC_DB") != null ?
            System.getenv("JDBC_DB") : "carport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args)
    {
        Javalin app = Javalin.create(config ->
        {
            config.staticFiles.add("/public");
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
            config.staticFiles.add("/templates");
        }).start(7070);

        UserMapper userMapper = new UserMapper(connectionPool);
        ZipCodeMapper zipCodeMapper = new ZipCodeMapper(connectionPool);
        ShedMapper shedMapper = new ShedMapper(connectionPool);
        CarportMapper carportMapper = new CarportMapper(connectionPool);
        MaterialVariantMapper materialVariantMapper = new MaterialVariantMapper(connectionPool);
        MaterialLineMapper materialLineMapper = new MaterialLineMapper(connectionPool);
        OrderMapper orderMapper = new OrderMapper(connectionPool);

        IBomService bomService = new BomService(materialVariantMapper);
        IUserService userService = new UserService(userMapper, zipCodeMapper);
        ICarportService carportService = new CarportService(carportMapper);
        IEmailService emailService = new SendGridEmailService();
        IMaterialService materialService = new MaterialService(materialLineMapper);
        IOrderService orderService = new OrderService(userMapper, materialLineMapper, shedMapper, carportMapper, orderMapper, bomService, emailService, connectionPool);

        UserController userController = new UserController(userService);
        CarportController carportController = new CarportController(carportService, userService, emailService, orderService);
        SellerController sellerController = new SellerController(orderService, carportService, materialService);
        CustomerController customerController = new CustomerController(orderService, carportService);
        OrderController orderController = new OrderController(orderService, carportService);

        app.before(ctx -> BeforeHandlersUtil.addBagdeCount(ctx, orderService));
        userController.addRoutes(app);
        carportController.addRoutes(app);
        sellerController.addRoutes(app);
        customerController.addRoutes(app);
        orderController.addRoutes(app);
    }
}
