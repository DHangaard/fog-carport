package app.controllers;

import app.dto.CustomerOfferDTO;
import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.entities.Order;
import app.entities.OrderDetail;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;
import app.services.ICarportService;
import app.services.IEmailService;
import app.services.IOrderService;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Locale;

public class CustomerController
{

    private IOrderService orderService;
    private ICarportService carportService;
    private IEmailService emailService;

    public CustomerController(IOrderService orderService, ICarportService carportService, IEmailService emailService)
    {
        this.orderService = orderService;
        this.carportService = carportService;
        this.emailService = emailService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/my-page", ctx -> showCustomerPage(ctx));
        app.get("/customer-offer/details/{id}", ctx -> showCustomerOfferDetails(ctx));

        app.post("/customer-offer/{id}/accept", ctx -> acceptCarportOffer(ctx));
        app.post("/customer-offer/{id}/reject", ctx -> rejectCarportOffer(ctx));
    }


    private void showCustomerPage(Context ctx)
    {
        if (!requireLogin(ctx))
        {
            return;
        }

        displayMessages(ctx);

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
        if (!requireLogin(ctx))
        {
            return;
        }

        displayMessages(ctx);

        int orderId = Integer.parseInt(ctx.pathParam("id"));
        Locale.setDefault(new Locale("US"));

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

    private void handleOffer(Context ctx, OrderStatus orderStatus, String acceptOrDeny)
    {
        if (!requireLogin(ctx))
        {
            return;
        }
        int orderId = Integer.parseInt(ctx.pathParam("id"));
        UserDTO currentUser =  ctx.sessionAttribute("currentUser");

        try
        {
            Order order = orderService.getOrderById(orderId);
            order.setOrderStatus(orderStatus);
            boolean isUpdated = orderService.updateOrder(order);

            if (isUpdated)
            {
                if(orderStatus.equals(OrderStatus.ACCEPTED))
                {
                    OrderDetail orderDetail = orderService.getOrderDetailByOrderId(orderId);
                    emailService.sendOrderConfirmation(currentUser, orderDetail);
                }
                ctx.sessionAttribute("successMessage", "Tilbuddet er " + acceptOrDeny);
                ctx.redirect("/my-page");
            }
            else
            {
                ctx.sessionAttribute("errorMessage", "Der er sket en fejl, prøv igen");
                ctx.redirect("/customer-offer-detail");
            }
            ctx.redirect("/my-page");
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/customer-offer-detail");
        }
    }

    private void acceptCarportOffer(Context ctx)
    {
        String accepted = "accepteret";
        handleOffer(ctx, OrderStatus.PAID, accepted);
    }

    private void rejectCarportOffer(Context ctx)
    {
        String rejected = "afslået";
        handleOffer(ctx, OrderStatus.REJECTED, rejected);
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

    private boolean requireLogin(Context ctx)
    {
        UserDTO userDTO = ctx.sessionAttribute("currentUser");

        if (userDTO == null)
        {
            ctx.sessionAttribute("errorMessage", "Du skal logge ind eller oprette en bruger for at tilgå \"Min side\"");
            ctx.sessionAttribute("loginRedirect", ctx.path());
            ctx.redirect("/login");
            return false;
        }
        return true;
    }
}
