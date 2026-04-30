package com.brainbooster.user;


import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.user.dto.UserCreationDTO;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.user.dto.UserNicknameUpdateDTO;
import com.brainbooster.user.dto.UserUpdateDTO;
import jakarta.validation.Valid;
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
    public ResponseEntity<UserDTO> addUser(@Valid @RequestBody UserCreationDTO userCreationDTO) {
        UserDTO savedUser = userService.addUser(userCreationDTO);
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
    public UserDTO getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/{userId}/flashcard-sets")
    public List<FlashcardSetDTO> getAllFlashcardSetsByUserId(@PathVariable Long userId) {
        return userService.getAllFlashcardSetsByUserId(userId);

    }

    @GetMapping("/nickname/{nickname}/flashcard-sets")
    public List<FlashcardSetDTO> getAllFlashcardSetsByNickname(@PathVariable String nickname) {
        return userService.getAllFlashcardSetsByUserNickname(nickname);
    }

    @PatchMapping("/{userId}/nickname")
    public UserDTO updateUserNickname(@Valid @RequestBody UserNicknameUpdateDTO updatedUserNickname,
                                      @PathVariable Long userId) {
        return userService.updateUserNickname(updatedUserNickname, userId);
    }

    @PutMapping("/{userId}")
    public UserDTO updateUser(@Valid @RequestBody UserUpdateDTO updatedUser, @PathVariable Long userId) {
        return userService.updateUser(updatedUser, userId);

    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String deleteUserById(@PathVariable Long userId) {

        userService.deleteUserById(userId);
        return "User with id: " + userId + " has been deleted";

    }
}