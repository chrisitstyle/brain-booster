package com.brainbooster.model.mappers;

import com.brainbooster.model.User;
import com.brainbooster.model.dtos.UserDTO;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service

public class UserDTOMapper implements Function<User, UserDTO> {
    @Override
    public UserDTO apply(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getNickname(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
