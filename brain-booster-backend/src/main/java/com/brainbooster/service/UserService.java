package com.brainbooster.service;

import com.brainbooster.model.User;
import com.brainbooster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
        if(user.getCreatedAt() == null){
            user.setCreatedAt(LocalDateTime.now());
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "userId" ));
    }
    public User getUserById(long userId){
        return userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this id does not exist"));
    }

    @Transactional
    public void deleteUserById(long userId){

        Optional<User> userExists = userRepository.findById(userId);
        if(userExists.isPresent()){
            userRepository.deleteById(userId);
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id: " + userId + " doesn't exist");
        }
    }

}
