package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PricingDetails
{
    private double costPrice;
    private double priceWithOutVat;
    private double totalPrice;
}
