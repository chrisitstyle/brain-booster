package com.brainbooster.user;

import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.mapper.FlashcardSetDTOMapper;
import com.brainbooster.user.dto.UserCreationDTO;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.user.dto.UserNicknameUpdateDTO;
import com.brainbooster.user.dto.UserUpdateDTO;
import com.brainbooster.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserDTOMapper userDTOMapper;
    private final PasswordEncoder passwordEncoder;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardSetDTOMapper flashcardSetDTOMapper;


    public UserDTO addUser(UserCreationDTO userCreationDTO) {
        if (userRepository.existsByEmail(userCreationDTO.email())) {
            throw new EmailAlreadyExistsException("User with this email already exists!");
        }

        User user = User.builder()
                .nickname(userCreationDTO.nickname())
                .email(userCreationDTO.email())
                .password(passwordEncoder.encode(userCreationDTO.password()))
                .role(userCreationDTO.role())
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        return userDTOMapper.apply(savedUser);
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
                .orElseThrow(() -> new NoSuchElementException("User with this id does not exist"));
    }

    public List<FlashcardSetDTO> getAllFlashcardSetsByUserId(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User with id: " + userId + " not found");
        }

        return flashcardSetRepository.findByUserId(userId)
                .stream()
                .map(flashcardSetDTOMapper)
                .toList();
    }

    public List<FlashcardSetDTO> getAllFlashcardSetsByUserNickname(String nickname) {

        if (!userRepository.existsByNickname(nickname)) {
            throw new NoSuchElementException("User with nickname: " + nickname + " not found");
        }

        return flashcardSetRepository.findAllByUserNickname(nickname)
                .stream()
                .map(flashcardSetDTOMapper)
                .toList();
    }

    @Transactional
    public UserDTO updateUserNickname(UserNicknameUpdateDTO updatedUserNickname, Long userId) {

        User authUser = SecurityUtils.getAuthenticatedUser();

        boolean isAdmin = authUser.getRole().equals(Role.ADMIN);
        boolean isOwner = Objects.equals(authUser.getUserId(), userId);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not allowed to update other users");
        }

        User existingUserFromDB = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id: " + userId + " not found"));

        existingUserFromDB.setNickname(updatedUserNickname.nickname());
        User savedUser = userRepository.save(existingUserFromDB);
        return userDTOMapper.apply(savedUser);

    }
    @Transactional
    public UserDTO updateUser(UserUpdateDTO updatedUser, Long userId) {

        User authUser = SecurityUtils.getAuthenticatedUser();

        boolean isAdmin = authUser.getRole().equals(Role.ADMIN);

        if (!isAdmin) {
            throw new AccessDeniedException("You are not allowed to update other users");
        }

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id: " + userId + " not found"));

        updateUserFields(existingUser, updatedUser);

        userRepository.save(existingUser);
        return userDTOMapper.apply(existingUser);

    }

    @Transactional
    public void deleteUserById(Long userId) {

        User authUser = SecurityUtils.getAuthenticatedUser();

        boolean isAdmin = authUser.getRole().equals(Role.ADMIN);
        boolean isOwner = Objects.equals(authUser.getUserId(), userId);

        if (isOwner || !isAdmin) {
            throw new AccessDeniedException("You cannot delete yourself or other users");
        }

        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User with id: " + userId + " not found");
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
