package app.controllers;

import app.entities.Carport;
import app.entities.Shed;
import app.enums.RoofType;
import app.enums.ShedPlacement;
import app.exceptions.DatabaseException;
import app.services.ICarportService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

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
        String carportWidthString = ctx.formParam("carportWidth");
        String carportLengthString = ctx.formParam("carportLength");
        String roofType = ctx.formParam("roofType");
        String shedWidthString = ctx.formParam("shedWidth");
        String shedLengthString = ctx.formParam("shedLength");
        String customerNote = ctx.formParam("customerNote");

        Carport carport;
        Shed shed;

        try
        {
            int carportWidth = Integer.parseInt(carportWidthString);
            int carportLength = Integer.parseInt(carportLengthString);

            if(!shedLengthString.equals("NONE") && shedWidthString.equals("NONE"))
            {
                int shedWidth = Integer.parseInt(shedWidthString);
                int shedLength = Integer.parseInt(shedLengthString);
                shed = new Shed(0, shedLength, shedWidth, ShedPlacement.FULL_WIDTH);
            }

            carport = new Carport(0, carportWidth, carportLength, RoofType.FLAT, shed);

            carportService.validateCarport(carport);


        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Mål skal være et tal");
            ctx.redirect("request-offer-carport");
        }

        catch (IllegalArgumentException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("request-offer-carport");
        }
    }

    private void showCarportFormular(Context ctx)
    {
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
}
