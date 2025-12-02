package app.entities;

import app.enums.MaterialCategory;
import app.enums.MaterialType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Material
{
    private int materialId;
    private String name;
    private MaterialCategory category;
    private MaterialType type;
    private Integer materialWidth;
    private Integer materialHeight;
    private String unit;
    private String usage;
    private String description;

    public Material(int materialId, String name, MaterialCategory category, MaterialType type,
                    Integer materialWidth, Integer materialHeight, String unit, String usage)
    {
        this.materialId = materialId;
        this.name = name;
        this.category = category;
        this.type = type;
        this.materialWidth = materialWidth;
        this.materialHeight = materialHeight;
        this.unit = unit;
        this.usage = usage;
        this.description = generateDescription();
    }

    private String generateDescription()
    {
        StringBuilder description = new StringBuilder();

        description.append(name).append(" ");
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

        return description.toString();
    }
}

