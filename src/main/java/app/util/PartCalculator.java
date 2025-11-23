package app.util;

import app.dto.RafterCalculationDTO;

public class PartCalculator
{
    public static int calculateNumberOfPosts(int length, boolean hasShed)
    {
        final int CORNER_POSTS = 4;
        final int POSTS_PER_ADDITION = 2;
        final double MAX_POST_SPACING_CM = 310.0;
        final int SHED_EXTRA_POSTS = 3;

        int numberOfPosts = CORNER_POSTS;

        for (double position = MAX_POST_SPACING_CM; position < length; position += MAX_POST_SPACING_CM)
        {
            numberOfPosts += POSTS_PER_ADDITION;
        }

        if (hasShed)
        {
            numberOfPosts += SHED_EXTRA_POSTS;
        }

        return numberOfPosts;
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
}
