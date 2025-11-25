package app.util;

import app.dto.RafterCalculationDTO;
import app.enums.ShedPlacement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PartCalculatorTest
{

    @Test
    void calculatePost()
    {
        int numberOfPostWith780 = PartCalculator.calculateNumberOfPostsWithShed(780, ShedPlacement.FULL_WIDTH);
        assertEquals(11, numberOfPostWith780);

        int numberOfPostWith750 = PartCalculator. calculateNumberOfPostsWithShed(750,  ShedPlacement.LEFT);
        assertEquals(10, numberOfPostWith750);

        int numberOfPostWith780NoShed = PartCalculator.calculateNumberOfPostsWithOutShed(780);
        assertEquals(6, numberOfPostWith780NoShed);

        int numberOfPostWith750NoShed = PartCalculator.calculateNumberOfPostsWithOutShed(750);
        assertEquals(6, numberOfPostWith750NoShed);

        int numberOfPostWith600WithShed = PartCalculator.calculateNumberOfPostsWithShed(600,  ShedPlacement.FULL_WIDTH);
        assertEquals(9, numberOfPostWith600WithShed);

        int numberOfPostWith300NoShed = PartCalculator.calculateNumberOfPostsWithOutShed(300);
        assertEquals(4, numberOfPostWith300NoShed);
    }

    @Test
    void calculateRafters()
    {
        final double MAX_ACCEPTABLE_SPACING = 56.0;
        final double MIN_ACCEPTABLE_SPACING = 48.0;

        RafterCalculationDTO rafters780 = PartCalculator.calculateRafters(780, 4.5);
        assertEquals(15, rafters780.numberOfRafters());
        assertTrue(rafters780.spacing() <= MAX_ACCEPTABLE_SPACING);
        assertTrue(rafters780.spacing() >= MIN_ACCEPTABLE_SPACING);

        RafterCalculationDTO rafters750 = PartCalculator.calculateRafters(750, 4.5);
        assertEquals(15, rafters750.numberOfRafters());
        assertTrue(rafters750.spacing() <= MAX_ACCEPTABLE_SPACING);
        assertTrue(rafters750.spacing() >= MIN_ACCEPTABLE_SPACING);

        RafterCalculationDTO rafters720 = PartCalculator.calculateRafters(720, 4.5);
        assertEquals(14, rafters720.numberOfRafters());
        assertTrue(rafters720.spacing() <= MAX_ACCEPTABLE_SPACING);
        assertTrue(rafters720.spacing() >= MIN_ACCEPTABLE_SPACING);

        RafterCalculationDTO rafters600 = PartCalculator.calculateRafters(600, 4.5);
        assertEquals(12, rafters600.numberOfRafters());
        assertTrue(rafters600.spacing() <= MAX_ACCEPTABLE_SPACING);
        assertTrue(rafters600.spacing() >= MIN_ACCEPTABLE_SPACING);

        RafterCalculationDTO rafters480 = PartCalculator.calculateRafters(480, 4.5);
        assertEquals(10, rafters480.numberOfRafters());
        assertTrue(rafters480.spacing() <= MAX_ACCEPTABLE_SPACING);
        assertTrue(rafters480.spacing() >= MIN_ACCEPTABLE_SPACING);

        RafterCalculationDTO rafters360 = PartCalculator.calculateRafters(360, 4.5);
        assertEquals(8, rafters360.numberOfRafters());
        assertTrue(rafters360.spacing() <= MAX_ACCEPTABLE_SPACING);
        assertTrue(rafters360.spacing() >= MIN_ACCEPTABLE_SPACING);

        RafterCalculationDTO rafters300 = PartCalculator.calculateRafters(300, 4.5);
        assertEquals(7, rafters300.numberOfRafters());
        assertTrue(rafters300.spacing() <= MAX_ACCEPTABLE_SPACING);
        assertTrue(rafters300.spacing() >= MIN_ACCEPTABLE_SPACING);
    }
}