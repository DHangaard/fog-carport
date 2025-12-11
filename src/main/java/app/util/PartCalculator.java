package app.util;

import app.dto.RafterCalculationDTO;
import app.entities.Carport;
import app.entities.Shed;
import app.enums.ShedPlacement;

public class PartCalculator
{
    private static final int SHED_FULL_SIZE_POSTS = 5;
    private static final int SHED_NOT_FULL_SIZE_POSTS_SMALL = 4;
    private static final int SHED_NOT_FULL_SIZE_POSTS_LARGE = 5;
    private static final int MAX_SPACING_CM = 60;
    private static final int RAFTER_START_END = 2;
    private static final double MAX_DISTANCE_BETWEEN_POSTS = 310.0;
    private static final double POST_EDGE_INSET_CM = 30.00;

    public static int calculateNumberOfPostsWithShed(int length, Shed shed)
    {
        int totalPosts = calculateNumberOfPostsWithOutShed(length);
        totalPosts += getShedPosts(shed);

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
        double spacing = (length - rafterWidth) / (totalNumberOfRafters - 1);

        if (spacing > 56.0)
        {
            numberOfMiddleRafters++;
            totalNumberOfRafters = numberOfMiddleRafters + 2;
            spacing = (length - rafterWidth) / (totalNumberOfRafters - 1);
        }
        double roundedSpacing = Math.round(spacing * 10) / 10.0;

        return new RafterCalculationDTO(totalNumberOfRafters, spacing, roundedSpacing);
    }

    private static double getRafterSpacing(int length, double rafterWidth)
    {
        int totalNumberOfRafters = calculateNumberOfRafters(length);
        return  (length - (2 * rafterWidth)) / (totalNumberOfRafters - 1);
    }

    private static int getShedPosts(Shed shed)
    {
        if (shed.getShedPlacement() == ShedPlacement.LEFT)
        {
            if (shed.getLength() > MAX_DISTANCE_BETWEEN_POSTS)
            {
                return SHED_NOT_FULL_SIZE_POSTS_LARGE;
            }
            else
            {
                return SHED_NOT_FULL_SIZE_POSTS_SMALL;
            }
        }
        else
        {
            return SHED_FULL_SIZE_POSTS;
        }
    }

    public static int calculateNumberOfRoofTileRows(int carportWidth, int roofVariantWidth)
    {
        final int OVERLAY = 9;
        int roofVariantWidthWithOverlay = roofVariantWidth - OVERLAY;

        return (int) Math.ceil(((double)carportWidth / roofVariantWidthWithOverlay));
    }

    public static int calculateNumberOfRoofScrewPackagesNeeded(int carportWidth, int carportLength, int screwsPerPackage)
    {
        int screwsPerSquareMeter = 12;

        double carportWidthInMeter = carportWidth / 100.0;
        double carportLengthInMeter = carportLength / 100.0;

        double totalCarportArea = carportWidthInMeter * carportLengthInMeter;
        double totalScrews = totalCarportArea * screwsPerSquareMeter;

        return (int) Math.ceil( totalScrews / screwsPerPackage);
    }

    public static int calculateNumberOfperforatedStripRools(Carport carport, int stripRoolLength)
    {
        int edgeInsetInCm = 35;
        double rafterWidth = 4.5;
        double stripRoolLengthInMeter = stripRoolLength / 100.0;

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

    public static int calculateNumberOfCarriageBoltsAndWashers(Carport carport, int beamMaxVariantLength)
    {
        boolean hasShed = carport.getShed() != null;
        int postsWithoutBeam = 3;
        int numberOfPosts = hasShed ? calculateNumberOfPostsWithShed(carport.getLength(), carport.getShed()) - postsWithoutBeam
                : calculateNumberOfPostsWithOutShed(carport.getLength());

        boolean isSingleBeamPerRow = beamMaxVariantLength >= carport.getLength();
        int jointsTotal = isSingleBeamPerRow ? numberOfPosts : numberOfPosts + 2;

        int numberOfBoltsPerJoin = 2; // Washers are the same number as bolts

        return jointsTotal * numberOfBoltsPerJoin;
    }

    public static int calculateNumberOfBracketScrewsNeeded(Carport carport, int screwsPerPackage)
    {
        int screwsPerFittingSide = 9;
        int screwsPerStripFixPoint = 2;
        int numberOfRafters = calculateNumberOfRafters(carport.getLength());

        int totalNumberOfFittingScrews = 2 * (screwsPerFittingSide * numberOfRafters);
        int totalStripFixPointScrews = 0;

        if(carport.getShed() == null)
        {
           totalStripFixPointScrews = numberOfRafters * screwsPerStripFixPoint;
        }
        else
        {
            int raftersBeforeShed = calculateNumberOfRafters(carport.getLength() - carport.getShed().getLength());
            int raftersCrossedByStrip = raftersBeforeShed - 1;

            totalStripFixPointScrews = raftersCrossedByStrip * screwsPerStripFixPoint;
        }

        int totalBracketScrews = totalNumberOfFittingScrews + totalStripFixPointScrews;

        return (int) Math.ceil((double) totalBracketScrews / screwsPerPackage);
    }
}
