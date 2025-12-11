package app.util;

import app.entities.Carport;
import app.entities.Shed;
import app.enums.RoofType;
import app.enums.ShedPlacement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PostPlacementCalculatorUtilTest
{
    @Test
    void testCalculateCenterPostPlacementWith220cmShed()
    {
        Carport carport = new Carport(0, 780, 600, RoofType.FLAT, new Shed(0, 220, 220, ShedPlacement.FULL_WIDTH));
        int centerpostPlacement = PostPlacementCalculatorUtil.calculateCenterPostPlacement(carport);

        assertEquals(410, centerpostPlacement);
    }

    @Test
    void testCalculateCenterPostPlacementWith270cmShed()
    {
        Carport carport = new Carport(0, 780, 600, RoofType.FLAT, new Shed(0, 270, 220, ShedPlacement.FULL_WIDTH));
        int centerpostPlacement = PostPlacementCalculatorUtil.calculateCenterPostPlacement(carport);

        assertEquals(380, centerpostPlacement);
    }

    @Test
    void testCalculateCenterPostPlacementWith380cmShed()
    {
        Carport carport = new Carport(0, 780, 600, RoofType.FLAT, new Shed(0, 380, 220, ShedPlacement.FULL_WIDTH));
        int centerpostPlacement = PostPlacementCalculatorUtil.calculateCenterPostPlacement(carport);

        assertEquals(480, centerpostPlacement);
    }

    @Test
    void testCalculateCenterPostPlacementWithShedLengthEqualToCenterPost()
    {
        Carport carport = new Carport(0, 780, 600, RoofType.FLAT, new Shed(0, 340, 220, ShedPlacement.FULL_WIDTH));
        int centerpostPlacement = PostPlacementCalculatorUtil.calculateCenterPostPlacement(carport);

        assertEquals(410, centerpostPlacement);
    }
}