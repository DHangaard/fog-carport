package app.util;

import app.dto.BeamCalculationDTO;
import app.dto.RafterCalculationDTO;
import app.entities.Carport;
import app.entities.Material;
import app.entities.MaterialVariant;
import app.enums.ShedPlacement;
import app.exceptions.DatabaseException;

import java.util.List;

public class PartCalculator
{
    private static final int SHED_FULL_SIZE_POSTS = 5;
    private static final int SHED_NOT_FULL_SIZE_POSTS = 4;
    private static final int MAX_SPACING_CM = 60;
    private static final int RAFTER_START_END = 2;

    public static int calculateNumberOfPostsWithShed(int length, ShedPlacement shedPlacement)
    {
        int totalPosts = calculateNumberOfPostsWithOutShed(length);
        totalPosts += getShedPosts(shedPlacement);

        return totalPosts;
    }

    public static int calculateNumberOfPostsWithOutShed(int length)
    {
        final double ROOF_LENGTH_PER_POST_CM = 310.0;
        final int ROWS = 2;
        final int MINIMUM_POSTS_PER_ROW = 2;

        int postsPerRow = (int) Math.ceil((double) length / ROOF_LENGTH_PER_POST_CM);

        if (postsPerRow < MINIMUM_POSTS_PER_ROW)
        {
            postsPerRow = MINIMUM_POSTS_PER_ROW;
        }

        return postsPerRow * ROWS;
    }

    public static int calculateNumberOfRafters(int length)
    {
        int numberOfMiddleRafters = length / MAX_SPACING_CM;
        return numberOfMiddleRafters + RAFTER_START_END;
    }

    public static RafterCalculationDTO calculateRafters(int length, double rafterWidth)
    {
        int totalNumberOfRafters = calculateNumberOfRafters(length);
        int numberOfMiddleRafters = totalNumberOfRafters - RAFTER_START_END;
        double spacing = (length - (2 * rafterWidth)) / (totalNumberOfRafters - 1);

        if (spacing > 56.0)
        {
            numberOfMiddleRafters++;
            totalNumberOfRafters = numberOfMiddleRafters + 2;
            spacing = (length - (2 * rafterWidth)) / (totalNumberOfRafters - 1);
        }
        double roundedSpacing = Math.round(spacing * 10) / 10.0;

        return new RafterCalculationDTO(totalNumberOfRafters, roundedSpacing);
    }

    private static double getRafterSpacing(int length, double rafterWidth)
    {
        int totalNumberOfRafters = calculateNumberOfRafters(length);
        return (length - (2 * rafterWidth)) / (totalNumberOfRafters - 1);
    }

    private static int getShedPosts(ShedPlacement shedPlacement)
    {
        return shedPlacement == ShedPlacement.FULL_WIDTH ? SHED_FULL_SIZE_POSTS : SHED_NOT_FULL_SIZE_POSTS;
    }

    public static int calculateNumberOfRoofTileRows(int carportWidth, int roofVariantWidth)
    {
        final int OVERLAY = 9;
        int roofVariantWidthWithOverlay = roofVariantWidth - OVERLAY;

        return (int) Math.ceil(((double) carportWidth / roofVariantWidthWithOverlay));
    }

    public static int calculateNumberOfRoofScrewPackagesNeeded(int carportWidth, int carportLength, int numberOfScrewsInPackage)
    {
        int screwsPerSquareMeter = 12;

        double carportWidthInMeter = carportWidth / 100;
        double carportLengthInMeter = carportLength / 100;

        double totalCarportArea = carportWidthInMeter * carportLengthInMeter;
        double totalScrews = totalCarportArea * screwsPerSquareMeter;

        return (int) Math.ceil(totalScrews / numberOfScrewsInPackage);
    }

    public static int calculateNumberOfperforatedStripRools(Carport carport, int stripRoolLength)
    {
        int edgeInsetInCm = 35;
        double rafterWidth = 4.5;
        double stripRoolLengthInMeter = stripRoolLength / 100;

        double rafterSpacing = getRafterSpacing(carport.getLength(), rafterWidth);

        double carportWidthAfterMargin = carport.getWidth() - (edgeInsetInCm * 2);
        double carportLengthAfterMargin = 0;

        if (carport.getShed() != null)
        {
            carportLengthAfterMargin = carport.getLength() - (carport.getShed().getLength() + rafterSpacing);
        }
        else
        {
            carportLengthAfterMargin = carport.getLength() - (rafterSpacing * 2);
        }

        double diagonal = Math.sqrt(Math.pow(carportLengthAfterMargin, 2) + Math.pow(carportWidthAfterMargin, 2));
        double totalStripNeed = 2 * (diagonal / 100);

        return (int) Math.ceil(totalStripNeed / stripRoolLengthInMeter);
    }

    public static int calculateNumberOfCarriageBoltsAndWashers(Carport carport, List<MaterialVariant> beamVariants) throws DatabaseException
    {
        int beamVariantMaxLength = beamVariants.stream()
                .filter(materialVariant -> materialVariant.getVariantLength() != null)
                .mapToInt(MaterialVariant::getVariantLength)
                .max()
                .orElseThrow(() -> new DatabaseException("Ingen materialer fundet"));

        boolean isSingleBeamPerRow = beamVariantMaxLength >= carport.getLength();

        int jointsPerSide = isSingleBeamPerRow ? 3 : 4;
        int jointsTotal = jointsPerSide * 2;

        int numberOfBoltsPerJoin = 2; // Washers are the same number as bolts
        int numberOfWashersPerJoin = numberOfBoltsPerJoin; // Redundant

        return jointsTotal * numberOfBoltsPerJoin;
    }
}
