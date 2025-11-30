package app.controllers;

import app.services.ICarportService;
import io.javalin.Javalin;
import io.javalin.http.Context;

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

    }

    private void showBuildCarportPage(Context ctx)
    {
        ctx.render("build-carport");
    }
}
