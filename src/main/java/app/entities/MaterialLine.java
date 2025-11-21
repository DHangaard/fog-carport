package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MaterialLine
{
    private Material material;
    private int bomId;
    private int materialId;
    private int quantity;
    private double lineTotal;
}