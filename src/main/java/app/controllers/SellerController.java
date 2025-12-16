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
import app.services.IMaterialService;
import app.services.IOrderService;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Locale;

public class SellerController
{
    private IOrderService orderService;
    private ICarportService carportService;
    private IMaterialService materialService;

    public SellerController(IOrderService orderService, ICarportService carportService, IMaterialService materialService)
    {
        this.orderService = orderService;
        this.carportService = carportService;
        this.materialService = materialService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/carport-requests", ctx -> showCarportRequests(ctx));
        app.get("/carport-request/details/{id}", ctx -> showRequestDetails(ctx));

        app.post("/requests/{id}/send-offer", ctx -> sendCarportOffer(ctx));
        app.post("/requests/{id}/update-bom", ctx -> updateBillOfMaterialQuantity(ctx));
        app.post("/requests/{id}/delete-bom-line", ctx -> deleteBillOfMaterialLine(ctx));
    }

    private void deleteBillOfMaterialLine(Context ctx)
    {
        if(!userIsAdmin(ctx)){return;}

        int orderId = Integer.parseInt(ctx.pathParam("id"));
        int materialLineId = Integer.parseInt(ctx.formParam("materialLineId"));

        try
        {
            double lineTotal = materialService.getLineTotalByMaterialId(materialLineId);

            if(lineTotal < 0)
            {
                ctx.sessionAttribute("errorMessage", "Fejl i hentning af linjetotalen");
                ctx.redirect("/carport-request/details/" + orderId);
                return;
            }

            boolean deleted = materialService.deleteBillOfMaterialLine(materialLineId);

            if (deleted)
            {
                Order order = orderService.getOrderById(orderId);
                double orderCostTotal = order.getPricingDetails().getCostPrice();
                double newOrderCostTotal = orderCostTotal - lineTotal;

                orderService.updateOrderCostPrice(orderId, newOrderCostTotal);
                ctx.sessionAttribute("successMessage", "Ordre linje blev slettet");
            }
            else
            {
                ctx.sessionAttribute("errorMessage", "Ordre linje blev ikke slettet");
            }

            ctx.redirect("/carport-request/details/" + orderId);
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/carport-request/details/" + orderId);
        }
    }

    private void updateBillOfMaterialQuantity(Context ctx)
    {
        if(!userIsAdmin(ctx)){return;}

        int orderId = Integer.parseInt(ctx.pathParam("id"));
        String materialLineIdString = ctx.formParam("materialLineId");
        String quantityString = ctx.formParam("quantity");

        if (materialLineIdString == null || quantityString == null)
        {
            ctx.sessionAttribute("errorMessage", "Manglende værdier");
            ctx.redirect("/carport-request/details/" + orderId);
            return;
        }

        try
        {
            int materialLineId = Integer.parseInt(materialLineIdString);
            int quantity = Integer.parseInt(quantityString);

            if (quantity < 0)
            {
                ctx.sessionAttribute("errorMessage", "Antal kan ikke være negativt");
                ctx.redirect("/carport-request/details/" + orderId);
                return;
            }

            double lineTotalDifference = materialService.calculateLinePriceDifference(materialLineId, quantity);
            boolean updated = materialService.updateBillOfMaterialLineQuantity(materialLineId, quantity);

            if (updated)
            {
                Order order = orderService.getOrderById(orderId);
                double orderCostTotal = order.getPricingDetails().getCostPrice();
                double newOrderCostTotal = orderCostTotal + lineTotalDifference;

                orderService.updateOrderCostPrice(orderId, newOrderCostTotal);
                ctx.sessionAttribute("successMessage", "Antal opdateret");
            }
            else
            {
                ctx.sessionAttribute("errorMessage", "Kunne ikke opdatere antal");
            }

            ctx.redirect("/carport-request/details/" + orderId);
        }
        catch (NumberFormatException e)
        {
            ctx.sessionAttribute("errorMessage", "Ugyldigt tal format");
            ctx.redirect("/carport-request/details/" + orderId);
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/carport-request/details/" + orderId);
        }
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
                ctx.sessionAttribute("successMessage", "Dit tilbud er afsendt");
                ctx.sessionAttribute("badgeNeedsUpdate", true);
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

            displayMessages(ctx);

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
            List<OrderOverviewDTO> orderRequests = orderService.getAllOrdersByStatus(OrderStatus.PENDING);
            ctx.attribute("orderRequests", orderRequests);
            displayMessages(ctx);

            ctx.render("admin-request.html");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke hente forespørgsler");
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
