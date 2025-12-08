package app.controllers;

import app.entities.Material;
import app.services.IMaterialService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class MaterialController
{
    private IMaterialService materialService;

    public MaterialController(IMaterialService materialService)
    {
        this.materialService = materialService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/materials", ctx -> showMaterialsPage(ctx));
    }

    private void showMaterialsPage(Context ctx)
    {

    }
}
