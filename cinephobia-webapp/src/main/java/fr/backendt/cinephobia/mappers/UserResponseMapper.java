package fr.backendt.cinephobia.mappers;

import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.UserResponseDTO;

import java.util.function.Function;

public class UserResponseMapper implements Function<User, UserResponseDTO> {
    @Override
    public UserResponseDTO apply(User user) { // ModelMapper doesn't support mapping to a Java Record :(
        return new UserResponseDTO(
                user.getId(),
                user.getDisplayName(),
                user.getEmail()
        );
    }
}
