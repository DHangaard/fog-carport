package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BillOfMaterials
{
    private int bomId;
    private int offerId;
    private List<MaterialLine> materialLines;
    private PricingDetails pricingDetails;
}
