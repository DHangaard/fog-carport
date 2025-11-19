package app.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

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
    void testPasswordMissingOneCapitalLetter() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validatePassword("password1"));
        assertEquals("Password skal indeholde et stort bogstav", exception.getMessage());
    }

    @Test
    void testCorrectFirstName() {
        assertDoesNotThrow(() -> ValidationUtil.validateName("Morten", "Fornavn"));
    }

    @Test
    void testCorrectInvalidFirstName() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateName("M", "Fornavn"));
        assertEquals("Fornavn skal være mindst 2 tegn", exception.getMessage());
    }

    @Test
    void testValidStreetName() {
        assertDoesNotThrow(() -> ValidationUtil.validateStreet("Testvej 123"));
    }

    @Test
    void testIncorrectStreetName() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateStreet("123"));
        assertEquals("Gade skal indeholde bogstaver", exception.getMessage());
    }

}