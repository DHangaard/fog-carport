package app.entities;

import app.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Offer
{
    private int offerId;
    private int customerId;
    private Integer sellerId;
    private int carportId;
    private OrderTimeLine orderTimeLine;
    private String customerComment;
    private OrderStatus orderStatus;
}
