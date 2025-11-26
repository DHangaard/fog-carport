package app.services;

import app.entities.*;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import app.persistence.MaterialVariantMapper;
import app.util.PartCalculator;

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

        MaterialLine postMaterialLine = calculateNumberOfPosts(carport);
        MaterialLine rafterMaterialLine = calculateNumberOfRafters(carport);
        List<MaterialLine> beamMaterialLines = calculateNumberOfBeams(carport);
        List<MaterialLine> roofMaterialLines = calculateRoofTiles(carport);

        billOfMaterial.add(rafterMaterialLine);
        billOfMaterial.add(postMaterialLine);

        beamMaterialLines.stream()
                .filter(materialLine -> materialLine != null)
                .forEach(materialLine -> billOfMaterial.add(materialLine));

        roofMaterialLines.stream()
                .filter(materialLine -> materialLine != null)
                .forEach(materialLine -> billOfMaterial.add(materialLine));

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

    private List<MaterialLine> calculateRoofTiles(Carport carport) throws DatabaseException
    {
        List<MaterialLine> roofVariantsNeeded = new ArrayList<>();
        List<MaterialVariant> roofVariants = variantMapper.getAllVariantsByType(MaterialType.ROOF);

        int roofVariantWidth = roofVariants.stream()
                .filter(materialVariant -> materialVariant.getVariantLength() != null)
                .filter(m -> m.getMaterial().getMaterialWidth() != null)
                .mapToInt(m -> m.getMaterial().getMaterialWidth())
                .findFirst()
                .orElseThrow(() -> new DatabaseException("Ingen kombination af tagplader passer til længde: " + carport.getLength() + " cm."));

        int numberOfRoofTileRows = PartCalculator.calculateNumberOfRoofTileRows(carport.getWidth(), roofVariantWidth);

        int variantMaxLength = getMaxVariantLength(roofVariants);
        final int OVERHANG = 20;
        final int OVERLAY = 20;

        int carportLengthWithOverhang = carport.getLength() + OVERHANG;

        if (variantMaxLength <= carportLengthWithOverhang)
        {
            int tolerance = 100;
            MaterialVariant roofVariant = findOptimalVariantLength(roofVariants, variantMaxLength);

            if (roofVariant.getVariantLength() == carport.getLength() || roofVariant.getVariantLength() < carport.getLength() + tolerance)
            {
                roofVariant = findOptimalVariantLength(roofVariants, variantMaxLength/2);
            }

            int totalRoofLengthCoverage = carport.getLength() + OVERHANG + OVERLAY;
            int remainingLength = totalRoofLengthCoverage - roofVariant.getVariantLength();

            MaterialVariant remainingVariant = findOptimalVariantLength(roofVariants, remainingLength);

            roofVariantsNeeded.add(new MaterialLine(roofVariant, numberOfRoofTileRows));
            roofVariantsNeeded.add(new MaterialLine(remainingVariant, numberOfRoofTileRows));
        }
        else
        {
            MaterialVariant roofVariant = findOptimalVariantLength(roofVariants, carport.getLength());
            roofVariantsNeeded.add(new MaterialLine(roofVariant, numberOfRoofTileRows));
        }
        return roofVariantsNeeded;
    }

    private List<MaterialLine> calculateNumberOfBeams(Carport carport) throws DatabaseException
    {
        List<MaterialLine> beamsNeeded = new ArrayList<>();
        List<MaterialVariant> beamVariants = variantMapper.getAllVariantsByType(MaterialType.BEAM);
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
                    .min(Comparator.comparingInt(MaterialVariant::getVariantLength))
                    .orElseThrow(() -> new DatabaseException("Ingen kombination af remme passer til længde: " + carport.getLength() + " cm."));

            beamsNeeded.add(new MaterialLine(beamVariant, NUMBER_OF_BEAM_ROWS));

            int remainingPerSide = carport.getLength() - DISTANCE_TO_CENTER_POST;
            int remainingTotal = remainingPerSide * NUMBER_OF_BEAM_ROWS;

            MaterialVariant remainingVariant = beamVariants.stream()
                    .filter(v -> v.getVariantLength() != null)
                    .filter(v -> v.getVariantLength() >= remainingTotal)
                    .min(Comparator.comparingInt(MaterialVariant::getVariantLength))
                    .orElseThrow(() -> new DatabaseException("Ingen kombination af remme passer til længde: " + carport.getLength() + " cm."));

            beamsNeeded.add(new MaterialLine(remainingVariant, 1));
        }
        else
        {
            MaterialVariant beamVariant = beamVariants.stream()
                    .filter(variant -> variant.getVariantLength() != null)
                    .filter(variant -> variant.getVariantLength() >= carport.getLength())
                    .min(Comparator.comparingInt(MaterialVariant::getVariantLength))
                    .orElseThrow(() -> new DatabaseException("Ingen kombination af remme passer til længde: " + carport.getLength() + " cm."));

            beamsNeeded.add(new MaterialLine(beamVariant, NUMBER_OF_BEAM_ROWS));
        }

        return beamsNeeded;
    }

    private List<MaterialLine> getFittingsForCarport(int number)
    {
    return null;
    }

    private MaterialVariant getFittingsForPost(String fittingDirection) throws DatabaseException
    {
        List<MaterialVariant> fittingVariants = variantMapper.getAllVariantsByType(MaterialType.FITTING);

        return fittingVariants.stream()
                .filter(materialVariant -> materialVariant != null)
                .filter(materialVariant -> materialVariant.getMaterial().getName().equals(fittingDirection))
                .findFirst()
                .orElseThrow(() -> new DatabaseException("Kunne ikke finde beslag"));
    }

    private MaterialVariant findOptimalVariantLength(List<MaterialVariant> variants, int carportLength) throws DatabaseException
    {
        return variants.stream()
                .filter(variant -> variant.getVariantLength() != null)
                .filter(variant -> variant.getVariantLength() >= carportLength)
                .min(Comparator.comparingInt(MaterialVariant::getVariantLength))
                .orElseThrow(() -> new DatabaseException("Ingen kombination af materialer passer til længde: " + carportLength + " cm."));
    }

    private int getMaxVariantLength(List<MaterialVariant> variants) throws DatabaseException
    {
        return variants.stream()
                .filter(materialVariant -> materialVariant.getVariantLength() != null)
                .mapToInt(MaterialVariant::getVariantLength)
                .max()
                .orElseThrow(() -> new DatabaseException("Ingen materialer fundet"));
    }

}
