package app.controllers;

import app.dto.UserDTO;
import app.entities.Material;
import app.entities.MaterialVariant;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.enums.Role;
import app.exceptions.DatabaseException;
import app.services.IMaterialService;
import app.util.ValidationUtil;
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
        app.get("/materials/{id}/update-material", ctx -> showUpdateMaterialPage(ctx));
        app.get("/materials/create", ctx -> showCreateMaterialPage(ctx));

        app.post("/materials/create/material", ctx -> handleCreateMaterial(ctx));
        app.post("/materials/{id}/update", ctx -> handleUpdateMaterial(ctx));
        app.post("/materials/{id}/delete-material", ctx -> handleDeleteMaterial(ctx));
    }

    private void handleDeleteMaterial(Context ctx)
    {
        if (!userIsAdmin(ctx)) return;
        int materialVariantId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
          boolean success = materialService.deleteMaterialVariant(materialVariantId);
          if(success)
          {
              ctx.sessionAttribute("successMessage", "Dit materiale blev slettet med id: " + materialVariantId);
              ctx.redirect("/materials");
          }
          else
          {
              ctx.sessionAttribute("errorMessage", "Der skete en fejl, ved slettelse af materialet med id: " + materialVariantId);
              ctx.redirect("/materials");
          }
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/materials");
        }
    }

    private void handleUpdateMaterial(Context ctx)
    {
        if (!userIsAdmin(ctx)) return;

        int materialVariantId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            MaterialVariant oldMaterialVariant = materialService.getMaterialVariantById(materialVariantId);

            MaterialVariant updatedMaterialVariant = buildVariantFromForm(ctx);
            Material updatedMaterial = updatedMaterialVariant.getMaterial();
            updatedMaterial.setMaterialId(oldMaterialVariant.getMaterialId());

            updatedMaterialVariant.setMaterialVariantId(oldMaterialVariant.getMaterialVariantId());
            updatedMaterialVariant.setMaterialId(oldMaterialVariant.getMaterialId());
            updatedMaterialVariant.setMaterial(updatedMaterial);

            boolean isUpdated = materialService.updateMaterialVariant(updatedMaterialVariant);

            if(isUpdated)
            {
                ctx.sessionAttribute("successMessage", "Du har opdateret material varianten med id: " + updatedMaterialVariant.getMaterialVariantId());
            }
            else
            {
                ctx.sessionAttribute("errorMessage", "Noget gik galt ved opdatering af materiale varianten med id: " + updatedMaterialVariant.getMaterialVariantId());
            }

            ctx.redirect("/materials");
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/materials/" + materialVariantId + "/update");
        }
        catch (IllegalArgumentException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/materials/" + materialVariantId + "/update");
        }
    }

    private void handleCreateMaterial(Context ctx)
    {
        if (!userIsAdmin(ctx)) return;

        try
        {
            MaterialVariant materialVariant = buildVariantFromForm(ctx);
            MaterialVariant newMaterialVariant = materialService.createMaterialVariant(materialVariant);

            if(newMaterialVariant != null)
            {
                ctx.sessionAttribute("successMessage", "Du har oprettet et nyt materiale variant med id: " + newMaterialVariant.getMaterialVariantId());
            }
            else
            {
                ctx.sessionAttribute("errorMessage", "Noget gik galt ved oprettelse af nyt materiale");
            }

            ctx.redirect("/materials");
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/materials/create");
        }
        catch (IllegalArgumentException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/materials/create");
        }
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

        boolean hasSearched = false;

        String searchType = ctx.queryParam("searchType");
        String query = ctx.queryParam("query");

        List<MaterialVariant> variants = List.of();

        if(hasSearch(searchType) && hasSearch(query))
        {
            hasSearched = true;
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
        ctx.attribute("hasSearched", hasSearched);
        ctx.render("materials");
    }

    private MaterialVariant buildVariantFromForm(Context ctx)
    {
        String name = ctx.formParam("name");
        String usage = ctx.formParam("usage");
        String unit = ctx.formParam("unit");
        String categoryStr = ctx.formParam("category");
        String typeStr = ctx.formParam("type");

        ValidationUtil.validateMaterialValue(name, "Navn");
        ValidationUtil.validateMaterialValue(usage, "Hjælpe tekst");
        ValidationUtil.validateMaterialValue(unit, "Enhed");

        Integer width = parseOptionalIntWithValidation(ctx.formParam("width"), "Bredde");
        Integer height = parseOptionalIntWithValidation(ctx.formParam("height"), "Højde");
        Integer length = parseOptionalIntWithValidation(ctx.formParam("length"), "Længde");
        Integer piecesPerUnit = parseOptionalIntWithValidation(ctx.formParam("piecesPerUnit"), "Antal per enhed");
        double unitPrice = parseDoubleOrThrow(ctx.formParam("unitPrice"), "Pris");

        MaterialCategory category = MaterialCategory.valueOf(categoryStr);
        MaterialType type = MaterialType.valueOf(typeStr);

        Material material = new Material(
                0,
                name,
                category,
                type,
                width,
                height,
                unit,
                usage
        );

        return new MaterialVariant(
                0,
                0,
                length,
                unitPrice,
                piecesPerUnit,
                material
        );
    }

    private Integer parseOptionalIntWithValidation(String value, String fieldName)
    {
        if (value == null || value.isBlank())
        {
            return null;
        }

        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(fieldName + " skal være et heltal.");
        }
    }

    private double parseDoubleOrThrow(String value, String fieldName) {

        try
        {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(fieldName + " skal være et tal.");
        }
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
