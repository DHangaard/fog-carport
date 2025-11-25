package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MaterialLine
{
    private int materialLineId;
    private int orderId;
    private MaterialVariant materialVariant;
    private int quantity;
}