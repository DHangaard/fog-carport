package app.util;

import app.dto.RafterCalculationDTO;

public class PartCalculator
{
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
        
        return new RafterCalculationDTO(totalNumberOfRafters, spacing);
    }
}
