package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Material
{
    private int materialId;
    private int materialVariantId;
    private String name;
    private double unitPrice;

    private Enum materialType;
    private Enum materialCategory;

    private String unit;
    private String usage;
    private String description;

    private int variantLength;
    private int materialWidth;
    private int materialHeight;
}
