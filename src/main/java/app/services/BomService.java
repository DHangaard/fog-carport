package app.services;

import app.entities.*;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import app.exceptions.MaterialNotFoundException;
import app.persistence.MaterialVariantMapper;
import app.util.PartCalculator;
import app.util.PostPlacementCalculatorUtil;

import java.util.*;

public class BomService implements IBomService
{
    private MaterialVariantMapper variantMapper;
    private final int STANDARD_POST_SIZE = 300;
    private final double COVERAGE_PERCENTAGE = 40.0;

    public BomService(MaterialVariantMapper variantMapper)
    {
        this.variantMapper = variantMapper;
    }

    @Override
    public List<MaterialLine> getBillOfMaterialByCarport(Carport carport) throws DatabaseException, MaterialNotFoundException
    {
        List<MaterialLine> billOfMaterial = new ArrayList<>();

        MaterialLine postMaterialLine = calculateNumberOfPosts(carport);
        MaterialLine rafterMaterialLine = calculateNumberOfRafters(carport);
        MaterialLine roofPlateScrewLine = calculateRoofPlateScrews(carport);
        MaterialLine stripRoolLine = calculateNumberOfStripRolls(carport);
        MaterialLine bracketScrewLine = calculateBracketScrews(carport);

        List<MaterialLine> beamMaterialLines = calculateNumberOfBeams(carport);
        List<MaterialLine> roofMaterialLines = calculateRoofTiles(carport);
        List<MaterialLine> fittingMaterialLines = getFittingsForCarport(PartCalculator.calculateNumberOfRafters(carport.getLength()));
        List<MaterialLine> boltsAndWashers = calculateNumberOfCarriageBoltsAndWashers(carport);

        billOfMaterial.add(rafterMaterialLine);
        billOfMaterial.add(postMaterialLine);
        billOfMaterial.add(roofPlateScrewLine);
        billOfMaterial.add(stripRoolLine);
        billOfMaterial.add(bracketScrewLine);

        beamMaterialLines.stream()
                .filter(materialLine -> materialLine != null)
                .forEach(materialLine -> billOfMaterial.add(materialLine));

        roofMaterialLines.stream()
                .filter(materialLine -> materialLine != null)
                .forEach(materialLine -> billOfMaterial.add(materialLine));

        fittingMaterialLines.stream()
                .filter(materialLine -> materialLine != null)
                .forEach(materialLine -> billOfMaterial.add(materialLine));

        boltsAndWashers.stream()
                .filter(materialLine -> materialLine != null)
                .forEach(materialLine -> billOfMaterial.add(materialLine));

        return billOfMaterial;
    }

    @Override
    public PricingDetails calculateCarportPrice(List<MaterialLine> billOfMaterial)
    {
        PricingDetails pricingDetails = null;

        if(billOfMaterial != null)
        {
            double costPrice = calculateCostPrice(billOfMaterial);
            pricingDetails = new PricingDetails(costPrice, COVERAGE_PERCENTAGE);
        }
        return pricingDetails;
    }

    private double calculateCostPrice(List<MaterialLine> materialLines)
    {
        return materialLines.stream()
                .mapToDouble(mLines -> mLines.getQuantity() * mLines.getMaterialVariant().getUnitPrice())
                .sum();
    }

    private MaterialLine calculateNumberOfPosts(Carport carport) throws DatabaseException, MaterialNotFoundException
    {
        int numberOfPosts = 0;

        if (carport.getShed() != null)
        {
            numberOfPosts = PartCalculator.calculateNumberOfPostsWithShed(carport.getLength(), carport.getShed());
        }
        else
        {
            numberOfPosts = PartCalculator.calculateNumberOfPostsWithOutShed(carport.getLength());
        }

        List<MaterialVariant> posts = variantMapper.getAllVariantsByType(MaterialType.POST);

        MaterialVariant postVariant = posts.stream()
                .filter(materialVariant -> materialVariant.getVariantLength() == STANDARD_POST_SIZE)
                .findFirst()
                .orElseThrow(() -> new MaterialNotFoundException("Kunne ikke finde stolpe"));

        return new MaterialLine(postVariant, numberOfPosts);
    }

    private MaterialLine calculateNumberOfRafters(Carport carport) throws DatabaseException, MaterialNotFoundException
    {
        int numberOfRafters = PartCalculator.calculateNumberOfRafters(carport.getLength());

        List<MaterialVariant> rafterVariants = variantMapper.getAllVariantsByType(MaterialType.RAFTER);

        MaterialVariant rafterVariant = rafterVariants.stream()
                .filter(variant -> variant.getVariantLength() != null)
                .filter(variant -> variant.getVariantLength() >= carport.getWidth())
                .min(Comparator.comparingInt(MaterialVariant::getVariantLength))
                .orElseThrow(() -> new MaterialNotFoundException("Ingen spær er lang nok til bredde: " + carport.getWidth() + " cm."));

        return new MaterialLine(rafterVariant, numberOfRafters);
    }

    private List<MaterialLine> calculateRoofTiles(Carport carport) throws DatabaseException, MaterialNotFoundException
    {
        List<MaterialLine> roofVariantsNeeded = new ArrayList<>();
        List<MaterialVariant> roofVariants = variantMapper.getAllVariantsByType(MaterialType.ROOF);

        int roofVariantWidth = roofVariants.stream()
                .filter(materialVariant -> materialVariant.getVariantLength() != null)
                .filter(m -> m.getMaterial().getMaterialWidth() != null)
                .mapToInt(m -> m.getMaterial().getMaterialWidth())
                .findFirst()
                .orElseThrow(() -> new MaterialNotFoundException("Ingen kombination af tagplader passer til længde: " + carport.getLength() + " cm."));

        int numberOfRoofTileRows = PartCalculator.calculateNumberOfRoofTileRows(carport.getWidth(), roofVariantWidth);

        int variantMaxLength = getMaxVariantLength(roofVariants);
        final int OVERHANG = 20;
        final int OVERLAY = 20;
        final int TOLERANCE = 100;

        int carportLengthWithOverhang = carport.getLength() + OVERHANG;

        if (variantMaxLength <= carportLengthWithOverhang)
        {

            MaterialVariant firstPlate = findOptimalVariantLength(roofVariants, variantMaxLength);

            if (firstPlate.getVariantLength() == carport.getLength() || firstPlate.getVariantLength() < carport.getLength() + TOLERANCE)
            {
                firstPlate = findOptimalVariantLength(roofVariants, variantMaxLength/2);
            }

            int remainingLength = carportLengthWithOverhang - (firstPlate.getVariantLength() - OVERLAY);

            MaterialVariant secondPlate = findOptimalVariantLength(roofVariants, remainingLength);

            roofVariantsNeeded.add(new MaterialLine(firstPlate, numberOfRoofTileRows));
            roofVariantsNeeded.add(new MaterialLine(secondPlate, numberOfRoofTileRows));
        }
        else
        {
            MaterialVariant roofVariant = findOptimalVariantLength(roofVariants, carportLengthWithOverhang);
            roofVariantsNeeded.add(new MaterialLine(roofVariant, numberOfRoofTileRows));
        }
        return roofVariantsNeeded;
    }

    // TODO test beam placement is correct - alternatively move beam joint to shedPostPlacement
    private List<MaterialLine> calculateNumberOfBeams(Carport carport) throws DatabaseException, MaterialNotFoundException
    {
        List<MaterialLine> beamsNeeded = new ArrayList<>();
        List<MaterialVariant> beamVariants = variantMapper.getAllVariantsByType(MaterialType.BEAM);
        final int NUMBER_OF_BEAM_ROWS = 2;
        final int MAX_VARIANT_lENGTH = getMaxVariantLength(beamVariants);
        int distanceToCenterPost = PostPlacementCalculatorUtil.calculateCenterPostPlacement(carport);
        int centerPostDefaultPlacement = 410;
        int postEdgeInsetCm = 30;
        int shedPostPlacement = 0;

        if(carport.getShed() != null)
        {
            shedPostPlacement = carport.getShed().getLength() + postEdgeInsetCm;
        }

        if (distanceToCenterPost < centerPostDefaultPlacement)
        {
            distanceToCenterPost = shedPostPlacement;
        }

        if (carport.getLength() > MAX_VARIANT_lENGTH) // When longer than max variant, the carport will have 6 posts
        {
            MaterialVariant beamVariant = findOptimalVariantLength(beamVariants, distanceToCenterPost);
            beamsNeeded.add(new MaterialLine(beamVariant, NUMBER_OF_BEAM_ROWS));

            int remainingPerSide = carport.getLength() - distanceToCenterPost;
            int remainingTotal = remainingPerSide * NUMBER_OF_BEAM_ROWS;
            int remainingBeams;
            MaterialVariant remainingVariant;

            if (remainingTotal > MAX_VARIANT_lENGTH)
            {
                remainingBeams = (int) Math.ceil(NUMBER_OF_BEAM_ROWS);
                remainingVariant = findOptimalVariantLength(beamVariants, remainingPerSide);
            }
            else
            {
                remainingBeams = (int) Math.ceil(NUMBER_OF_BEAM_ROWS / 2);
                remainingVariant = findOptimalVariantLength(beamVariants, remainingTotal);
            }

            beamsNeeded.add(new MaterialLine(remainingVariant, remainingBeams));

        }
        else
        {
            MaterialVariant beamVariant = findOptimalVariantLength(beamVariants, carport.getLength());
            beamsNeeded.add(new MaterialLine(beamVariant, NUMBER_OF_BEAM_ROWS));
        }

        return beamsNeeded;
    }

    private MaterialLine calculateRoofPlateScrews(Carport carport) throws DatabaseException, MaterialNotFoundException
    {
        final String PLASTMO_BOTTOM_SCREW_NAME = "Plastmo Bundskruer";

        List<MaterialVariant> fastenerVariants = variantMapper.getAllVariantsByType(MaterialType.FASTENER);

        MaterialVariant roofFastenerVariant = fastenerVariants.stream()
                .filter(materialVariant -> materialVariant != null)
                .filter(materialVariant -> materialVariant.getMaterial().getName().equals(PLASTMO_BOTTOM_SCREW_NAME))
                .findFirst()
                .orElseThrow(() -> new MaterialNotFoundException("Ingen bund skruer fundet"));

        int numberOfScrewsInPackage = roofFastenerVariant.getPiecesPerUnit();
        int numberOfPackagesNeeded = PartCalculator.calculateNumberOfRoofScrewPackagesNeeded(carport.getWidth(), carport.getLength(), numberOfScrewsInPackage);

        return new MaterialLine(roofFastenerVariant, numberOfPackagesNeeded);
    }

    private List<MaterialLine> getFittingsForCarport(int numberOfFittings) throws DatabaseException, MaterialNotFoundException
    {
        final String FITTING_RIGHT = "Universal højre";
        final String FITTING_LEFT= "Universal venstre";
        List<MaterialLine> fittings = new ArrayList<>();

        MaterialVariant rightFitting = getFittingsForRafters(FITTING_RIGHT);
        MaterialVariant leftFitting = getFittingsForRafters(FITTING_LEFT);

        fittings.add(new MaterialLine(rightFitting, numberOfFittings));
        fittings.add(new MaterialLine(leftFitting, numberOfFittings));

        return fittings;
    }

    private MaterialVariant getFittingsForRafters(String fittingDirection) throws DatabaseException, MaterialNotFoundException
    {
        List<MaterialVariant> fittingVariants = variantMapper.getAllVariantsByType(MaterialType.FITTING);

        return fittingVariants.stream()
                .filter(materialVariant -> materialVariant != null)
                .filter(materialVariant -> materialVariant.getMaterial().getName().equals(fittingDirection))
                .findFirst()
                .orElseThrow(() -> new MaterialNotFoundException("Kunne ikke finde beslag"));
    }

    private MaterialLine calculateNumberOfStripRolls(Carport carport) throws DatabaseException, MaterialNotFoundException
    {
        List<MaterialVariant> stripRoolVariants = variantMapper.getAllVariantsByType(MaterialType.METAL_STRAP);

        MaterialVariant stripRoolVariant = stripRoolVariants.stream()
                .filter(materialVariant -> materialVariant != null)
                .findFirst()
                .orElseThrow(() -> new MaterialNotFoundException("Kunne ikke finde hulbånd"));

        int numberOfStripRoolsNeeded = PartCalculator.calculateNumberOfperforatedStripRools(carport, stripRoolVariant.getVariantLength());

        return new MaterialLine(stripRoolVariant, numberOfStripRoolsNeeded);
    }

    private List<MaterialLine> calculateNumberOfCarriageBoltsAndWashers(Carport carport) throws DatabaseException, MaterialNotFoundException
    {
        final String CARRIAGE_BOLT_NAME = "bræddebolt";
        final int CARRIAGE_BOLT_LENGTH_CM = 12;

        final String WASHER_NAME = "firkantskiver";

        List<MaterialLine> boltsAndWashers = new ArrayList<>();

        List<MaterialVariant> beamVariants = variantMapper.getAllVariantsByType(MaterialType.BEAM);
        int beamMaxVariantLength = getMaxVariantLength(beamVariants);
        int numberOfBolts = PartCalculator.calculateNumberOfCarriageBoltsAndWashers(carport, beamMaxVariantLength);
        int numberOfWashers = numberOfBolts;

        List<MaterialVariant> fastenerVariants = variantMapper.getAllVariantsByType(MaterialType.FASTENER);
        MaterialVariant bolt = fastenerVariants.stream()
                .filter(materialVariant -> materialVariant != null)
                .filter(materialVariant -> materialVariant.getMaterial().getName().equals(CARRIAGE_BOLT_NAME))
                .filter(materialVariant -> materialVariant.getVariantLength() != null)
                .filter(materialVariant -> materialVariant.getVariantLength() == CARRIAGE_BOLT_LENGTH_CM)
                .min(Comparator.comparing(MaterialVariant::getUnitPrice))
                .orElseThrow(() -> new MaterialNotFoundException("Kunne ikke finde bræddebolt"));

        List<MaterialVariant> washerVariants = variantMapper.getAllVariantsByType(MaterialType.WASHER);
        MaterialVariant washer = washerVariants.stream()
                .filter(materialVariant -> materialVariant != null)
                .filter(materialVariant -> materialVariant.getMaterial().getName().equals(WASHER_NAME))
                .min(Comparator.comparing(MaterialVariant::getUnitPrice))
                .orElseThrow(() -> new MaterialNotFoundException("Kunne ikke finde firkantskiver"));

        boltsAndWashers.add(new MaterialLine(bolt, numberOfBolts));
        boltsAndWashers.add(new MaterialLine(washer, numberOfWashers));

        return boltsAndWashers;
    }

    private MaterialLine calculateBracketScrews(Carport carport) throws DatabaseException, MaterialNotFoundException
    {
        final String BRACKET_SCREW_NAME = "Beslagskruer";
        final int BRACKET_SCREW_LENGTH_CM = 5;

        List<MaterialVariant> fastenerVariants = variantMapper.getAllVariantsByType(MaterialType.FASTENER);

        MaterialVariant bracketScrewVariant = fastenerVariants.stream()
                .filter(materialVariant -> materialVariant != null)
                .filter(materialVariant -> materialVariant.getMaterial().getName().equals(BRACKET_SCREW_NAME))
                .filter(materialVariant -> materialVariant.getVariantLength() != null)
                .filter(materialVariant -> materialVariant.getVariantLength() == BRACKET_SCREW_LENGTH_CM)
                .min(Comparator.comparing(MaterialVariant::getUnitPrice))
                .orElseThrow(() -> new MaterialNotFoundException("Kunne ikke finde beslagskruer"));

        int bracketScrewPackages = PartCalculator.calculateNumberOfBracketScrewsNeeded(carport, bracketScrewVariant.getPiecesPerUnit());

        return  new MaterialLine(bracketScrewVariant, bracketScrewPackages);
    }

    private MaterialVariant findOptimalVariantLength(List<MaterialVariant> variants, int length) throws MaterialNotFoundException
    {
        return variants.stream()
                .filter(variant -> variant.getVariantLength() != null)
                .filter(variant -> variant.getVariantLength() >= length)
                .min(Comparator.comparingInt(MaterialVariant::getVariantLength))
                .orElseThrow(() -> new MaterialNotFoundException("Ingen kombination af materialer passer til længde: " + length + " cm."));
    }

    private int getMaxVariantLength(List<MaterialVariant> variants) throws MaterialNotFoundException
    {
        return variants.stream()
                .filter(materialVariant -> materialVariant.getVariantLength() != null)
                .mapToInt(MaterialVariant::getVariantLength)
                .max()
                .orElseThrow(() -> new MaterialNotFoundException("Ingen materialer fundet"));
    }

}
