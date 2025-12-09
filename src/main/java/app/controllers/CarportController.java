package app.controllers;

import app.dto.CreateOrderRequestDTO;
import app.dto.UserDTO;
import app.entities.*;
import app.enums.Role;
import app.enums.RoofType;
import app.exceptions.DatabaseException;
import app.services.*;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarportController
{
    private ICarportService carportService;
    private IUserService userService;
    private IEmailService emailService;
    private IOrderService orderService;

    public CarportController(ICarportService carportService, IUserService userService, IEmailService emailService, IOrderService orderService)
    {
        this.carportService = carportService;
        this.userService = userService;
        this.emailService = emailService;
        this.orderService = orderService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/carporte", ctx -> showBuildCarportPage(ctx));
        app.get("/carport-formular", ctx -> showCarportFormular(ctx));
        app.get("/request-offer-contact", ctx -> showRequestOfferContact(ctx));
        app.get("/show-carport-drawing/{id}", ctx -> showCarportDrawing(ctx));
        app.get("/requests/{id}/update-carport", ctx -> showUpdateCarportFormular(ctx));

        app.post("/request-carport", ctx -> handleCarportRequest(ctx));
        app.post("/confirm-request", ctx -> confirmCarportRequest(ctx));
        app.post("/requests/{id}/update-carport", ctx -> handleUpdateCarportDimensions(ctx));
    }

    private void showUpdateCarportFormular(Context ctx)
    {
        if(!userIsAdmin(ctx)){return;}

        int orderId = Integer.parseInt(ctx.pathParam("id"));
        displayMessages(ctx);

        try
        {
            Order order = orderService.getOrderById(orderId);
            Carport carport = carportService.getCarportByCarportId(order.getCarportId());

            fillCarportDimensionAttributes(ctx);
            ctx.attribute("carport", carport);
            ctx.attribute("orderDetail", order);
            ctx.render("admin-update-carport");
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", "Kunne ikke hente carport data: " + e.getMessage());
            ctx.redirect("/carport-requests");
        }
    }

    private void handleUpdateCarportDimensions(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            Carport updatedCarport = buildCarportFromRequest(ctx);
            Order order = orderService.getOrderById(orderId);
            Carport oldCarport = carportService.getCarportByCarportId(order.getCarportId());

            updatedCarport.setCarportId(oldCarport.getCarportId());

            if(updatedCarport.getShed() != null && oldCarport.getShed() != null)
            {
                updatedCarport.getShed().setShedId(oldCarport.getShed().getShedId());
            }

            boolean isUpdated = orderService.updateCarportAndBillOfMaterials(orderId, updatedCarport);

            if(isUpdated)
            {
                ctx.sessionAttribute("successMessage", "Du har opdateret carport mål tilhørende ordre nr: " + order.getOrderId());
            }
            else
            {
                ctx.sessionAttribute("errorMessage", "Noget gik galt ved opdatering af carport tilhørende ordre nr: " + order.getOrderId());
            }

            ctx.redirect("/carport-request/details/" + orderId);

        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/carport-request/details/" + orderId);
        }

    }

    private void showRequestOfferContact(Context ctx)
    {
        if (!requireLogin(ctx)) return;

        displayMessages(ctx);

        UserDTO currentUser = ctx.sessionAttribute("currentUser");
        Carport carport = ctx.sessionAttribute("carport");

        if (carport == null)
        {
            ctx.sessionAttribute("errorMessage", "Session udløbet. Gå tilbage og udfyld formularen igen.");
            ctx.redirect("/carport-formular");
            return;
        }

        ctx.attribute("user", currentUser);
        ctx.render("request-offer-contact");
    }

    private void showCarportDrawing(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));
        Locale.setDefault(new Locale("US"));

        try
        {
            Order order = orderService.getOrderById(orderId);
            Carport carport = carportService.getCarportByCarportId(order.getCarportId());
            CarportSvgTop carportSvgTop = carportService.getCarportTopSvgView(carport);
            CarportSvgSide carportSvgSide = carportService.getCarportSideSvgView(carport);

            displayMessages(ctx);
            ctx.attribute("order", order);
            ctx.attribute("carportSvgTop", carportSvgTop);
            ctx.attribute("carportSvgSide", carportSvgSide);

            ctx.render("carport-drawing");
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", "Kunne ikke hente tegning");
            ctx.redirect("/carport-requests");
        }
    }

    private void confirmCarportRequest(Context ctx)
    {
        if(!requireLogin(ctx)) {return;}

        UserDTO currentUser = ctx.sessionAttribute("currentUser");
        Carport carport = ctx.sessionAttribute("carport");
        String customerNote = ctx.sessionAttribute("customerNote");

        if (currentUser == null || carport == null)
        {
            ctx.attribute("error", "Session udløbet. Prøv igen.");
            ctx.redirect("/");
            return;
        }

        try
        {
            String firstName = ctx.formParam("firstName");
            String lastName = ctx.formParam("lastName");
            String street = ctx.formParam("street");
            String zipCodeString = ctx.formParam("zipCode");
            String city = ctx.formParam("city");
            String email = ctx.formParam("email");
            String phoneNumber = ctx.formParam("phoneNumber");

            int zipCode;

            try
            {
                zipCode = Integer.parseInt(zipCodeString);
            }
            catch (NumberFormatException e)
            {
                ctx.sessionAttribute("errorMessage", "Postnummer skal være et tal");
                ctx.redirect("/request-offer-contact");
                return;
            }

            UserDTO userFromContactPage = new UserDTO(
                    currentUser. userId(),
                    firstName,
                    lastName,
                    street,
                    zipCode,
                    city,
                    email,
                    phoneNumber,
                    currentUser.role()
            );

            boolean userChangedContactInformation = userService.updateUser(userFromContactPage);

            if(userChangedContactInformation)
            {
                ctx.sessionAttribute("currentUser", userFromContactPage);
                ctx.sessionAttribute("successMessage", "Dine kontakt oplysninger er opdateret!");
            }

            CreateOrderRequestDTO createOrderRequest = new CreateOrderRequestDTO(
                    currentUser.userId(),
                    carport,
                    customerNote
            );

            Order createdOrder = orderService.createPendingOrder(createOrderRequest);
            emailService.sendRequestConfirmation(userFromContactPage);

            ctx.sessionAttribute("order", createdOrder);
            ctx.render("request-confirmation");
        }

        catch (DatabaseException | IllegalArgumentException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/request-offer-contact");
        }
    }

    private void handleCarportRequest(Context ctx)
    {
        if(!requireLogin(ctx)) {return;}

        String customerNote = ctx.formParam("customerNote").trim();

        try
        {
            Carport carport = buildCarportFromRequest(ctx);

            ctx.sessionAttribute("carport",carport);
            ctx.sessionAttribute("customerNote", customerNote);

            ctx.redirect("/request-offer-contact");
        }
        catch (NumberFormatException e)
        {
            ctx.sessionAttribute("errorMessage", "Mål skal være et tal");
            ctx.redirect("/carport-formular");
        }
        catch (IllegalArgumentException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/carport-formular");
        }
    }

    private void showCarportFormular(Context ctx)
    {
        if(!requireLogin(ctx)) {return;}
        displayMessages(ctx);

        fillCarportDimensionAttributes(ctx);
        ctx.render("request-offer-carport");
    }

    private void fillCarportDimensionAttributes(Context ctx)
    {
        List<Integer> carportWidthDimensions = getDimensionFromTo(240, 600, 30);
        ctx.attribute("carportWidths", carportWidthDimensions);

        List<Integer> carportLengthDimensions = getDimensionFromTo(240, 780, 30);
        ctx.attribute("carportLengths", carportLengthDimensions);

        List<Integer> shedWidthDimensions = getDimensionFromTo(200, 530, 30);
        ctx.attribute("shedWidths", shedWidthDimensions);

        List<Integer> shedLengthDimensions = getDimensionFromTo(150, 720, 30);
        ctx.attribute("shedLengths", shedLengthDimensions);
    }

    private void showBuildCarportPage(Context ctx)
    {
        displayMessages(ctx);
        ctx.render("build-carport");
    }

    private Carport buildCarportFromRequest(Context ctx)
    {
        int carportWidth = Integer.parseInt(ctx.formParam("carportWidth").trim());
        int carportLength = Integer.parseInt(ctx.formParam("carportLength").trim());
        String roofType = ctx.formParam("roofType").trim();
        String shedWidthString = ctx.formParam("shedWidth").trim();
        String shedLengthString = ctx.formParam("shedLength").trim();

        Shed shed = null;

        if(!shedLengthString.equals("NONE") && !shedWidthString.equals("NONE"))
        {
            int shedWidth = Integer.parseInt(shedWidthString);
            int shedLength = Integer.parseInt(shedLengthString);

            shed = carportService.createShedWithPlacement(carportWidth, shedWidth, shedLength);
        }

        Carport carport = new Carport(0, carportLength, carportWidth, RoofType.valueOf(roofType), shed);

        carportService.validateCarport(carport);

        return carport;
    }

    private List<Integer> getDimensionFromTo(int from, int to, int increment)
    {
        List<Integer> dimensions = new ArrayList<>();

        for(int i = from; i <= to; i+= increment)
        {
            dimensions.add(i);
        }
        return dimensions;
    }

    private boolean requireLogin(Context ctx)
    {
        UserDTO userDTO = ctx.sessionAttribute("currentUser");

        if(userDTO == null)
        {
            ctx.sessionAttribute("errorMessage", "Du skal logge ind eller oprette en bruger for at lave en carport forespørgsel");
            ctx.sessionAttribute("loginRedirect", ctx.path());
            ctx.redirect("/login");
            return false;
        }
        return true;
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
