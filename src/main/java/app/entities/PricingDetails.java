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

    public PricingDetails(double costPrice, double coveragePercentage)
    {
        this.costPrice = costPrice;
        this.coveragePercentage = coveragePercentage;
        this.priceWithoutVat = costPrice / (1 - coveragePercentage / 100.0);
        this.vatAmount = priceWithoutVat * VAT_PERCENTAGE;
        this.totalPrice = priceWithoutVat + vatAmount;
    }
}
