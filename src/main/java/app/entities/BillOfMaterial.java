package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class BillOfMaterial
{
    private List<MaterialLine> materialLines = new ArrayList<>();
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
        materialLines.add(materialLine);
    }
}
