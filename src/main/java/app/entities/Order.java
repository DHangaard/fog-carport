package app.entities;

import app.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Order
{
    private int orderId;
    private int customerId;
    private Integer sellerId;
    private int carportId;
    private Timestamp customerRequestCreatedAt;
    private Timestamp createdAt;
    private Integer offerValidDays;
    private String customerComment;
    private OrderStatus orderStatus;
    private PricingDetails pricingDetails;
}
