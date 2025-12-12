package app.util;

import app.exceptions.PropertyException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProperties
{
    private static final Properties PROPERTIES = new Properties();
    private static final String NOT_FOUND = "Variablen %s findes ikke i app.properties";
    private static final String EMPTY = "Variablen %s i app.properties har ingen tilegnet værdi";

    static
    {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("app.properties"))
        {
            if (inputStream == null)
            {
                throw new PropertyException("app.properties ikke fundet på classpath (fx src/main/resources/app.properties)");
            }
            PROPERTIES.load(inputStream);
        }
        catch (IOException e)
        {
            throw new PropertyException("Fejl ved indlæsning af app.properties: " + e.getMessage());
        }
    }

    public static String getRequired(String key)
    {
        String property = getTrimmedProperty(key);
        return property;
    }

    public static int getRequiredInt(String key)
    {
        String property = getTrimmedProperty(key);
        try
        {
            return Integer.parseInt(property);
        }
        catch (NumberFormatException e)
        {
            throw new PropertyException("Variablen " + key + " i app.properties er ikke et gyldigt heltal: " + property, e.getCause());
        }
    }

    public static double getRequiredDouble(String key)
    {
        String property = getTrimmedProperty(key);
        try
        {
            return Double.parseDouble(property);
        }
        catch (NumberFormatException e)
        {
            throw new PropertyException("Variablen " + key + " i app.properties er ikke et gyldigt tal: " + property, e.getCause());
        }

    }

    private static String getTrimmedProperty(String key)
    {
        String property = PROPERTIES.getProperty(key);

        if (property == null) {
            throw new PropertyException(String.format(NOT_FOUND, key));
        }

        property = property.trim();

        if (property.isEmpty()) {
            throw new PropertyException(String.format(EMPTY, key));
        }
        return property;
    }
}
