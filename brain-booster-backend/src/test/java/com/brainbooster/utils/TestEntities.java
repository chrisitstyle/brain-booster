package com.brainbooster.utils;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.folder.Folder;
import com.brainbooster.folder.dto.FlashcardSetInFolderDTO;
import com.brainbooster.folder.dto.FolderCreationDTO;
import com.brainbooster.folder.dto.FolderDTO;
import com.brainbooster.folder.dto.FolderUpdateDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetUpdateDTO;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.dto.UserCreationDTO;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.user.dto.UserSummaryDTO;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

/**
 * Utility class for creating entity and DTO instances for testing purposes.
 * <p>
 * This class acts as an "Object Mother" or "Test Data Factory", providing
 * pre-configured objects with valid default data to avoid code duplication
 * in unit and integration tests.
 */
public class TestEntities {


    /**
     * Private constructor to block instantiation of utility class.
     */
    private TestEntities() {
    }

    // USER METHODS

    /**
     * Creates a {@link User.UserBuilder} pre-configured with default test data.
     * <p>
     * Defaults: ID=1L, nickname="johndoe", email="johndoe@example.com", role=USER.
     * Use this method if you need to modify specific fields before building the object.
     *
     * @return a builder instance with default user data.
     */
    public static User.UserBuilder userBuilder() {
        return User.builder()
                .userId(1L)
                .nickname("johndoe")
                .email("johndoe@example.com")
                .password("test_password")
                .role(Role.USER);
    }

    /**
     * Creates a fully instantiated {@link User} entity with default test data.
     *
     * @return a User entity.
     * @see #userBuilder()
     */
    public static User createUser() {
        return userBuilder().build();
    }

    /**
     * Creates a {@link UserDTO} with data corresponding to the default User entity.
     *
     * @return a UserDTO object.
     */
    public static UserDTO createUserDTO() {

        return new UserDTO(
                1L,
                "johndoe",
                "johndoe@example.com",
                Role.USER,
                LocalDateTime.of(2026, 1, 19, 23, 0)

        );
    }

    /**
     * Creates a {@link UserSummaryDTO} for nested flashcard set responses.
     */
    public static UserSummaryDTO createUserSummaryDTO() {
        return new UserSummaryDTO("johndoe",
                LocalDateTime.of(2026, 1, 19, 23, 0));
    }

    /**
     * Creates a {@link UserCreationDTO} used for testing user creation requests.
     * <p>
     * Includes a valid password to satisfy @Valid constraints in controllers.
     *
     * @return a UserCreationDTO object.
     */
    public static UserCreationDTO createUserCreationDTO() {
        return new UserCreationDTO(
                "johndoe",
                "johndoe@example.com",
                "test_password",
                Role.USER
        );
    }

    // FLASHCARD METHODS

    /**
     * Creates a {@link Flashcard.FlashcardBuilder} pre-configured with default test data.
     * <p>
     * Defaults: ID=1L, term="test_term", definition="test_definition".
     *
     * @return a builder instance with default flashcard data.
     */
    public static Flashcard.FlashcardBuilder flashcardBuilder() {
        return Flashcard.builder()
                .flashcardId(1L)
                .flashcardSet(createFlashcardSet())
                .term("test_term")
                .definition("test_definition");


    }

    /**
     * Creates a fully instantiated {@link Flashcard} entity with default test data.
     *
     * @return a Flashcard entity.
     * @see #flashcardBuilder()
     */
    public static Flashcard createFlashcard() {
        return flashcardBuilder().build();
    }

    public static FlashcardDTO createFlashcardDTO() {
        return new FlashcardDTO(1L, 1L, "test_term", "test_definition");
    }

    // FLASHCARD SET METHODS

    /**
     * Creates a {@link FlashcardSet.FlashcardSetBuilder} pre-configured with default test data.
     * <p>
     * Defaults: ID=1L, Name="test_flashcardset_name", associated with the default User.
     *
     * @return a builder instance with default flashcard set data.
     */
    public static FlashcardSet.FlashcardSetBuilder flashcardSetBuilder() {
        return FlashcardSet.builder()
                .setId(1L)
                .user(userBuilder().build())
                .setName("test_flashcardset_name")
                .description("test_flashcardset_description")
                .createdAt(LocalDateTime.of(2026, 1, 19, 23, 0))
                .termCount(0L);

    }

    /**
     * Creates a fully instantiated {@link FlashcardSet} entity with default test data.
     *
     * @return a FlashcardSet entity.
     * @see #flashcardSetBuilder()
     */
    public static FlashcardSet createFlashcardSet() {
        return flashcardSetBuilder().build();
    }

    /**
     * Creates a {@link FlashcardSetDTO} with data corresponding to the default FlashcardSet entity.
     * <p>
     * Includes the full UserDTO inside.
     *
     * @return a FlashcardSetDTO object.
     */
    public static FlashcardSetDTO createFlashcardSetDTO() {
        return new FlashcardSetDTO(
                1L,
                createUserSummaryDTO(),
                "test_flashcardset_name",
                "test_flashcardset_description",
                LocalDateTime.of(2026, 1, 19, 23, 0),
                0L
        );
    }

    /**
     * Creates a {@link FlashcardSetCreationDTO} used for testing creation requests.
     * <p>
     * Contains only the fields required to create a new set (userId, name, description).
     *
     * @return a FlashcardSetCreationDTO object.
     */
    public static FlashcardSetCreationDTO createFlashcardSetCreationDTO() {
        return new FlashcardSetCreationDTO(
                1L,
                "test_flashcardset_name",
                "test_flashcardset_description");
    }


    /**
     * Creates a {@link FlashcardSetUpdateDTO} used for testing update (PATCH/PUT) requests.
     * <p>
     * Contains the fields that are allowed to be updated.
     *
     * @return a FlashcardSetUpdateDTO object.
     */
    public static FlashcardSetUpdateDTO createFlashcardSetUpdateDTO() {
        return new FlashcardSetUpdateDTO("Updated Set", "Updated description");
    }

    // FOLDER METHODS

    /**
     * Creates a {@link Folder.FolderBuilder} pre-configured with default test data.
     */
    public static Folder.FolderBuilder folderBuilder() {
        return Folder.builder()
                .folderId(1L)
                .user(createUser())
                .name("test_folder_name")
                .description("test_folder_description")
                .createdAt(LocalDateTime.of(2026, 1, 19, 23, 0))
                .flashcardSets(new HashSet<>())
                .setCount(0L);
    }

    /**
     * Creates a fully instantiated {@link Folder} entity with default test data.
     */
    public static Folder createFolder() {
        return folderBuilder().build();
    }

    /**
     * Creates a folder entity containing one flashcard set.
     */
    public static Folder createFolderWithFlashcardSet() {
        Folder folder = folderBuilder()
                .setCount(1L)
                .build();

        folder.getFlashcardSets().add(createFlashcardSet());

        return folder;
    }

    /**
     * Creates a nested DTO representing a flashcard set inside a folder.
     */
    public static FlashcardSetInFolderDTO createFlashcardSetInFolderDTO() {
        return new FlashcardSetInFolderDTO(
                1L,
                "test_flashcardset_name",
                0L
        );
    }

    /**
     * Creates a {@link FolderDTO} with one nested flashcard set.
     */
    public static FolderDTO createFolderDTO() {
        return new FolderDTO(
                1L,
                "johndoe",
                "test_folder_name",
                "test_folder_description",
                1L,
                List.of(createFlashcardSetInFolderDTO())
        );
    }

    /**
     * Creates a {@link FolderDTO} without nested flashcard sets.
     */
    public static FolderDTO createEmptyFolderDTO() {
        return new FolderDTO(
                1L,
                "johndoe",
                "test_folder_name",
                "test_folder_description",
                0L,
                List.of()
        );
    }

    /**
     * Creates a {@link FolderCreationDTO} used for testing folder creation requests.
     */
    public static FolderCreationDTO createFolderCreationDTO() {
        return new FolderCreationDTO(
                "test_folder_name",
                "test_folder_description"
        );
    }

    /**
     * Creates a {@link FolderUpdateDTO} used for testing folder update requests.
     */
    public static FolderUpdateDTO createFolderUpdateDTO() {
        return new FolderUpdateDTO(
                "Updated Folder",
                "Updated folder description"
        );
    }

}
