package app.services;

import app.entities.*;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import app.persistence.MaterialVariantMapper;
import app.util.PartCalculator;

import java.time.DateTimeException;
import java.util.*;

public class BomService
{
    private MaterialVariantMapper variantMapper;
    private final int STANDARD_POST_SIZE = 300;

    public BomService(MaterialVariantMapper variantMapper)
    {
        this.variantMapper = variantMapper;
    }

    public List<MaterialLine> getBillOfMaterialByCarport(Carport carport) throws DatabaseException
    {
        List<MaterialLine> billOfMaterial = new ArrayList<>();

        MaterialLine postsMaterialLine = calculateNumberOfPosts(carport);
        List<MaterialLine> beamsMaterialLine = calculateNumberOfBeams(carport);
        MaterialLine raftersMaterialLine = calculateNumberOfRafters(carport);

        billOfMaterial.add(postsMaterialLine);
        beamsMaterialLine.stream()
                .filter(materialLine -> materialLine != null)
                .forEach(materialLine -> billOfMaterial.add(materialLine));
        billOfMaterial.add(raftersMaterialLine);

        return billOfMaterial;
    }

    private MaterialLine calculateNumberOfPosts(Carport carport) throws DatabaseException
    {
        int numberOfPosts = 0;

        if (carport.getShed() != null)
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
        List<MaterialVariant> roofVariants = variantMapper.getAllVariantsByType(MaterialType.ROOF);
        int numberofRoofTiles = PartCalculator.calculateRoofTiles(carportWidth, carportLength);

        // Beregn hvilken længde tagplade der skal anvendes

        // Find tagplade variant
        MaterialVariant roofVariant = roofVariants.stream()
                .filter(materialVariant -> materialVariant.getVariantLength() == 00) // TODO insert variable
                .findFirst()
                .orElseThrow(() -> new DateTimeException("Kunne ikke finde tagmateriale"));

        return new MaterialLine(roofVariant, numberofRoofTiles);
    }

    private List<MaterialLine> calculateNumberOfBeams(Carport carport) throws DatabaseException
    {
        List<MaterialLine> beamsNeeded = new ArrayList<>();
        List<MaterialVariant> beamVariants = variantMapper.getAllVariantsByType(MaterialType.BEAM);

        final int WASTE_TOLERANCE_CM = 60;
        final int NUMBER_OF_BEAM_ROWS = 2;
        final int MAX_VARIANT_lENGTH = beamVariants.stream()
                .filter(materialVariant -> materialVariant.getVariantLength() != null)
                .mapToInt(MaterialVariant::getVariantLength)
                .max()
                .orElseThrow(() -> new DatabaseException("Ingen kombination af remme passer til længde: " + carport.getLength() + " cm."));

        if (carport.getLength() > MAX_VARIANT_lENGTH) // When longer than max variant, the carport will have 6 posts
        {
            final int DISTANCE_TO_CENTER_POST = 420; // Should this be 410 ?

            MaterialVariant beamVariant = beamVariants.stream()
                    .filter(v -> v.getVariantLength() != null)
                    .filter(v -> v.getVariantLength() >= DISTANCE_TO_CENTER_POST)
                    .filter(v -> v.getVariantLength() - DISTANCE_TO_CENTER_POST <= WASTE_TOLERANCE_CM)
                    .min(Comparator.comparingInt(MaterialVariant::getVariantLength))
                    .orElseThrow(() -> new DatabaseException("Ingen kombination af remme passer til længde: " + carport.getLength() + " cm."));

            beamsNeeded.add(new MaterialLine(beamVariant, NUMBER_OF_BEAM_ROWS));

            int remainingPerSide = carport.getLength() - DISTANCE_TO_CENTER_POST;
            int remainingTotal = remainingPerSide * NUMBER_OF_BEAM_ROWS;

            MaterialVariant remainingVariant = beamVariants.stream()
                    .filter(v -> v.getVariantLength() != null)
                    .filter(v -> v.getVariantLength() >= remainingTotal)
                    .filter(v -> v.getVariantLength() - remainingTotal <= WASTE_TOLERANCE_CM)
                    .min(Comparator.comparingInt(MaterialVariant::getVariantLength))
                    .orElseThrow(() -> new DatabaseException("Ingen kombination af remme passer til længde: " + carport.getLength() + " cm."));

            beamsNeeded.add(new MaterialLine(remainingVariant, 1));
        }
        else
        {
            MaterialVariant beamVariant = beamVariants.stream()
                    .filter(variant -> variant.getVariantLength() != null)
                    .filter(variant -> variant.getVariantLength() >= carport.getLength())
                    .filter(variant -> variant.getVariantLength() - carport.getLength() <= WASTE_TOLERANCE_CM)
                    .min(Comparator.comparingInt(MaterialVariant::getVariantLength))
                    .orElseThrow(() -> new DatabaseException("Ingen kombination af remme passer til længde: " + carport.getLength() + " cm."));

            beamsNeeded.add(new MaterialLine(beamVariant, NUMBER_OF_BEAM_ROWS));
        }

        return beamsNeeded;
    }
}
