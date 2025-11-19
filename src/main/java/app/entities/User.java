package app.entities;

import app.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class User
{
    private int userId;
    private String firstName;
    private String lastName;
    private String hashedPassword;
    private String street;
    private int zipCode;
    private String city;
    private String email;
    private String phoneNumber;
    private Role role;
}
