package app.controllers;

import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.enums.OrderStatus;
import app.enums.Role;
import app.exceptions.DatabaseException;
import app.services.IOrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

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

    }

    private void showCarportRequests(Context ctx)
    {
        if(!userIsAdmin(ctx)){return;}

        try
        {
            List<OrderOverviewDTO> orderOverviews = orderService.getAllOrdersByStatus(OrderStatus.PENDING);
            orderOverviews.get(0).customerRequestCreatedAt();
            ctx.attribute("orderOverviews", orderOverviews);

        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke hente forespørgsler");
            System.out.println(e.getMessage());
            ctx.redirect("/");
        }
        ctx.render("admin-request.html");
    }

    private boolean userIsAdmin(Context ctx)
    {
        UserDTO userDTO = ctx.sessionAttribute("currentUser");

        if(userDTO == null)
        {
            ctx.sessionAttribute("errorMessage", "Du skal logge ind for at tilgå denne side");
            ctx.redirect("/login");
            return false;
        }

        return userDTO.role().equals(Role.SALESREP);
    }
}
