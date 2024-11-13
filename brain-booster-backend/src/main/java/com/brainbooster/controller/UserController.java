package com.brainbooster.controller;


import com.brainbooster.dto.FlashcardSetDTO;
import com.brainbooster.model.User;
import com.brainbooster.dto.UserDTO;
import com.brainbooster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> addUser(@RequestBody User user) {
        UserDTO savedUser = userService.addUser(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        UserDTO user = userService.getUserById(userId);

        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{userId}/flashcardsets")
    public ResponseEntity<List<FlashcardSetDTO>> getAllFlashcardSetsByUserId(@PathVariable Long userId) {
        List<FlashcardSetDTO> flashcardSets = userService.getAllFlashcardSetsByUserId(userId);
        return ResponseEntity.ok(flashcardSets);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@RequestBody User updatedUser, @PathVariable Long userId) {
        UserDTO responseUser = userService.updateUser(updatedUser, userId);

        return new ResponseEntity<>(responseUser, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long userId) {

        userService.deleteUserById(userId);
        return new ResponseEntity<>("User with id: " + userId + " has been deleted", HttpStatus.OK);

    }

}