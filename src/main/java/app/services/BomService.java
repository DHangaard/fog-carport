package app.services;

import app.entities.*;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import app.persistence.MaterialVariantMapper;
import app.util.PartCalculator;

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

    public BillOfMaterial getBillOfMaterial(Carport carport) throws DatabaseException
    {
        BillOfMaterial billOfMaterial = new BillOfMaterial();

        MaterialLine postsMaterialLine = calculateNumberOfPosts(carport.getLength());
        billOfMaterial.addMaterialLine(postsMaterialLine);

        MaterialLine beamsMaterialLine = calculateNumberOfBeams(carport.getLength());
        billOfMaterial.addMaterialLine(beamsMaterialLine);

        return billOfMaterial;
    }

    private MaterialLine calculateNumberOfPosts(int carportLength) throws DatabaseException
    {
        int numberOfPosts = PartCalculator.calculateNumberOfPostsWithOutShed(carportLength);
        List<MaterialVariant> posts = variantMapper.getAllVariantsByType(MaterialType.POST);

        MaterialVariant postVariant = posts.stream()
                        .filter(materialVariant -> materialVariant.getVariantLength() == STANDARD_POST_SIZE)
                        .findFirst()
                        .orElseThrow(() -> new DatabaseException("Kunne ikke finde stolpe"));

        return new MaterialLine(postVariant, numberOfPosts);
    }


    private MaterialLine calculateNumberOfBeams(int carportLength) throws DatabaseException
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

        beams.stream()
                .filter(materialVariant -> materialVariant.getVariantLength() == )

        return null;
    }

}
