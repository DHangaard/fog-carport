package app.dto;

import app.enums.Role;

public record UserDTO(
        int userId,
        String firstName,
        String lastName,
        String street,
        int zipCode,
        String city,
        String email,
        String phoneNumber,
        Role role
)
{}
