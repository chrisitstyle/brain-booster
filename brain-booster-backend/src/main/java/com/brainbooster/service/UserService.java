package com.brainbooster.service;

import com.brainbooster.model.User;
import com.brainbooster.model.dtos.UserDTO;
import com.brainbooster.model.mappers.UserDTOMapper;
import com.brainbooster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserDTOMapper userDTOMapper;

    public User addUser(User user){
        Optional<User> userFromDatabase = userRepository.findByEmail(user.getEmail());
        if(userFromDatabase.isPresent()){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "User with this email already exists");
        }
        if(user.getCreatedAt() == null){
            user.setCreatedAt(LocalDateTime.now());
        }
        return userRepository.save(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "userId" ))
                .stream()
                .map(userDTOMapper)
                .toList();
    }
    public UserDTO getUserById(Long userId){
        return userRepository.findById(userId).
                map(userDTOMapper)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this id does not exist"));
    }

    @Transactional
    public User updateUser(User updatedUser, Long userId){
        return userRepository.findById(userId)
                .map(user -> {
                    user.setNickname(updatedUser.getNickname());
                    user.setEmail(updatedUser.getEmail());
                    user.setPassword(updatedUser.getPassword());
                    user.setRole(updatedUser.getRole());

                    return userRepository.save(user);
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found"));

    }

    @Transactional
    public void deleteUserById(Long userId){

        Optional<User> userExists = userRepository.findById(userId);
        if(userExists.isPresent()){
            userRepository.deleteById(userId);
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id: " + userId + " doesn't exist");
        }
    }

}
