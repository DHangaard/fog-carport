package app.dto;

import app.entities.Carport;

public record CreateOrderRequest(
        int userId,
        Carport carport,
        String customerComment
) {
}
