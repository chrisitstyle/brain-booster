package com.brainbooster.service;

import com.brainbooster.model.User;
import com.brainbooster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public ResponseEntity<User> addUser(User user){
        Optional<User> userFromDatabase = userRepository.findByEmail(user.getEmail());
        if(userFromDatabase.isPresent()){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "User with this email already exists");
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User getUserById(long userId){
        return userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this id does not exist"));
    }

}
