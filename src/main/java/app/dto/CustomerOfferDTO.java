package app.dto;

import app.entities.Carport;
import app.entities.OrderTimeLine;
import app.entities.User;
import app.enums.OrderStatus;

public record CustomerOfferDTO(
        int orderId,
        Carport carport,
        User customer,
        String sellerFullName,
        OrderTimeLine orderTimeLine,
        Double totalPrice,
        OrderStatus orderStatus,
        String customerComment
)
{
}
