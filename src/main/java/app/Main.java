package app;

import app.config.ThymeleafConfig;
import app.controllers.CarportController;
import app.controllers.UserController;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import app.persistence.ZipCodeMapper;
import app.services.*;
import app.util.AppProperties;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.logging.Logger;

public class Main
{
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String USER = System.getenv("JDBC_USER") != null ?
            System.getenv("JDBC_USER") : AppProperties.getRequired("db.user");

    private static final String PASSWORD = System.getenv("JDBC_PASSWORD") != null ?
            System.getenv("JDBC_PASSWORD") : AppProperties.getRequired("db.password");

    private static final String URL = System.getenv("JDBC_CONNECTION_STRING") != null ?
            System.getenv("JDBC_CONNECTION_STRING") : AppProperties.getRequired("db.url");

    private static final String DB = System.getenv("JDBC_DB") != null ?
            System.getenv("JDBC_DB") : AppProperties.getRequired("db.dbname");

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args)
    {
        Javalin app = Javalin.create(config ->
        {
            config.staticFiles.add("/public");
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
            config.staticFiles.add("/templates");
        }).start(AppProperties.getRequiredInt("server.port"));

        UserMapper userMapper = new UserMapper(connectionPool);
        ZipCodeMapper zipCodeMapper = new ZipCodeMapper(connectionPool);
        IUserService userService = new UserService(userMapper, zipCodeMapper);
        ICarportService carportService = new CarportService();
        IEmailService emailService = new SendGridEmailService();

        UserController userController = new UserController(userService);
        CarportController carportController = new CarportController(carportService, userService, emailService);

        userController.addRoutes(app);
        carportController.addRoutes(app);
    }
}
