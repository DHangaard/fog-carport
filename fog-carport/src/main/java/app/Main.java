package app;

import app.config.ThymeleafConfig;
import app.persistence.ConnectionPool;
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

        app.get("/", ctx -> ctx.render("/index"));
    }
}
