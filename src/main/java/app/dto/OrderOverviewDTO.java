package app.dto;


import app.enums.OrderStatus;

import java.sql.Timestamp;

public record OrderOverviewDTO(
        int orderId,
        String customerFullName,
        String email,
        Timestamp customerRequestCreatedAt,
        double totalPrice,
        OrderStatus orderStatus
) {
}
