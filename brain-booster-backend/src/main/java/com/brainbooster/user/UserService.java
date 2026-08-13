package com.brainbooster.user;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.security.authorization.AdminPolicy;
import com.brainbooster.security.authorization.UserDeletionPolicy;
import com.brainbooster.user.dto.UserCreationDTO;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.user.dto.UserUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserDTOMapper userDTOMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;
    private final UserAccountCreator userAccountCreator;
    private final AdminPolicy adminPolicy;
    private final UserDeletionPolicy userDeletionPolicy;


    public UserDTO addUser(UserCreationDTO userCreationDTO) {
        User createdUser = userAccountCreator.create(
                userCreationDTO.nickname(),
                userCreationDTO.email(),
                userCreationDTO.password(),
                userCreationDTO.role());

        return userDTOMapper.apply(createdUser);
    }

    public UserDTO getCurrentUser() {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();
        return userRepository.findById(authenticatedUser.userId())
                .map(userDTOMapper).orElseThrow(() ->
                        new ResourceNotFoundException("Authenticated user does not exist"));
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "userId"))
                .stream()
                .map(userDTOMapper)
                .toList();
    }

    public UserDTO getUserById(Long userId) {
        return userRepository.findById(userId).
                map(userDTOMapper)
                .orElseThrow(() -> new ResourceNotFoundException("User with this id does not exist"));
    }

    @Transactional
    public UserDTO updateUser(UserUpdateDTO updatedUser, Long userId) {

        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        adminPolicy.verify(authenticatedUser);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found"));

        updateUserFields(existingUser, updatedUser);

        userRepository.save(existingUser);
        return userDTOMapper.apply(existingUser);

    }

    @Transactional
    public void deleteUserById(Long userId) {

        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        userDeletionPolicy.verify(authenticatedUser, userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with id: " + userId + " not found");
        }

        userRepository.deleteById(userId);
    }

    private void updateUserFields(User existingUser, UserUpdateDTO updatedUser) {

        existingUser.setNickname(updatedUser.nickname());
        existingUser.setEmail(updatedUser.email());
        existingUser.setPassword(passwordEncoder.encode(updatedUser.password()));
        existingUser.setRole(updatedUser.role());

    }

}
