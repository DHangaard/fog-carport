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

    /*
    Variabler:
    Længde
    Max. spænvidde = Antages / fastslået
    Spær bredde = Afhænger af materiale, formentlig ens

    Antal spær = (længde / max spænvidde rundet op) + 2 til gavle

    Mellemrum = antal spær - 1

    Mellemrum størrelse = ((længde - 2 * spær bredde) / antal mellemrum)


    Længde = 750
    Spænvidde = 60
    antal spær = (750/60 rundet op) + 2 = 15
    Mellemrum = 15-1 = 14
    M.Størrelse = (750/14) = 53,5 cm
     */

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
        return shedPlacement == ShedPlacement.FULL_WIDTH ? SHED_FULL_SIZE_POSTS : SHED_NOT_FULL_SIZE_POSTS;
    }
}
