package app.controllers;

import app.dto.CreateUserRequestDTO;
import app.dto.UserDTO;
import app.exceptions.DatabaseException;
import app.services.IUserService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class UserController
{
    private IUserService userService;

    public UserController(IUserService userService)
    {
        this.userService = userService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/", ctx -> showHomepage(ctx));
        app.get("/login", ctx -> showLoginPage(ctx));
        app.get("/register", ctx -> showCreateUserPage(ctx));

        app.post("/logout", ctx -> logOut(ctx));
        app.post("/register", ctx -> handleCreateUser(ctx));
        app.post("/login", ctx -> handleUserLogin(ctx));
    }

    private void showHomepage(Context ctx)
    {
        displayMessages(ctx);
        ctx.render("index");
    }

    private void logOut(Context ctx)
    {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }

    private void handleUserLogin(Context ctx)
    {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        try
        {
            UserDTO user = userService.login(email, password);
            ctx.sessionAttribute("currentUser", user);

            String redirectPath = ctx.sessionAttribute("loginRedirect");

            if(redirectPath != null)
            {
                ctx.sessionAttribute("loginRedirect", null);
                ctx.redirect(redirectPath);
            }
            else
            {
                ctx.redirect("/");
            }
        }
        catch (DatabaseException  e)
        {
            ctx.sessionAttribute("errorMessage", "Forkert email eller password");
            ctx.redirect("/login");
        }
        catch (IllegalArgumentException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/login");
        }
    }

    private void handleCreateUser(Context ctx)
    {
        try
        {
            int zipCode = Integer.parseInt(ctx.formParam("zipCode"));

            CreateUserRequestDTO createUserRequestDTO = new CreateUserRequestDTO(
                    ctx.formParam("firstName"),
                    ctx.formParam("lastName"),
                    ctx.formParam("email"),
                    ctx.formParam("password1"),
                    ctx.formParam("password2"),
                    ctx.formParam("phoneNumber"),
                    ctx.formParam("street"),
                    zipCode
            );

            userService.registerUser(createUserRequestDTO);
            ctx.sessionAttribute("successMessage", "Du har oprettet en bruger! Log på med email og password");
            ctx.redirect("login");
        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Postnummer skal være et tal");
            populateFormFields(ctx);
            ctx.render("create-user");
        }

        catch (DatabaseException | IllegalArgumentException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            populateFormFields(ctx);
            ctx.render("create-user");
        }
    }

    private void showCreateUserPage(Context ctx)
    {
        displayMessages(ctx);
        ctx.render("create-user");
    }

    private void showLoginPage(Context ctx)
    {
        displayMessages(ctx);
        ctx.render("login");
    }

    private static void populateFormFields(Context ctx) {
        ctx.attribute("firstName", ctx.formParam("firstName"));
        ctx.attribute("lastName", ctx.formParam("lastName"));
        ctx.attribute("email", ctx.formParam("email"));
        ctx.attribute("phoneNumber", ctx.formParam("phoneNumber"));
        ctx.attribute("street", ctx.formParam("street"));
        ctx.attribute("zipCode", ctx.formParam("zipCode"));
    }

    private void displayMessages(Context ctx)
    {
        String errorMessage = ctx.sessionAttribute("errorMessage");
        String successMessage = ctx.sessionAttribute("successMessage");

        ctx.attribute("errorMessage", errorMessage);
        ctx.attribute("successMessage", successMessage);

        ctx.sessionAttribute("errorMessage", null);
        ctx.sessionAttribute("successMessage", null);
    }
}
