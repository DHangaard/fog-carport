package app.util;

import java.util.Locale;

public class PriceFormatUtil
{
    private static final Locale DANISH_LOCALE = new Locale("da", "DK");

    public static String getFormattedPrice(double price)
    {
        return String.format(DANISH_LOCALE, "%,.2f", price);
    }

    public static String getFormattedCoveragePercentage(double coveragePercentage)
    {
        return String.format(DANISH_LOCALE, "%.1f", coveragePercentage);
    }

    public static String getFormattedProfit(double priceWithoutVat, double costPrice)
    {
        return String.format(DANISH_LOCALE, "%,.2f", priceWithoutVat - costPrice);
    }
}
