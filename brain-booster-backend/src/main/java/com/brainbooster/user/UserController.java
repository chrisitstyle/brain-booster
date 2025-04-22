package com.brainbooster.user;


import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.flashcardset.FlashcardSetDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

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

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMessage());
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(RuntimeException ex) {
        return ex.getMessage();
    }

}