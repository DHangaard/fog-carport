package app.controllers;

import app.dto.CreateOrderRequest;
import app.dto.UserDTO;
import app.entities.*;
import app.enums.RoofType;
import app.enums.ShedPlacement;
import app.exceptions.DatabaseException;
import app.services.*;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

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
        app.get("/show-carport-drawing/{id}", ctx -> showCarportDrawing(ctx));

        app.post("/request-carport", ctx -> handleCarportRequest(ctx));
        app.post("/confirm-request", ctx -> confirmCarportRequest(ctx));
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

            ctx.attribute("order", order);
            ctx.attribute("carportSvgTop", carportSvgTop);
            ctx.attribute("carportSvgSide", carportSvgSide);

            ctx.render("carport-drawing");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke hente tegning");
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

        UserDTO userFromContactPage = new UserDTO(
                currentUser.userId(),
                ctx.formParam("firstName"),
                ctx.formParam("lastName"),
                ctx.formParam("street"),
                Integer.parseInt(ctx.formParam("zipCode")),
                ctx.formParam("city"),
                ctx.formParam("email"),
                ctx.formParam("phoneNumber"),
                currentUser.role()
        );

        try
        {
            boolean userChangedContactInformation = userService.updateUser(userFromContactPage);

            if(userChangedContactInformation)
            {
                ctx.sessionAttribute("successMessage", "Dine kontakt oplysninger er opdateret!");
            }

            CreateOrderRequest createOrderRequest = new CreateOrderRequest(
                    currentUser.userId(),
                    carport,
                    customerNote
            );

            Order createdOrder = orderService.createPendingOrder(createOrderRequest);

            //TODO maybe use the order instead of carport request? in next view
            //ctx.sessionAttribute("carport", null);
            //ctx.sessionAttribute("customerNote", null);
            ctx.sessionAttribute("order", createdOrder);

            emailService.sendRequestConfirmation(userFromContactPage);
            ctx.render("request-confirmation");
        }
        //TODO catch entitie Exception here!
        catch (DatabaseException | IllegalArgumentException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            System.out.println(e.getMessage());
            ctx.render("request-offer-contact");
        }
    }

    private void handleCarportRequest(Context ctx)
    {
        if(!requireLogin(ctx)) {return;}

        String carportWidthString = ctx.formParam("carportWidth").trim();
        String carportLengthString = ctx.formParam("carportLength").trim();
        String roofType = ctx.formParam("roofType").trim();
        String shedWidthString = ctx.formParam("shedWidth").trim();
        String shedLengthString = ctx.formParam("shedLength").trim();
        String customerNote = ctx.formParam("customerNote").trim();

        Shed shed = null;

        try
        {
            int carportWidth = Integer.parseInt(carportWidthString);
            int carportLength = Integer.parseInt(carportLengthString);

            if(!shedLengthString.equals("NONE") && !shedWidthString.equals("NONE"))
            {
                int shedWidth = Integer.parseInt(shedWidthString);
                int shedLength = Integer.parseInt(shedLengthString);
                shed = new Shed(0, shedLength, shedWidth, ShedPlacement.FULL_WIDTH);
            }

            Carport carport = new Carport(0, carportLength, carportWidth, RoofType.valueOf(roofType), shed);

            carportService.validateCarport(carport);
            ctx.sessionAttribute("carport",carport);
            ctx.sessionAttribute("customerNote", customerNote);
            ctx.render("request-offer-contact");
        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Mål skal være et tal");
            showCarportFormular(ctx);
        }
        catch (IllegalArgumentException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            showCarportFormular(ctx);
        }
    }

    private void showCarportFormular(Context ctx)
    {
        if(!requireLogin(ctx)) {return;}

        List<Integer> carportWidthDimensions = getDimensionFromTo(240, 600, 30);
        ctx.attribute("carportWidths", carportWidthDimensions);

        List<Integer> carportLengthDimensions = getDimensionFromTo(240, 780, 30);
        ctx.attribute("carportLengths", carportLengthDimensions);

        List<Integer> shedWidthDimensions = getDimensionFromTo(210, 720, 30);
        ctx.attribute("shedWidths", shedWidthDimensions);

        List<Integer> shedLengthDimensions = getDimensionFromTo(150, 690, 30);
        ctx.attribute("shedLengths", shedLengthDimensions);

        ctx.render("request-offer-carport");
    }

    private void showBuildCarportPage(Context ctx)
    {
        ctx.render("build-carport");
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
}
