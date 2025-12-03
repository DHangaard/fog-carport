package app.controllers;

import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;
import app.persistence.OrderMapper;
import app.services.OrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class CustomerController
{

    OrderService orderService;

    public CustomerController(OrderService orderService)
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
            int userId = ctx.sessionAttribute("userId");
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
