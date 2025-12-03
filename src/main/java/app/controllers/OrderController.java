package app.controllers;

import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.entities.OrderDetail;
import app.enums.OrderStatus;
import app.enums.Role;
import app.exceptions.DatabaseException;
import app.services.ICarportService;
import app.services.IOrderService;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderController
{
        private IOrderService orderService;
        private ICarportService carportService;

        public OrderController(IOrderService orderService, ICarportService carportService)
        {
            this.orderService = orderService;
            this.carportService = carportService;
        }

        public void addRoutes(Javalin app)
        {
            app.get("/offers", ctx -> showOfferOverview(ctx));
            app.get("/orders", ctx -> showOrderOverview(ctx));
            app.get("/carport/details/view/{id}", ctx -> showOrderDetail(ctx));

            app.post("/carport/order/delete/{id}", ctx -> deleteOrder(ctx));

        }

    private void deleteOrder(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));
        String pageType = ctx.formParam("pageType");

        try
        {
            boolean isDeleted = orderService.deleteOrder(orderId);

            if(isDeleted)
            {
                ctx.sessionAttribute("successMessage", "Ordren blev slettet");
            }
            else
            {
                ctx.sessionAttribute("errorMessage", "Kunne ikke slette ordren");
            }

            redirectToCorrectPath(ctx, pageType);
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            redirectToCorrectPath(ctx, pageType);
        }
    }

    private void showOrderDetail(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));
        String pageType = ctx.formParam("pageType");
        Locale.setDefault(new Locale("US"));

        try
        {
            OrderDetail orderDetail = orderService.getOrderDetailByOrderId(orderId);
            CarportSvgTop carportSvgTop = carportService.getCarportTopSvgView(orderDetail.getCarport());
            CarportSvgSide carportSvgSide = carportService.getCarportSideSvgView(orderDetail.getCarport());
            ctx.attribute("orderDetail", orderDetail);
            ctx.attribute("carportSvgTop", carportSvgTop);
            ctx.attribute("carportSvgSide", carportSvgSide);

            ctx.render("order-detail");
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            redirectToCorrectPath(ctx, pageType);
            System.out.println(e.getMessage());
        }
    }

    private void showOrderOverview(Context ctx)
    {
        if(!userIsAdmin(ctx)){return;}

        try
        {
            List<OrderStatus> statuses = List.of(OrderStatus.PAID, OrderStatus.CANCELLED);

            Map<OrderStatus, List<OrderOverviewDTO>> orderOverviews = orderService.getOrderOverViewsByStatus(statuses);

            ctx.attribute("paidOrders", orderOverviews.get(OrderStatus.READY));
            ctx.attribute("cancelledOrders", orderOverviews.get(OrderStatus.REJECTED));

            ctx.render("admin-orders");

        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", "Kunne ikke hente ordre: " + e.getMessage());
            ctx.redirect("/");
        }
    }

    private void showOfferOverview(Context ctx)
    {
        if(!userIsAdmin(ctx)) return;

        try
        {
            List<OrderStatus> statuses = List.of(OrderStatus.READY, OrderStatus.REJECTED, OrderStatus.EXPIRED);

            Map<OrderStatus, List<OrderOverviewDTO>> orderOverviews = orderService.getOrderOverViewsByStatus(statuses);

            ctx.attribute("offersToAccept", orderOverviews.get(OrderStatus.READY));
            ctx.attribute("rejectedOffers", orderOverviews.get(OrderStatus.REJECTED));
            ctx.attribute("expiredOffers", orderOverviews.get(OrderStatus.EXPIRED));

            ctx.render("admin-offers");

        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", "Kunne ikke hente tilbud: " + e.getMessage());
            ctx.redirect("/");
        }
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

    private void redirectToCorrectPath(Context ctx, String pageType)
    {
        switch (pageType)
        {
            case "requests":
                ctx.redirect("/carport-requests");
                break;
            case "offers":
                ctx.redirect("/offers");
                break;
            case "orders":
                ctx.redirect("/orders");
                break;
            default:
                ctx.redirect("/");
        }
    }
}
