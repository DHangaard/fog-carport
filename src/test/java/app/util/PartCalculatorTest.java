package app.util;

import app.dto.RafterCalculationDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PartCalculatorTest
{

    @Test
    void calculateRafters()
    {
        RafterCalculationDTO rafterCalculationDTO = PartCalculator.calculateRafters(780, 4.5, 60);
        assertEquals(15,rafterCalculationDTO.numberOfRafters());
        assertEquals(55.071, rafterCalculationDTO.spacing(),0.01);
    }
}