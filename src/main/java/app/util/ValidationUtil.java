package app.util;

public class ValidationUtil
{

    public static void validateEmail(String email)
    {
        if (email == null || email.trim().isEmpty())
        {
            throw new IllegalArgumentException("Email kan ikke være tom");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
        {
            throw new IllegalArgumentException("Ikke gyldig email format");
        }
    }

    public static void validatePhoneNumber(String phone)
    {
        if (phone == null || phone.trim().isEmpty())
        {
            throw new IllegalArgumentException("Telefonnummer kan ikke være tomt");
        }

        String cleanPhone = phone.replaceAll("[\\s-]", "");

        if (!cleanPhone.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("Telefonnummer skal være 8 cifre");
        }
    }

    public static void validateZipCode(int zipCode)
    {
        if (zipCode < 800 || zipCode > 9999)
        {
            throw new IllegalArgumentException("Postnummer skal være mellem 800 og 9999");
        }
    }

    public static void validatePassword(String password)
    {
        if (password == null || password.length() < 8)
        {
            throw new IllegalArgumentException("Password skal være mindst 8 tegn");
        }

        if (!password.matches(".*[A-Z].*"))
        {
            throw new IllegalArgumentException("Password skal indeholde et stort bogstav");
        }

        if (!password.matches(".*[0-9].*"))
        {
            throw new IllegalArgumentException("Password skal indeholde et tal");
        }
    }

    public static void validateName(String name, String fieldName)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " kan ikke være tomt");
        }

        if (name.length() < 2)
        {
            throw new IllegalArgumentException(fieldName + " skal være mindst 2 tegn");
        }
    }

    public static void validateStreet(String street)
    {
        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Gade kan ikke være tom");
        }

        if (street.trim().length() < 3)
        {
            throw new IllegalArgumentException("Gade skal være mindst 3 tegn");
        }

        if (!street.matches(".*[a-zA-ZæøåÆØÅ]+.*"))
        {
            throw new IllegalArgumentException("Gade skal indeholde bogstaver");
        }
    }

    public static void validateCity(String city)
    {
        if (city == null || city.trim().isEmpty())
        {
            throw new IllegalArgumentException("By kan ikke være tom");
        }

        if (city.length() < 2)
        {
            throw new IllegalArgumentException("By skal være mindst 2 tegn");
        }
    }

    public static void validateCarportDimensions(int length, int width, int height)
    {
        if (length < 240 || length > 780)
        {
            throw new IllegalArgumentException("Carport længde skal være mellem 240 og 780 cm");
        }

        if (width < 240 || width > 600)
        {
            throw new IllegalArgumentException("Carport bredde skal være mellem 240 og 600 cm");
        }
    }
}