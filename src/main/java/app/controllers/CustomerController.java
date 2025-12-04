package app.controllers;

import app.dto.CustomerOfferDTO;
import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;
import app.services.ICarportService;
import app.services.IOrderService;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class CustomerController
{

    private IOrderService orderService;
    private ICarportService carportService;

    public CustomerController(IOrderService orderService, ICarportService carportService)
    {
        this.orderService = orderService;
        this.carportService = carportService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/my-page", ctx -> showCustomerPage(ctx));
        app.get("/customer-offer/details/{id}", ctx -> showCustomerOfferDetails(ctx));

        app.post("/customer-offer/{id}/accept", ctx -> acceptCarportOffer(ctx));
        app.post("customer-offer/{id}/decline", ctx -> declineCarportOffer(ctx));
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
            ctx.redirect("/");
        }
        ctx.render("my-page.html");
    }

    private void showCustomerOfferDetails(Context ctx)
    {
        if(!requireLogin(ctx)) {return;}
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            CustomerOfferDTO offer = orderService.getCustomerOfferByOrderId(orderId);
            CarportSvgTop carportSvgTop = carportService.getCarportTopSvgView(offer.carport());
            CarportSvgSide carportSvgSide = carportService.getCarportSideSvgView(offer.carport());
            ctx.attribute("carportSvgTop", carportSvgTop);
            ctx.attribute("carportSvgSide", carportSvgSide);
            ctx.attribute("offer", offer);

            ctx.render("customer-offer-detail.html");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke hente ordrer");
            ctx.redirect("/my-page");
        }
    }

    private void acceptCarportOffer(Context ctx)
    {

    }

    private void declineCarportOffer(Context ctx)
    {

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
