package app;

import app.config.SessionConfig;
import app.config.ThymeleafConfig;
import app.controllers.*;
import app.persistence.*;
import app.services.*;
import app.util.BeforeHandlersUtil;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

public class Main
{
    private static final String USER = System.getenv("JDBC_USER");
    private static final String PASSWORD = System.getenv("JDBC_PASSWORD");
    private static final String URL = System.getenv("JDBC_CONNECTION_STRING");
    private static final String DB = System.getenv("JDBC_DB");

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args)
    {
        Javalin app = Javalin.create(config ->
        {
            config.staticFiles.add("/public");
            config.jetty.modifyServletContextHandler(handler ->  handler.setSessionHandler(SessionConfig.sessionConfig()));
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
        }).start(7070);

        UserMapper userMapper = new UserMapper(connectionPool);
        ZipCodeMapper zipCodeMapper = new ZipCodeMapper(connectionPool);
        ShedMapper shedMapper = new ShedMapper(connectionPool);
        CarportMapper carportMapper = new CarportMapper(connectionPool);
        MaterialVariantMapper materialVariantMapper = new MaterialVariantMapper(connectionPool);
        MaterialLineMapper materialLineMapper = new MaterialLineMapper(connectionPool);
        MaterialMapper materialMapper = new MaterialMapper(connectionPool);
        OrderMapper orderMapper = new OrderMapper(connectionPool);

        IBomService bomService = new BomService(materialVariantMapper);
        IUserService userService = new UserService(userMapper, zipCodeMapper);
        ICarportService carportService = new CarportService(carportMapper);
        IEmailService emailService = new SendGridEmailService();
        IMaterialService materialService = new MaterialService(materialLineMapper, materialVariantMapper, materialMapper, connectionPool);
        IOrderService orderService = new OrderService(userMapper, materialLineMapper, shedMapper, carportMapper, orderMapper, bomService, emailService, connectionPool);

        UserController userController = new UserController(userService);
        CarportController carportController = new CarportController(carportService, userService, emailService, orderService);
        SellerController sellerController = new SellerController(orderService, carportService, materialService);
        CustomerController customerController = new CustomerController(orderService, carportService, emailService);
        OrderController orderController = new OrderController(orderService, carportService);
        MaterialController materialController = new MaterialController(materialService);

        app.before(ctx -> BeforeHandlersUtil.addBagdeCount(ctx, orderService));
        userController.addRoutes(app);
        carportController.addRoutes(app);
        sellerController.addRoutes(app);
        customerController.addRoutes(app);
        orderController.addRoutes(app);
        materialController.addRoutes(app);
    }
}
