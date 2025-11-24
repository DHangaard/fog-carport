package app.entities;

import lombok.Data;

@Data
public class PricingDetails
{
    private final double costPrice;
    private final double coveragePercentage;
    private final double priceWithoutVat;
    private final double vatAmount;
    private final double totalPrice;


    public PricingDetails(double costPrice, double coveragePercentage)
    {
        this.costPrice = costPrice;
        this.coveragePercentage = coveragePercentage;

        this.priceWithoutVat = costPrice * (1 + coveragePercentage / 100.0);
        this.vatAmount = priceWithoutVat * 0.25;
        this.totalPrice = priceWithoutVat + vatAmount;
    }
}
