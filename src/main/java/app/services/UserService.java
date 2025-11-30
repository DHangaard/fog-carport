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

    @Override
    public boolean updateUser(UserDTO userDTO) throws DatabaseException
    {
        validateUpdate(userDTO);

        User user = userMapper.getUserById(userDTO.userId());
        boolean changed = false;

        if (!user.getFirstName().equals(userDTO.firstName()))
        {
            user.setFirstName(userDTO.firstName());
            changed = true;
        }

        if (!user.getLastName().equals(userDTO.lastName()))
        {
            user.setLastName(userDTO.lastName());
            changed = true;
        }

        if (!user.getEmail().equals(userDTO.email()))
        {
            user.setEmail(userDTO.email());
            changed = true;
        }

        if (!user.getPhoneNumber().equals(userDTO.phoneNumber()))
        {
            user.setPhoneNumber(userDTO.phoneNumber());
            changed = true;
        }

        if (!user.getStreet().equals(userDTO.street()))
        {
            user.setStreet(userDTO.street());
            changed = true;
        }

        if (user.getZipCode() != userDTO.zipCode())
        {
            user.setZipCode(userDTO.zipCode());
            changed = true;
        }

        if (!user.getCity().equals(userDTO.city()))
        {
            user.setCity(userDTO.city());
            changed = true;
        }

        if (changed)
        {
            userMapper.updateUser(user);
        }

        return changed;
    }

    private void validateUpdate(UserDTO dto) throws DatabaseException
    {
        ValidationUtil.validateName(dto.firstName(), "Fornavn");
        ValidationUtil.validateName(dto.lastName(), "Efternavn");
        ValidationUtil.validateStreet(dto.street());
        ValidationUtil.validateZipCode(dto.zipCode());
        ValidationUtil.validateEmail(dto.email());
        ValidationUtil.validatePhoneNumber(dto.phoneNumber());

        if (!zipCodeMapper.zipCodeExists(dto.zipCode()))
        {
            throw new DatabaseException("Postnummer findes ikke: " + dto.zipCode());
        }
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
