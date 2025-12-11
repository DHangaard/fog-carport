package app.util;

import app.entities.Shed;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    private static final int SHED_SIDE_MARGIN = 35;

    @Test
    void testEmailIsCorrect()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("test@example.com"));
    }

    @Test
    void testEmailIsNull()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateEmail(null));
        assertEquals("Email kan ikke være tom", exception.getMessage());
    }

    @Test
    void testInvalidEmailFormatMissingASingleLetter()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateEmail("john@admin.d"));
        assertEquals("Ikke gyldig email format", exception.getMessage());
    }

    @Test
    void testInvalidEmailFormat()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateEmail("john-admin.dk"));
        assertEquals("Ikke gyldig email format", exception.getMessage());
    }

    @Test
    void testCorrectPhoneNumber()
    {
        assertDoesNotThrow(() -> ValidationUtil.validatePhoneNumber("12345678"));
        assertDoesNotThrow(() -> ValidationUtil.validatePhoneNumber("12 34 56 78"));
    }

    @Test
    void testToShortPhoneNumber()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validatePhoneNumber("1234567"));
        assertEquals("Telefonnummer skal være 8 cifre", exception.getMessage());
    }

    @Test
    void testCorrectZipCode()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateZipCode(2800));
        assertDoesNotThrow(() -> ValidationUtil.validateZipCode(9999));
    }

    @Test
    void testInvalidZipCode()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateZipCode(500));
        assertEquals("Postnummer skal være mellem 800 og 9999", exception.getMessage());
    }

    @Test
    void testCorrectPasswordFormat()
    {
        assertDoesNotThrow(() -> ValidationUtil.validatePassword("Password1"));
    }

    @Test
    void testToShortPassword()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validatePassword("Pass1"));
        assertEquals("Password skal være mindst 8 tegn", exception.getMessage());
    }

    @Test
    void testPasswordMissingOneCapitalLetter()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validatePassword("password1"));
        assertEquals("Password skal indeholde et stort bogstav", exception.getMessage());
    }

    @Test
    void testCorrectFirstName() {
        assertDoesNotThrow(() -> ValidationUtil.validateName("Morten", "Fornavn"));
    }

    @Test
    void testCorrectInvalidFirstName()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateName("M", "Fornavn"));
        assertEquals("Fornavn skal være mindst 2 tegn", exception.getMessage());
    }

    @Test
    void testValidStreetName() {
        assertDoesNotThrow(() -> ValidationUtil.validateStreet("Testvej 123"));
    }

    @Test
    void testIncorrectStreetName()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateStreet("123"));
        assertEquals("Gade skal indeholde bogstaver", exception.getMessage());
    }

    @Test
    void testValidCarportDimensionsMaxAndMin()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateCarportDimensions(240, 240));
        assertDoesNotThrow(() -> ValidationUtil.validateCarportDimensions(600, 780));
    }

    @Test
    void testCarportWidthTooSmall()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateCarportDimensions(239, 400));
        assertEquals("Carport bredde skal være mellem 240 og 600 cm", exception.getMessage());
    }

    @Test
    void testCarportWidthTooLarge()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateCarportDimensions(601, 400));
        assertEquals("Carport bredde skal være mellem 240 og 600 cm", exception.getMessage());
    }

    @Test
    void testCarportLengthTooSmall()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateCarportDimensions(400, 239));
        assertEquals("Carport længde skal være mellem 240 og 780 cm", exception.getMessage());
    }

    @Test
    void testCarportLengthTooLarge()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateCarportDimensions(400, 781));
        assertEquals("Carport længde skal være mellem 240 og 780 cm", exception.getMessage());
    }


    @Test
    void testValidShedDimensions()
    {
        Shed maxShed = new Shed(0, 780, 530, null);
        assertDoesNotThrow(() -> ValidationUtil.validateShedDimensions(600, 780, maxShed, SHED_SIDE_MARGIN));

        Shed validSmallShed = new Shed(0, 200, 210, null);
        assertDoesNotThrow(() -> ValidationUtil.validateShedDimensions(300, 400, validSmallShed, SHED_SIDE_MARGIN));
    }

    @Test
    void testShedWidthTooLargeForMargin()
    {
        Shed tooWideShed = new Shed(0, 300, 340, null);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateShedDimensions(400, 500, tooWideShed, SHED_SIDE_MARGIN));
        assertEquals("Skurets bredde på 340 cm er for stor. Den maksimale bredde for et skur er 330 cm (Carportbredde - 70 cm).", exception.getMessage());
    }

    @Test
    void testShedWidthIsEqualToMaxAllowed()
    {
        Shed maxAllowedShed = new Shed(0, 300, 430, null);
        assertDoesNotThrow(() -> ValidationUtil.validateShedDimensions(500, 500, maxAllowedShed, SHED_SIDE_MARGIN));
    }

    @Test
    void testShedLengthTooLarge()
    {
        Shed tooLongShed = new Shed(0, 501, 300, null);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateShedDimensions(600, 500, tooLongShed, SHED_SIDE_MARGIN));
        assertEquals("Skurets længde må ikke være større end carportens længde", exception.getMessage());
    }

    @Test
    void testShedWidthIsZero()
    {
        Shed zeroWidthShed = new Shed(0, 300, 0, null);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateShedDimensions(600, 500, zeroWidthShed, SHED_SIDE_MARGIN));
        assertEquals("Skuret skal have både bredde og længde", exception.getMessage());
    }

}