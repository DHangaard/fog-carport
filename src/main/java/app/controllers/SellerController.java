package app.controllers;

import app.dto.OrderOverviewDTO;
import app.dto.UserDTO;
import app.entities.Order;
import app.entities.OrderDetail;
import app.entities.PricingDetails;
import app.enums.OrderStatus;
import app.enums.Role;
import app.exceptions.DatabaseException;
import app.services.ICarportService;
import app.services.IOrderService;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class SellerController
{
    private IOrderService orderService;
    private ICarportService carportService;

    public SellerController(IOrderService orderService, ICarportService carportService)
    {
        this.orderService = orderService;
        this.carportService = carportService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/carport-requests", ctx -> showCarportRequests(ctx));
        app.get("/carport-request/details/{id}", ctx -> showRequestDetails(ctx));

        app.post("/requests/{id}/send-offer", ctx -> sendCarportOffer(ctx));
        app.post("/requests/{id}/update-bom", ctx -> updateBillOfMaterial(ctx));
    }

    private void updateBillOfMaterial(Context ctx)
    {
    }

    private void sendCarportOffer(Context ctx)
    {
        if(!userIsAdmin(ctx)){return;}

        int orderId = Integer.parseInt(ctx.pathParam("id"));
        UserDTO seller = ctx.sessionAttribute("currentUser");

        String offerValidDaysString = ctx.formParam("offerValidDays");
        String coveragePercentageString = ctx.formParam("coveragePercentage");
        String costPriceString = ctx.formParam("costPrice");

        if (offerValidDaysString == null || coveragePercentageString == null || costPriceString == null)
        {
            ctx.sessionAttribute("errorMessage", "Formularen mangler værdier");
            ctx.redirect("/carport-request/details/" + orderId);
            return;
        }

        try
        {
            Integer offerValidDays = Integer.parseInt(offerValidDaysString);
            double coveragePercentage = Double.parseDouble(coveragePercentageString);
            double costPrice = Double.parseDouble(costPriceString);
            PricingDetails pricingDetails = new PricingDetails(costPrice, coveragePercentage);

            Order order = orderService.getOrderById(orderId);

            order.setSellerId(seller.userId());
            order.setOfferValidDays(offerValidDays);
            order.setPricingDetails(pricingDetails);
            order.setOrderStatus(OrderStatus.READY);

            boolean offerSend = orderService.confirmAndSendOffer(order);
            if(offerSend)
            {
                ctx.sessionAttribute("succesMessage", "Dit tilbud er afsendt");
            }
            else
            {
                ctx.sessionAttribute("errorMessage", "Dit tilbud blev ikke afsendt, prøv igen");
            }

            ctx.redirect("/carport-requests");
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/carport-request/details/" + orderId);
        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke hente de korrekte værdier ud til prisen, kun tal er muligt");
            ctx.redirect("/carport-request/details/" + orderId);
        }
    }

    private void showRequestDetails(Context ctx)
    {
        if(!userIsAdmin(ctx)){return;}

        int orderId = Integer.parseInt(ctx.pathParam("id"));
        Locale.setDefault(new Locale("US"));

        try
        {
            OrderDetail orderDetail = orderService.getOrderDetailByOrderId(orderId);
            CarportSvgTop carportSvgTop = carportService.getCarportTopSvgView(orderDetail.getCarport());
            CarportSvgSide carportSvgSide = carportService.getCarportSideSvgView(orderDetail.getCarport());
            ctx.attribute("orderDetail", orderDetail);
            ctx.attribute("carportSvgTop", carportSvgTop);
            ctx.attribute("carportSvgSide", carportSvgSide);

            ctx.render("admin-request-detail");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("admin-request");
        }
    }

    private void showCarportRequests(Context ctx)
    {
        if(!userIsAdmin(ctx)){return;}

        try
        {
            List<OrderOverviewDTO> orderOverviews = orderService.getAllOrdersByStatus(OrderStatus.PENDING);
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
