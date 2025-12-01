package app.services;

import app.dto.CreateUserRequestDTO;
import app.dto.UserDTO;
import app.exceptions.DatabaseException;
import java.util.List;

public interface IUserService
{
    public UserDTO registerUser(CreateUserRequestDTO createUserRequestDTO) throws DatabaseException;
    public UserDTO login(String email, String password) throws DatabaseException;
    public boolean updateUser(UserDTO userDTO) throws DatabaseException;
    public UserDTO getUserById(int userId) throws DatabaseException;
    public List<UserDTO> getAllCustomers() throws DatabaseException;
}
