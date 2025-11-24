package app.util;

import app.dto.BeamCalculationDTO;
import app.dto.RafterCalculationDTO;
import app.entities.Material;
import app.enums.ShedPlacement;

import java.util.List;

public class PartCalculator
{
    private static final int SHED_FULL_SIZE_POSTS = 5;
    private static final int SHED_NOT_FULL_SIZE_POSTS = 4;

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

    public static BeamCalculationDTO calculateBeam(int lenght, List<Material> beamMaterials)
    {
        return new BeamCalculationDTO(3, List.of());
    }

    {

    }

    public static RafterCalculationDTO calculateRafters(int length, double rafterWidth, int maxSpacing)
    {
        int numberOfMiddleRafters = length / maxSpacing;
        int totalNumberOfRafters = numberOfMiddleRafters + 2;
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
        if(shedPlacement.equals(ShedPlacement.FULL_WIDTH))
        {
            return  SHED_FULL_SIZE_POSTS;
        }
        else
        {
            return SHED_NOT_FULL_SIZE_POSTS;
        }
    }
}
