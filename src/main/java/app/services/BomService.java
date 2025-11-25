package app.services;

import app.entities.*;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import app.persistence.MaterialVariantMapper;
import app.util.PartCalculator;

import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BomService
{
    private MaterialVariantMapper variantMapper;
    private final int NUMBER_OF_BEAMS = 2;
    private final int STANDARD_POST_SIZE = 300;

    public BomService(MaterialVariantMapper variantMapper)
    {
        this.variantMapper = variantMapper;
    }

    public List<MaterialLine> getBillOfMaterialByCarport(Carport carport) throws DatabaseException
    {
        List<MaterialLine> billOfMaterial = new ArrayList<>();

        MaterialLine postsMaterialLine = calculateNumberOfPosts(carport);
        MaterialLine beamsMaterialLine = calculateNumberOfBeams(carport);
        MaterialLine raftersMaterialLine = calculateNumberOfRafters(carport);

        billOfMaterial.add(postsMaterialLine);
        billOfMaterial.add(beamsMaterialLine);
        billOfMaterial.add(raftersMaterialLine);

        return billOfMaterial;
    }

    private MaterialLine calculateNumberOfPosts(Carport carport) throws DatabaseException
    {
        int numberOfPosts = 0;

        if(carport.getShed() != null)
        {
           numberOfPosts = PartCalculator.calculateNumberOfPostsWithShed(carport.getLength(), carport.getShed().getShedPlacement());
        }
        else
        {
            numberOfPosts = PartCalculator.calculateNumberOfPostsWithOutShed(carport.getLength());
        }

        List<MaterialVariant> posts = variantMapper.getAllVariantsByType(MaterialType.POST);

        MaterialVariant postVariant = posts.stream()
                .filter(materialVariant -> materialVariant.getVariantLength() == STANDARD_POST_SIZE)
                .findFirst()
                .orElseThrow(() -> new DatabaseException("Kunne ikke finde stolpe"));

        return new MaterialLine(postVariant, numberOfPosts);
    }

    private MaterialLine calculateNumberOfRafters(Carport carport) throws DatabaseException
    {
        int numberOfRafters = PartCalculator.calculateNumberOfRafters(carport.getLength());

        List<MaterialVariant> rafterVariants = variantMapper.getAllVariantsByType(MaterialType.RAFTER);

        MaterialVariant rafterVariant = rafterVariants.stream()
                .filter(variant -> variant.getVariantLength() != null)
                .filter(variant -> variant.getVariantLength() >= carport.getWidth())
                .min(Comparator.comparingInt(MaterialVariant::getVariantLength))
                .orElseThrow(() -> new DatabaseException("Ingen spær er lang nok til bredde: " + carport.getWidth() + " cm."));

        return new MaterialLine(rafterVariant, numberOfRafters);
    }

    private MaterialLine calculateRoofTiles(int carportWidth, int carportLength) throws DatabaseException
    {
        List<MaterialVariant> roofs = variantMapper.getAllVariantsByType(MaterialType.ROOF);
        int numberofRoofTiles = PartCalculator.calculateRoofTiles(carportWidth, carportLength);

        // Beregn hvilken længde tagplade der skal anvendes

        // Find tagplade variant
        MaterialVariant roofVariant = roofs.stream()
                .filter(materialVariant -> materialVariant.getVariantLength() == 00) // TODO insert variable
                .findFirst()
                .orElseThrow(() -> new DateTimeException("Kunne ikke finde tagmateriale"));

        return new MaterialLine(roofVariant, numberofRoofTiles);
    }


    private MaterialLine calculateNumberOfBeams(Carport carport) throws DatabaseException
    {
        List<MaterialVariant> beams = variantMapper.getAllVariantsByType(MaterialType.BEAM);
        // List<Integer> lengths = new ArrayList<>();

        /*
        final MINIMUMLENGTH = 420
        final WASTE_TOLERENCE = 60
        Er længden på remmen > variant Længden

        1. Hent alle remme via mapper
        2. Populer listen lengths med rem-længder
        Valgt: 690 + 30 cm (Acceptable spild) = vælg : 720!
        Valgt: 660: fundet 660!

        Logik:
        Findes længden?
        - løb listen igennem
        - Hvis ja:
            - return materiale
        Findes længden ikke?


        Er længden på remmen = variant Længden - Så brug

        Er længden på remmen > variant Længden
        Kunde valgt 750
        Kunde valgt 780 findes ikke i længde brug 2 stykker for at opnå fuld længden
            - Hardcoded
        Vælg 2 stykker på 600
        780-600 = 180 mangler
        180 * 2 = 360
        Find et stykke med acceptable spild
        Vi ved at 2. stople er ved 420 cm

        Programmet længde/7 første stolpe
        780: |110cm ------ |420 -------- |30 (750)cm
        750: |110cm ------ |420 -------- |30 (720)cm
        Er længden på remmen < variant Længden

        Carport på 420:
                 110cm                        390cm
        ----------*----------------------------*---
        |                                         |
        |                                         |
        |                                         |
        |                                         |
        |                                         |
        |                                         |
        ----------*----------------------------*---
        Længde

        Første skal være 310+110 - (stolpebredde / 2) = 415 = længde Vælg 2 stykker til liste!
        Andet stykke længde (780) - 415 = 365
        Find et stykke 365 + spildTolerence og læg et i listen!


        Andet stykke længde (750) - 415 = 335


         */

        //beams.stream()
              //  .filter(materialVariant -> materialVariant.getVariantLength() ==)

        return null;
    }

}
