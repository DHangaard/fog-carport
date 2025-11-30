package app.controllers;

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
