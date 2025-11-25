package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MaterialLine
{
    public MaterialLine(MaterialVariant materialVariant, int quantity)
    {
        this.materialVariant = materialVariant;
        this.quantity = quantity;
    }

    private int materialLineId;
    private int orderId;
    private MaterialVariant materialVariant;
    private int quantity;
}