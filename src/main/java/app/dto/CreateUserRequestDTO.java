package app.dto;

public record CreateUserRequestDTO(
        String firstName,
        String lastName,
        String email,
        String password1,
        String password2,
        String phoneNumber,
        String street,
        int zipCode
) {}