package app.dto;

import app.entities.Carport;
import app.entities.OrderTimeLine;
import app.enums.OrderStatus;

public record CustomerOfferDTO(
        int orderId,
        Carport carport,
        UserDTO customer,
        UserDTO seller,
        OrderTimeLine orderTimeLine,
        Double totalPrice,
        OrderStatus orderStatus,
        String customerComment
)
{
}
