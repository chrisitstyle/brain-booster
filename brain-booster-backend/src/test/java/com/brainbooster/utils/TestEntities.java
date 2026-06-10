package com.brainbooster.utils;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.dto.FlashcardCreationDTO;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.dto.FlashcardUpdateDTO;
import com.brainbooster.flashcard.starred.UserStarredFlashcard;
import com.brainbooster.flashcard.starred.UserStarredFlashcardId;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetUpdateDTO;
import com.brainbooster.folder.Folder;
import com.brainbooster.folder.dto.FlashcardSetInFolderDTO;
import com.brainbooster.folder.dto.FolderCreationDTO;
import com.brainbooster.folder.dto.FolderDTO;
import com.brainbooster.folder.dto.FolderUpdateDTO;
import com.brainbooster.gameresult.GameMode;
import com.brainbooster.gameresult.GameResult;
import com.brainbooster.gameresult.dto.GameResultDTO;
import com.brainbooster.gameresult.dto.SaveGameResultRequest;
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
     * Creates a fully instantiated {@link User} entity with custom ID and role.
     */
    public static User createUser(Long userId, Role role) {
        return userBuilder()
                .userId(userId)
                .nickname("user" + userId)
                .email("user" + userId + "@example.com")
                .role(role)
                .build();
    }

    /**
     * Creates an admin user entity.
     */
    public static User createAdminUser() {
        return createUser(2L, Role.ADMIN);
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
        return new UserSummaryDTO(
                "johndoe",
                LocalDateTime.of(2026, 1, 19, 23, 0)
        );
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

    /**
     * Creates a {@link FlashcardCreationDTO} used for testing flashcard creation requests.
     */
    public static FlashcardCreationDTO createFlashcardCreationDTO() {
        return new FlashcardCreationDTO(
                1L,
                "test_term",
                "test_definition"
        );
    }

    /**
     * Creates a {@link FlashcardUpdateDTO} used for testing flashcard update requests.
     */
    public static FlashcardUpdateDTO createFlashcardUpdateDTO() {
        return new FlashcardUpdateDTO(
                "updated_term",
                "updated_definition"
        );
    }

    /**
     * Creates a {@link FlashcardDTO} with default data and starred=false.
     */
    public static FlashcardDTO createFlashcardDTO() {
        return createFlashcardDTO(false);
    }

    /**
     * Creates a {@link FlashcardDTO} with default data and custom starred status.
     */
    public static FlashcardDTO createFlashcardDTO(boolean starred) {
        return new FlashcardDTO(
                1L,
                1L,
                "test_term",
                "test_definition",
                starred
        );
    }

    /**
     * Creates a {@link FlashcardDTO} representing an updated flashcard.
     */
    public static FlashcardDTO createUpdatedFlashcardDTO() {
        return new FlashcardDTO(
                1L,
                1L,
                "updated_term",
                "updated_definition",
                false
        );
    }

    /**
     * Creates a {@link UserStarredFlashcard} relation between a user and flashcard.
     */
    public static UserStarredFlashcard createUserStarredFlashcard(
            User user,
            Flashcard flashcard
    ) {
        return UserStarredFlashcard.builder()
                .id(new UserStarredFlashcardId(
                        user.getUserId(),
                        flashcard.getFlashcardId()
                ))
                .user(user)
                .flashcard(flashcard)
                .createdAt(LocalDateTime.of(2026, 1, 19, 23, 0))
                .build();
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
     * Creates a fully instantiated {@link FlashcardSet} entity with custom ID.
     *
     * @param setId ID of the flashcard set.
     * @return a FlashcardSet entity with the provided ID.
     */
    public static FlashcardSet createFlashcardSet(Long setId) {
        return flashcardSetBuilder()
                .setId(setId)
                .build();
    }

    /**
     * Creates a fully instantiated {@link FlashcardSet} entity with custom ID and owner.
     *
     * @param setId ID of the flashcard set.
     * @param user  owner of the flashcard set.
     * @return a FlashcardSet entity with the provided ID and owner.
     */
    public static FlashcardSet createFlashcardSet(Long setId, User user) {
        return flashcardSetBuilder()
                .setId(setId)
                .user(user)
                .build();
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
                "test_flashcardset_description"
        );
    }

    /**
     * Creates a {@link FlashcardSetUpdateDTO} used for testing update (PATCH/PUT) requests.
     * <p>
     * Contains the fields that are allowed to be updated.
     *
     * @return a FlashcardSetUpdateDTO object.
     */
    public static FlashcardSetUpdateDTO createFlashcardSetUpdateDTO() {
        return new FlashcardSetUpdateDTO(
                "Updated Set",
                "Updated description"
        );
    }

    // GAME RESULT METHODS

    /**
     * Creates a {@link GameResult.GameResultBuilder} pre-configured with default test data.
     * <p>
     * Defaults: ID=1L, user=default User, set=default FlashcardSet,
     * mode=MULTIPLE_CHOICE, score=8, totalQuestions=10, durationSeconds=null.
     *
     * @return a builder instance with default game result data.
     */
    public static GameResult.GameResultBuilder gameResultBuilder() {
        return GameResult.builder()
                .resultId(1L)
                .user(createUser())
                .set(createFlashcardSet())
                .mode(GameMode.MULTIPLE_CHOICE)
                .score(8)
                .totalQuestions(10)
                .durationSeconds(null)
                .completedAt(LocalDateTime.of(2026, 1, 19, 23, 0));
    }

    /**
     * Creates a fully instantiated {@link GameResult} entity with default test data.
     *
     * @return a GameResult entity.
     * @see #gameResultBuilder()
     */
    public static GameResult createGameResult() {
        return gameResultBuilder().build();
    }

    /**
     * Creates a fully instantiated {@link GameResult} entity for the given user,
     * flashcard set, and game mode.
     *
     * @param user         owner of the game result.
     * @param flashcardSet flashcard set related to the result.
     * @param mode         game mode related to the result.
     * @return a GameResult entity.
     */
    public static GameResult createGameResult(
            User user,
            FlashcardSet flashcardSet,
            GameMode mode
    ) {
        return gameResultBuilder()
                .user(user)
                .set(flashcardSet)
                .mode(mode)
                .build();
    }

    /**
     * Creates a {@link SaveGameResultRequest} with default test data.
     * <p>
     * Defaults: setId=1L, mode=MULTIPLE_CHOICE, score=8, totalQuestions=10,
     * durationSeconds=null.
     *
     * @return a SaveGameResultRequest object.
     */
    public static SaveGameResultRequest createSaveGameResultRequest() {
        return new SaveGameResultRequest(
                1L,
                GameMode.MULTIPLE_CHOICE,
                8,
                10,
                null
        );
    }

    /**
     * Creates a {@link SaveGameResultRequest} with custom test data.
     *
     * @param setId           ID of the flashcard set.
     * @param mode            game mode.
     * @param score           achieved score.
     * @param totalQuestions  total number of questions.
     * @param durationSeconds optional duration in seconds.
     * @return a SaveGameResultRequest object.
     */
    public static SaveGameResultRequest createSaveGameResultRequest(
            Long setId,
            GameMode mode,
            Integer score,
            Integer totalQuestions,
            Integer durationSeconds
    ) {
        return new SaveGameResultRequest(
                setId,
                mode,
                score,
                totalQuestions,
                durationSeconds
        );
    }

    /**
     * Creates a {@link GameResultDTO} with default test data.
     *
     * @return a GameResultDTO object.
     */
    public static GameResultDTO createGameResultDTO() {
        return new GameResultDTO(
                1L,
                1L,
                1L,
                GameMode.MULTIPLE_CHOICE,
                8,
                10,
                null,
                LocalDateTime.of(2026, 1, 19, 23, 0)
        );
    }

    /**
     * Creates a {@link GameResultDTO} from a {@link GameResult} entity.
     *
     * @param gameResult source entity.
     * @return a GameResultDTO object containing data from the entity.
     */
    public static GameResultDTO createGameResultDTO(GameResult gameResult) {
        return new GameResultDTO(
                gameResult.getResultId(),
                gameResult.getUser().getUserId(),
                gameResult.getSet().getSetId(),
                gameResult.getMode(),
                gameResult.getScore(),
                gameResult.getTotalQuestions(),
                gameResult.getDurationSeconds(),
                gameResult.getCompletedAt()
        );
    }

    /**
     * Creates a {@link GameResultDTO} with custom test data.
     *
     * @param resultId        ID of the game result.
     * @param userId          ID of the result owner.
     * @param setId           ID of the related flashcard set.
     * @param mode            game mode.
     * @param score           achieved score.
     * @param totalQuestions  total number of questions.
     * @param durationSeconds optional duration in seconds.
     * @return a GameResultDTO object.
     */
    public static GameResultDTO createGameResultDTO(
            Long resultId,
            Long userId,
            Long setId,
            GameMode mode,
            Integer score,
            Integer totalQuestions,
            Integer durationSeconds
    ) {
        return new GameResultDTO(
                resultId,
                userId,
                setId,
                mode,
                score,
                totalQuestions,
                durationSeconds,
                LocalDateTime.of(2026, 1, 19, 23, 0)
        );
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