package app.services;

import app.entities.Carport;
import app.entities.MaterialLine;
import app.enums.MaterialType;
import app.enums.RoofType;
import app.exceptions.DatabaseException;
import app.exceptions.MaterialNotFoundException;
import app.persistence.ConnectionPool;
import app.persistence.MaterialVariantMapper;
import org.junit.jupiter.api.Test;

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