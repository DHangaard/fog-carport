package app.util;

import app.dto.BeamCalculationDTO;
import app.dto.RafterCalculationDTO;
import app.entities.Material;
import app.entities.MaterialVariant;
import app.enums.ShedPlacement;

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

    public static MaterialVariant calculateBeam(int lenght, List<Material> beamMaterials)
    {
        return null;
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

    private static int getShedPosts(ShedPlacement shedPlacement)
    {
        return shedPlacement == ShedPlacement.FULL_WIDTH ? SHED_FULL_SIZE_POSTS : SHED_NOT_FULL_SIZE_POSTS;
    }

    public static int calculateRoofTiles(int carportWidth, int carportLength)
    {
        return 0;
    }
}
