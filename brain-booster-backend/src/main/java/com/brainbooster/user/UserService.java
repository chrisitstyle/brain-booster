package com.brainbooster.user;

import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.flashcardset.FlashcardSetDTO;
import com.brainbooster.flashcardset.FlashcardSetDTOMapper;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserDTOMapper userDTOMapper;
    private final PasswordEncoder passwordEncoder;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardSetDTOMapper flashcardSetDTOMapper;


    public UserDTO addUser(User user){
        if(userRepository.existsByEmail(user.getEmail())){
            throw new EmailAlreadyExistsException("User with this email already exists!");
        }

        if(user.getCreatedAt() == null){
            user.setCreatedAt(LocalDateTime.now());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return userDTOMapper.apply(savedUser);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "userId" ))
                .stream()
                .map(userDTOMapper)
                .toList();
    }
    public UserDTO getUserById(long userId){
        return userRepository.findById(userId).
                map(userDTOMapper)
        .orElseThrow(() -> new NoSuchElementException("User with this id does not exist"));
    }

    public List<FlashcardSetDTO> getAllFlashcardSetsByUserId(long userId) {

        if(!userRepository.existsById(userId)){
            throw new NoSuchElementException("User with id: " + userId + " not found");
        }

        return flashcardSetRepository.findByUserId(userId)
                .stream()
                .map(flashcardSetDTOMapper)
                .toList();
    }

    @Transactional
    public UserDTO updateUser(User updatedUser, long userId){

        User authUser = getAuthenticatedUser();

        boolean isAdmin = authUser.getRole().equals(Role.ADMIN);
        boolean isOwner = authUser.getUserId() == userId;

        if(!isAdmin && !isOwner){
            throw new AccessDeniedException("You are not allowed to update other users");
        }

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id: " + userId + " not found"));

        updateUserFields(existingUser, updatedUser);

        userRepository.save(existingUser);
        return userDTOMapper.apply(existingUser);

    }

    @Transactional
    public void deleteUserById(long userId){

        User authUser = getAuthenticatedUser();

        boolean isAdmin = authUser.getRole().equals(Role.ADMIN);
        boolean isOwner = authUser.getUserId() == userId;

        if (isOwner || !isAdmin) {
            throw new AccessDeniedException("You cannot delete yourself or other users");
        }

        if(!userRepository.existsById(userId)){
            throw new NoSuchElementException("User with id: " + userId + " not found");
        }

        userRepository.deleteById(userId);
    }

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void updateUserFields(User existingUser, User updatedUser){

        existingUser.setNickname(updatedUser.getNickname());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        existingUser.setRole(updatedUser.getRole());

    }

}
