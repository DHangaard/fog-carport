package app.controllers;

import app.dto.UserDTO;
import app.entities.Carport;
import app.entities.Shed;
import app.enums.RoofType;
import app.enums.ShedPlacement;
import app.services.ICarportService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class CarportController
{
    private ICarportService carportService;

    public CarportController(ICarportService carportService)
    {
        this.carportService = carportService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/carporte", ctx -> showBuildCarportPage(ctx));
        app.get("/carport-formular", ctx -> showCarportFormular(ctx));

        app.post("/request-offer", ctx -> handleCarportRequest(ctx));
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
