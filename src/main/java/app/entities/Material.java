package app.entities;

import app.enums.MaterialCategory;
import app.enums.MaterialType;
import lombok.Data;

@Data
public class Material
{
    private int materialId;
    private String name;
    private MaterialCategory materialCategory;
    private MaterialType materialType;
    private Integer materialWidth;
    private Integer materialHeight;
    private String unit;
    private String usage;
    private Integer materialVariantId;
    private double unitPrice;
    private int variantLength;
    private String description;

    Material(int materialId,
             String name,
             MaterialCategory materialCategory,
             MaterialType materialType,
             Integer materialWidth,
             Integer materialHeight,
             String unit,
             String usage,
             Integer materialVariantId,
             double unitPrice,
             int variantLength)
    {
        this.materialId = materialId;
        this.name = name;
        this.materialCategory = materialCategory;
        this.materialType = materialType;
        this.materialWidth = materialWidth;
        this.materialHeight = materialHeight;
        this.unit = unit;
        this.usage = usage;
        this.materialVariantId = materialVariantId;
        this.unitPrice = unitPrice;
        this.variantLength = variantLength;

        this.description = generateDescription();
    }

    private String generateDescription()
    {
        StringBuilder description = new StringBuilder();
        if (this.materialWidth != null && this.materialHeight != null)
        {
            description.append(materialWidth).append("x");
            description.append(materialHeight).append(" mm. ");
        }
        else if (this.materialWidth != null && this.materialHeight == null)
        {
            description.append(materialWidth).append(" mm. ");
        }
        else if (this.materialWidth == null && this.materialHeight != null)
        {
            description.append(materialHeight).append(" mm. ");
        }
        description.append(name);
        return description.toString();
    }
}
