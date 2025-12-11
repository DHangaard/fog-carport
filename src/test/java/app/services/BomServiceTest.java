package app.services;

import app.entities.*;
import app.enums.MaterialType;
import app.enums.RoofType;
import app.enums.ShedPlacement;
import app.exceptions.DatabaseException;
import app.exceptions.MaterialNotFoundException;
import app.persistence.ConnectionPool;
import app.persistence.MaterialVariantMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BomServiceTest
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=public";
    private static final String DB = "carport";
    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static MaterialVariantMapper variantMapper = new MaterialVariantMapper(connectionPool);
    private static IBomService bomService = new BomService(variantMapper);


    @Test
    void testPricingCalculator() throws DatabaseException
    {
        List<MaterialVariant> postVariants = variantMapper.getAllVariantsByType(MaterialType.POST);
        List<MaterialVariant> rafterVariants = variantMapper.getAllVariantsByType(MaterialType.RAFTER);

        List<MaterialLine> materialLines = new ArrayList<>();
        materialLines.add(new MaterialLine(postVariants.get(0),4)); //Price 221.85 x 4
        materialLines.add(new MaterialLine(rafterVariants.get(0),4)); //Price 158.85 x 4

        double expectedCostPrice = 1522.80;
        double coveragePercentage = 40.0;
        double expectedPriceWithCoveragePercentage = expectedCostPrice  / (1 - coveragePercentage / 100.0);

        PricingDetails pricingDetails = bomService.calculateCarportPrice(materialLines);

        assertNotNull(pricingDetails);
        assertEquals(expectedCostPrice, pricingDetails.getCostPrice());
        assertTrue(coveragePercentage == pricingDetails.getCoveragePercentage());
        assertEquals(expectedPriceWithCoveragePercentage, pricingDetails.getPriceWithoutVat());

    }


    @Test
    void test240CmWidthCarportGetsCorrectRafters() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 240, 240, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.RAFTER)
                .collect(Collectors.toList());

        int expectedLength = 300;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 1);
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test330CmWidthCarportGetsCorrectRafters() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 500, 330, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.RAFTER)
                .collect(Collectors.toList());

        int expectedLength = 360;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 1);
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }


    @Test
    void test600CmWidthCarportGetsCorrectRafters() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 600, 600, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.RAFTER)
                .collect(Collectors.toList());

        int expectedLength = 600;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 1);
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test240CmCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 240, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int expectedLength = 300;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 1);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test360CmCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 360, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int expectedLength = 360;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 1 );
        assertTrue(beamLines.get(0).getMaterialVariant().getVariantLength() >= 360);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test600CmCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 600, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int expectedLength = 600;

        assertTrue(beamLines.size() == 1);
        assertNotNull(beamLines);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(expectedLength, beamLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test780CmCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 780, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int firstBeam = 420;
        int secondBeam = 420;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 2);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(2, beamLines.get(1).getQuantity());
        assertEquals(firstBeam, beamLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondBeam, beamLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test750CmWithShedCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 750, 420, RoofType.TRAPEZ_ROOF, new Shed(0,200,200, ShedPlacement.FULL_WIDTH));

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int firstBeam = 420;
        int secondBeam = 720;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 2);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(1, beamLines.get(1).getQuantity());
        assertEquals(firstBeam, beamLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondBeam, beamLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test780CmWithShedCarportGetsCorrectBeams() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 780, 420, RoofType.TRAPEZ_ROOF, new Shed(0,400,200, ShedPlacement.FULL_WIDTH));

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> beamLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.BEAM)
                .collect(Collectors.toList());

        int firstBeam = 480;
        int secondBeam = 660;

        assertNotNull(beamLines);
        assertTrue(beamLines.size() == 2);
        assertEquals(2, beamLines.get(0).getQuantity());
        assertEquals(1, beamLines.get(1).getQuantity());
        assertEquals(firstBeam, beamLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondBeam, beamLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test240CmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 240, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int roofLengthExpected = 300;
        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 1);
        assertEquals(roofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test360CmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 360, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int roofLengthExpected = 420;
        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 1);
        assertEquals(roofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test300CmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 300, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int roofLengthExpected = 360;
        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 1);
        assertEquals(roofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
    }

    @Test
    void test560CmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 570, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int roofLengthExpected = 600;
        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 1);
        assertEquals(roofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
    }


    @Test
    void test600cmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 600, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int firstRoofLengthExpected = 300;
        int secondRoofLengthExpected = 360;

        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 2);
        assertEquals(firstRoofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondRoofLengthExpected, roofLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test630cmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 630, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int firstRoofLengthExpected = 300;
        int secondRoofLengthExpected = 420;

        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 2);
        assertEquals(firstRoofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondRoofLengthExpected, roofLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test660cmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 660, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int firstRoofLengthExpected = 300;
        int secondRoofLengthExpected = 420;

        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 2);
        assertEquals(firstRoofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondRoofLengthExpected, roofLines.get(1).getMaterialVariant().getVariantLength());
    }

    @Test
    void test690cmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 690, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int firstRoofLengthExpected = 300;
        int secondRoofLengthExpected = 480;

        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 2);
        assertEquals(firstRoofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondRoofLengthExpected, roofLines.get(1).getMaterialVariant().getVariantLength());
    }


    @Test
    void test780CmCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 780, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int firstRoofLengthExpected = 300;
        int secondRoofLengthExpected = 600;

        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 2);
        assertEquals(firstRoofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondRoofLengthExpected, roofLines.get(1).getMaterialVariant().getVariantLength());
    }
}