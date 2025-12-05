package app.entities;

import lombok.Data;

import java.util.Locale;

@Data
public class PricingDetails
{
    private double costPrice;
    private double coveragePercentage;
    private double priceWithoutVat;
    private final double VAT_PERCENTAGE = 0.25;
    private double vatAmount;
    private double totalPrice;
    private static final Locale DANISH_LOCALE = new Locale("da", "DK");

    public PricingDetails(double costPrice, double coveragePercentage)
    {
        this.costPrice = costPrice;
        this.coveragePercentage = coveragePercentage;
        this.priceWithoutVat = costPrice / (1 - coveragePercentage / 100.0);
        this.vatAmount = priceWithoutVat * VAT_PERCENTAGE;
        this.totalPrice = priceWithoutVat + vatAmount;
    }

    public String getFormattedCostPrice()
    {
        return String.format(DANISH_LOCALE, "%,.2f", costPrice);
    }

    public String getFormattedPriceWithoutVat()
    {
        return String.format(DANISH_LOCALE, "%,.2f", priceWithoutVat);
    }

    public String getFormattedTotalPrice()
    {
        return String.format(DANISH_LOCALE, "%,.2f", totalPrice);
    }

    public String getFormattedVatAmount()
    {
        return String.format(DANISH_LOCALE, "%,.2f", vatAmount);
    }

    public String getFormattedCoveragePercentage()
    {
        return String.format(DANISH_LOCALE, "%.1f", coveragePercentage);
    }

    public String getFormattedProfit()
    {
        return String.format(DANISH_LOCALE, "%,.2f", priceWithoutVat - costPrice);
    }
}
