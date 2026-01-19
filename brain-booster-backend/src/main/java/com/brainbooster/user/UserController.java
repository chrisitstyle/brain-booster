package com.brainbooster.user;


import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> addUser(@RequestBody User user) {
        UserDTO savedUser = userService.addUser(user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.userId())
                .toUri();

        return ResponseEntity.created(location).body(savedUser);
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDTO getUserById(@PathVariable long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/{userId}/flashcard-sets")
    public List<FlashcardSetDTO> getAllFlashcardSetsByUserId(@PathVariable long userId) {
        return userService.getAllFlashcardSetsByUserId(userId);

    }

    @PutMapping("/{userId}")
    public UserDTO updateUser(@RequestBody User updatedUser, @PathVariable long userId) {
        return userService.updateUser(updatedUser, userId);

    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String deleteUserById(@PathVariable Long userId) {

        userService.deleteUserById(userId);
        return "User with id: " + userId + " has been deleted";

    }
}