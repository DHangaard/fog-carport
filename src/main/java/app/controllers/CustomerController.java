package app.controllers;

import app.dto.OrderOverviewDTO;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class CustomerController
{

    public CustomerController()
    {

    }

    public void addRoutes(Javalin app)
    {
        app.get("/my-page", ctx -> showCustomerPage(ctx));
    }


    private void showCustomerPage(Context ctx)
    {
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
}
