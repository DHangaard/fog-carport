package app.services;

import app.entities.Carport;
import app.entities.Material;
import app.entities.MaterialLine;
import app.entities.MaterialVariant;
import app.enums.MaterialType;
import app.enums.RoofType;
import app.exceptions.DatabaseException;
import app.exceptions.MaterialNotFoundException;
import app.persistence.ConnectionPool;
import app.persistence.MaterialVariantMapper;
import org.junit.jupiter.api.Assertions;
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
    void testSmallCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 300, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        MaterialLine roofLine = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .findFirst()
                .orElseThrow();

        int roofLengthExpected = 360;
        assertEquals(roofLengthExpected, roofLine.getMaterialVariant().getVariantLength());
    }

    @Test
    void testMediumCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 560, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        MaterialLine roofLine = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .findFirst()
                .orElseThrow();

        int roofLengthExpected = 600;
        assertEquals(roofLengthExpected, roofLine.getMaterialVariant().getVariantLength());
    }

    @Test
    void testBigCarportGetsCorrectRoofPlate() throws MaterialNotFoundException, DatabaseException
    {
        Carport carport = new Carport(0, 780, 420, RoofType.TRAPEZ_ROOF, null);

        List<MaterialLine> materialLines = bomService.getBillOfMaterialByCarport(carport);

        List<MaterialLine> roofLines = materialLines.stream()
                .filter(l -> l.getMaterialVariant().getMaterial().getType() == MaterialType.ROOF)
                .collect(Collectors.toList());

        int firstRoofLengthExpected = 600;
        int secondRoofLengthExpected = 240;

        assertNotNull(roofLines);
        assertTrue(roofLines.size() == 2);
        assertEquals(firstRoofLengthExpected, roofLines.get(0).getMaterialVariant().getVariantLength());
        assertEquals(secondRoofLengthExpected, roofLines.get(1).getMaterialVariant().getVariantLength());
    }

}