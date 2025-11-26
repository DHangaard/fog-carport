package app;

import app.config.ThymeleafConfig;
import app.controllers.UserController;
import app.entities.Carport;
import app.entities.MaterialLine;
import app.enums.RoofType;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.MaterialVariantMapper;
import app.persistence.UserMapper;
import app.persistence.ZipCodeMapper;
import app.services.BomService;
import app.services.IUserService;
import app.services.UserService;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.ArrayList;
import java.util.List;
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

    public static void main(String[] args) throws DatabaseException
    {
        Javalin app = Javalin.create(config ->
        {
            config.staticFiles.add("/public");
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
            config.staticFiles.add("/templates");
        }).start(7070);

        UserMapper userMapper = new UserMapper(connectionPool);
        ZipCodeMapper zipCodeMapper = new ZipCodeMapper(connectionPool);
        IUserService userService = new UserService(userMapper, zipCodeMapper);

        UserController userController = new UserController(userService);

        userController.addRoutes(app);

        MaterialVariantMapper mvm = new MaterialVariantMapper(connectionPool);
        BomService bom = new BomService(mvm);
        Carport carport = new Carport(0, 420, 600, RoofType.FLAT, null);
        List<MaterialLine> materials = bom.getBillOfMaterialByCarport(carport);
        materials.stream()
                .forEach(System.out::println);

    }
}
