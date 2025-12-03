package app.controllers;

import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;
import app.services.IOrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class CustomerController
{

    private IOrderService orderService;

    public CustomerController(IOrderService orderService)
    {
        this.orderService = orderService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/my-page", ctx -> showCustomerPage(ctx));
    }


    private void showCustomerPage(Context ctx)
    {
        if(!requireLogin(ctx)) {return;}

        try
        {
            UserDTO customer = ctx.sessionAttribute("currentUser");
            int userId = customer.userId();

            List<OrderOverviewDTO> pendingOrders = orderService.getAllOrdersByUserIdAndStatus(userId, OrderStatus.PENDING);
            List<OrderOverviewDTO> readyOrders = orderService.getAllOrdersByUserIdAndStatus(userId, OrderStatus.READY);
            List<OrderOverviewDTO> paidOrders = orderService.getAllOrdersByUserIdAndStatus(userId, OrderStatus.PAID);

            ctx.attribute("pendingOrders", pendingOrders);
            ctx.attribute("readyOrders", readyOrders);
            ctx.attribute("paidOrders", paidOrders);
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke hente ordrer");
            System.out.println(e.getMessage());
            ctx.redirect("/");
        }
        ctx.render("my-page.html");
    }

    private boolean requireLogin(Context ctx)
    {
        UserDTO userDTO = ctx.sessionAttribute("currentUser");

        if(userDTO == null)
        {
            ctx.sessionAttribute("errorMessage", "Du skal logge ind eller oprette en bruger for at tilgå \"Min side\"");
            ctx.sessionAttribute("loginRedirect", ctx.path());
            ctx.redirect("/login");
            return false;
        }
        return true;
    }
}
