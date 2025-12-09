package app.dto;

import app.entities.Carport;

public record CreateOrderRequestDTO(
        int userId,
        Carport carport,
        String customerComment
) {
}
