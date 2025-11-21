package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Order
{
    private int orderId;
    private Offer offer;
    private BillOfMaterials billOfMaterials;
    private Timestamp orderDateTime;
    private OrderStatus status;
    private boolean paymentConfirmed;
}
