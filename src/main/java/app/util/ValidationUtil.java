package app.util;

import app.entities.Shed;

public class ValidationUtil
{

    public static String validateEmail(String email)
    {
        if (email == null || email.trim().isEmpty())
        {
            throw new IllegalArgumentException("Email kan ikke være tom");
        }


        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
        {
            throw new IllegalArgumentException("Ikke gyldig email format");
        }

        return email.trim();
    }

    public static String validatePhoneNumber(String phone)
    {
        if (phone == null || phone.trim().isEmpty())
        {
            throw new IllegalArgumentException("Telefonnummer kan ikke være tomt");
        }

        phone = phone.trim();

        String cleanPhone = phone.replaceAll("[\\s-]", "");

        if (!cleanPhone.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("Telefonnummer skal være 8 cifre");
        }

        return cleanPhone;
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

    public static String validateName(String name, String fieldName)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " kan ikke være tomt");
        }

        if (name.length() < 2)
        {
            throw new IllegalArgumentException(fieldName + " skal være mindst 2 tegn");
        }

        return name.trim();
    }

    public static String validateStreet(String street)
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

        return street.trim();
    }

    public static void validateQuantity(int quantity)
    {
        if(quantity < 0)
        {
            throw new IllegalArgumentException("Antal skal være positivt");
        }
    }

    public static void validateSearchTypeAndQuery(String searchType, String query)
    {
        if(searchType == null || searchType.isEmpty() || query == null || query.isEmpty())
        {
            throw new IllegalArgumentException("Dit søge input mangler en type eller søgning prøv igen");
        }
    }

    public static void validateMaterialValue(String value, String fieldName)
    {
        if (value == null || value.trim().isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " kan ikke være tom");
        }

        if (value.length() < 2)
        {
            throw new IllegalArgumentException(fieldName + " skal være mindst 2 tegn");
        }
    }

    public static void validateCarportDimensions(int carportWidth, int carportLength)
    {
        if (carportWidth < 240 || carportWidth > 600)
        {
            throw new IllegalArgumentException("Carport bredde skal være mellem 240 og 600 cm");
        }

        if (carportLength < 240 || carportLength > 780)
        {
            throw new IllegalArgumentException("Carport længde skal være mellem 240 og 780 cm");
        }
    }

    public static void validateShedDimensions(int carportWidth, int carportLength, Shed shed, int shedSideMargin)
    {
        int shedSides = 2;
        int maxAllowedShedWidth = carportWidth - (shedSides * shedSideMargin);

        if (shed.getWidth() <= 0 || shed.getLength() <= 0)
        {
            throw new IllegalArgumentException("Skuret skal have både bredde og længde");
        }

        if (shed.getWidth() > carportWidth)
        {
            throw new IllegalArgumentException("Skurets bredde må ikke være større end carportens bredde");
        }

        if (shed.getLength() > carportLength)
        {
            throw new IllegalArgumentException("Skurets længde må ikke være større end carportens længde");
        }

        if (shed.getWidth() > carportWidth)
        {
            throw new IllegalArgumentException("Skurets bredde må ikke være større end carportens bredde");
        }

        if (shed.getWidth() > maxAllowedShedWidth)
        {
            throw new IllegalArgumentException("Skurets bredde på " + shed.getWidth() + " cm er for stor. Den maksimale bredde for et skur er " + maxAllowedShedWidth + " cm (Carportbredde - 70 cm).");
        }

        if (shed.getLength() > carportLength)
        {
            throw new IllegalArgumentException("Skurets længde må ikke være større end carportens længde");
        }
    }
}