package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialVariant
{
    private int materialVariantId;
    private int materialId;
    private Integer variantLength;
    private double unitPrice;
    private Integer piecesPerUnit;
    private Material material;

    public MaterialVariant(int materialVariantId, int materialId, Integer variantLength, double unitPrice, Integer piecesPerUnit) {
        this(materialVariantId, materialId, variantLength, unitPrice, piecesPerUnit, null);
    }
}
