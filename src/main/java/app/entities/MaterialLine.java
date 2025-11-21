package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MaterialLine
{
    private int materialLineId;
    private int bomId;
    private int materialId;
    private int quantity;
    private double lineTotal;
}