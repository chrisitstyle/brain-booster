package com.brainbooster.user;


import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.folder.FolderService;
import com.brainbooster.folder.dto.FolderDTO;
import com.brainbooster.user.dto.UserCreationDTO;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.user.dto.UserUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Users", description = "Endpoints for managing users and user-related resources")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final FolderService folderService;

    @Operation(
            summary = "Create a new user",
            description = "Creates a new user account. This endpoint is intended for administrators.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to access this resource")
    @ApiResponse(responseCode = "422", description = "Email already exists")
    @PostMapping
    public ResponseEntity<UserDTO> addUser(@Valid @RequestBody UserCreationDTO userCreationDTO) {
        UserDTO savedUser = userService.addUser(userCreationDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.userId())
                .toUri();

        return ResponseEntity.created(location).body(savedUser);
    }

    @Operation(summary = "Get authenticated user", description = "Returns information about the currently authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Authenticated user fetched successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "404", description = "Authenticated user does not exist")
    @GetMapping("/me")
    public UserDTO getCurrentUser() {
        return userService.getCurrentUser();
    }

    @Operation(
            summary = "Get all users",
            description = "Fetches all users from the database. This endpoint is intended for administrators.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Users fetched successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to access this resource")
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(
            summary = "Get user by ID",
            description = "Fetches a single user by their ID. This endpoint is intended for administrators.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "User fetched successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to access this resource")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{userId}")
    public UserDTO getUserById(
            @Parameter(description = "ID of the user", example = "1")
            @PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @Operation(
            summary = "Get user's flashcard sets",
            description = "Fetches all public flashcard sets created by a specific user."
    )
    @ApiResponse(responseCode = "200", description = "Flashcard sets fetched successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{userId}/flashcard-sets")
    public List<FlashcardSetDTO> getAllFlashcardSetsByUserId(
            @Parameter(description = "ID of the user", example = "1")
            @PathVariable Long userId) {
        return userService.getAllFlashcardSetsByUserId(userId);

    }

    @Operation(
            summary = "Get user's flashcard sets by nickname",
            description = "Fetches all public flashcard sets created by a user with the given nickname."
    )
    @ApiResponse(responseCode = "200", description = "Flashcard sets fetched successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/nickname/{nickname}/flashcard-sets")
    public List<FlashcardSetDTO> getAllFlashcardSetsByNickname(
            @Parameter(description = "Nickname of the user", example = "johndoe")
            @PathVariable String nickname) {
        return userService.getAllFlashcardSetsByUserNickname(nickname);
    }

    @Operation(
            summary = "Get user's folders by nickname",
            description = "Fetches all public folders created by a user with the given nickname."
    )
    @ApiResponse(responseCode = "200", description = "Folders fetched successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{nickname}/folders")
    public List<FolderDTO> getFoldersByNickname(
            @Parameter(description = "Nickname of the user", example = "johndoe")
            @PathVariable String nickname) {

        return folderService.getFoldersByNickname(nickname);
    }

    @Operation(
            summary = "Update user",
            description = "Updates an existing user by ID. This endpoint is intended for administrators.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to update users")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{userId}")
    public UserDTO updateUser(@Valid @RequestBody UserUpdateDTO updatedUser,
                              @Parameter(description = "ID of the user", example = "1")
                              @PathVariable Long userId) {
        return userService.updateUser(updatedUser, userId);

    }

    @Operation(
            summary = "Delete user",
            description = "Deletes an existing user by ID. This endpoint is intended for administrators.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to delete users")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String deleteUserById(
            @Parameter(description = "ID of the user", example = "1")
            @PathVariable Long userId) {

        userService.deleteUserById(userId);
        return "User with id: " + userId + " has been deleted";

    }
}