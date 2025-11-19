package app.services;

import app.dto.CreateUserRequestDTO;
import app.dto.UserDTO;
import app.entities.User;
import app.enums.Role;
import app.exceptions.DatabaseException;
import app.persistence.UserMapper;
import app.persistence.ZipCodeMapper;
import app.util.PasswordUtil;
import app.util.ValidationUtil;

import java.util.List;
import java.util.stream.Collectors;

public class UserService implements IUserService
{
    private UserMapper userMapper;
    private ZipCodeMapper zipCodeMapper;

    public UserService(UserMapper userMapper, ZipCodeMapper zipCodeMapper)
    {
        this.userMapper = userMapper;
        this.zipCodeMapper = zipCodeMapper;
    }

    @Override
    public UserDTO registerUser(CreateUserRequestDTO createUserRequestDTO) throws DatabaseException
    {
        ValidationUtil.validateName(createUserRequestDTO.firstName(), "Fornavn");
        ValidationUtil.validateName(createUserRequestDTO.lastName(), "Efternavn");
        ValidationUtil.validateEmail(createUserRequestDTO.email());
        ValidationUtil.validatePassword(createUserRequestDTO.password1());
        ValidationUtil.validatePhoneNumber(createUserRequestDTO.phoneNumber());
        ValidationUtil.validateStreet(createUserRequestDTO.street());
        ValidationUtil.validateZipCode(createUserRequestDTO.zipCode());

        if(!createUserRequestDTO.password1().equals(createUserRequestDTO.password2()))
        {
            throw new IllegalArgumentException("De to passwords er ikke ens");
        }

        if (!zipCodeMapper.zipCodeExists(createUserRequestDTO.zipCode()))
        {
            throw new DatabaseException("Postnummer " + createUserRequestDTO.zipCode()  + " findes ikke");
        }

        String hashedPassword = PasswordUtil.hashPassword(createUserRequestDTO.password1());
        String cleanPhone = createUserRequestDTO.phoneNumber().replaceAll("[\\s-]", "");

        User user = userMapper.createUser(
                createUserRequestDTO.firstName(),
                createUserRequestDTO.lastName(),
                createUserRequestDTO.email(),
                hashedPassword,
                cleanPhone,
                createUserRequestDTO.street(),
                createUserRequestDTO.zipCode()
        );

        return buildUserDTO(user);
    }

    @Override
    public UserDTO login(String email, String password) throws DatabaseException
    {
        ValidationUtil.validateEmail(email);

        User user = userMapper.getUserByEmail(email);

        if (!PasswordUtil.verifyPassword(password, user.getHashedPassword()))
        {
            throw new DatabaseException("Forkert password. Prøv igen");
        }

        return buildUserDTO(user);
    }

    @Override
    public UserDTO getUserById(int userId) throws DatabaseException
    {
        User user = userMapper.getUserById(userId);
        return buildUserDTO(user);
    }

    @Override
    public List<UserDTO> getAllCustomers() throws DatabaseException
    {
        List<User> users = userMapper.getAllUsers();

        return users.stream()
                .filter(user -> user.getRole().equals(Role.CUSTOMER))
                .map(user -> buildUserDTO(user))
                .collect(Collectors.toList());
    }

    private UserDTO buildUserDTO(User user)
    {
        return new UserDTO(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getStreet(),
                user.getZipCode(),
                user.getCity(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }
}
