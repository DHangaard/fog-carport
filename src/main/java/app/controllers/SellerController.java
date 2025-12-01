package app.controllers;

import app.dto.UserDTO;
import app.enums.Role;
import app.services.IOrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class SellerController
{
    private IOrderService orderService;

    public SellerController(IOrderService orderService)
    {
        this.orderService = orderService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/carport-requests", ctx -> showCarportRequests(ctx));
        app.get("/carport-request/details/{id}", ctx -> showRequestDetails(ctx));

    }

    private void showRequestDetails(Context ctx)
    {
        if(!userIsAdmin(ctx)){return;}
        
        ctx.render("admin-request");
    }

    private void showCarportRequests(Context ctx)
    {
    }

    private boolean userIsAdmin(Context ctx)
    {
        UserDTO userDTO = ctx.sessionAttribute("currentUser");

        if(userDTO == null)
        {
            ctx.sessionAttribute("errorMessage", "Du skal logge ind for at tilgå denne side");
            ctx.sessionAttribute("loginRedirect", ctx.path());
            ctx.redirect("/login");
            return false;
        }

        return userDTO.role().equals(Role.SALESREP);
    }
}
