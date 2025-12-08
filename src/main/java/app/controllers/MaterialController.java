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
import org.jetbrains.annotations.NotNull;

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
        app.get("/materials/{id}/update-material", ctx -> showUpdateMaterialPage(ctx));
        app.get("/materials/create", ctx -> showCreateMaterialPage(ctx));
    }

    private void showCreateMaterialPage(Context ctx)
    {
        displayMessages(ctx);
        ctx.attribute("categories", MaterialCategory.values());
        ctx.attribute("types", MaterialType.values());
        ctx.render("material-create");
    }

    private void showUpdateMaterialPage(Context ctx)
    {
        int materialVariantId = Integer.parseInt(ctx.pathParam("id"));
        displayMessages(ctx);

        try
        {
            MaterialVariant materialVariant = materialService.getMaterialVariantById(materialVariantId);
            ctx.attribute("materialVariant", materialVariant);
            ctx.attribute("categories", MaterialCategory.values());
            ctx.attribute("types", MaterialType.values());
            ctx.render("material-update");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage",e.getMessage());
        }
    }

    private void showMaterialsPage(Context ctx)
    {
        if (!userIsAdmin(ctx)) return;
        displayMessages(ctx);

        String searchType = ctx.queryParam("searchType");
        String query = ctx.queryParam("query");

        List<MaterialVariant> variants = List.of();

        if(hasSearch(searchType) && hasSearch(query))
        {

            try
            {
                variants = materialService.searchMaterials(searchType, query.trim());

                ctx.attribute("searchType", searchType);
                ctx.attribute("query", query);
            }
            catch (DatabaseException e)
            {
                ctx.sessionAttribute("errorMessage","Søgning fejlede: " + e.getMessage());
                ctx.redirect("/materials");
            }
            catch (IllegalArgumentException e)
            {
                ctx.sessionAttribute("errorMessage","Søgning fejlede: " + e.getMessage());
                ctx.redirect("/materials");
            }
        }

        ctx.attribute("variants", variants);
        ctx.render("materials");
    }

    private boolean hasSearch(String value)
    {
        return value != null && !value.isBlank();
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
