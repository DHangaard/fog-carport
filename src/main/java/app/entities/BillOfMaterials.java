package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BillOfMaterials
{
    private int bomId;
    private int offerId;
    private List<MaterialLine> materialLines;
    private PricingDetails pricingDetails;

    public void addMaterialLine(MaterialLine materialLine)
    {
        if(materialLine != null)
        {
            materialLines.add(materialLine);
        }
    }

    public void removeMaterialLine(MaterialLine materialLine)
    {
        if(materialLine != null)
        {
            materialLines.remove(materialLine);
        }
    }

    public void calculatePrice(double coverage, double vat)
    {
        //TODO implemented calculation
    }
}
