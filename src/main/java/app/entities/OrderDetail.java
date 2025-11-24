package app.entities;

import app.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class OrderDetail
{
    private int orderId;
    private Offer offer;
    private Timestamp orderDateTime;
    private OrderStatus status;
}