package app.controllers;

import app.dto.UserDTO;
import app.entities.MaterialVariant;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.enums.Role;
import app.exceptions.DatabaseException;
import app.services.IMaterialService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

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
        if (!userIsAdmin(ctx)) return;

        String searchType = ctx.queryParam("searchType");
        String query = ctx.queryParam("query");

        List<MaterialVariant> variants = List.of();

        if(searchType != null || !searchType.isEmpty()  && query != null || query.isEmpty())
        {

        try
        {
            variants = materialService.searchMaterials(searchType, query.trim());

            ctx.attribute("searchType", searchType);
            ctx.attribute("query", query);
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", "Søgning fejlede: " + e.getMessage());
            ctx.redirect("/materials");
        }
        catch (IllegalArgumentException e)
        {
            ctx.sessionAttribute("errorMessage", "Søgning fejlede: " + e.getMessage());
            ctx.redirect("/materials");
        }

        }


        displayMessages(ctx);
        ctx.attribute("variants", variants);
        ctx.render("materials");

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
