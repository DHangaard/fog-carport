package app.entities;

import app.enums.MaterialCategory;
import app.enums.MaterialType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}

