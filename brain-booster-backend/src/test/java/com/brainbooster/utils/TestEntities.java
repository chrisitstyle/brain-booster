package com.brainbooster.utils;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.UserDTO;

import java.time.LocalDateTime;

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
                .setId(1L)
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

    //flashcardset methods

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
                .createdAt(LocalDateTime.of(2026, 1, 19, 23, 0));

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
                createUserDTO(),
                "test_flashcardset_name",
                "test_flashcardset_description",
                LocalDateTime.of(2026, 1, 19, 23, 0)
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
}
