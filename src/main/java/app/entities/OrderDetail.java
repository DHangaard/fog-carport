package app.entities;

import app.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderDetail
{
    private int orderId;
    private User seller;
    private User customer;
    private Carport carport;
    private OrderTimeLine orderTimeLine;
    private List<MaterialLine> materialLines;
    private String customerComment;
    private PricingDetails pricingDetails;
    private OrderStatus orderStatus;
}
