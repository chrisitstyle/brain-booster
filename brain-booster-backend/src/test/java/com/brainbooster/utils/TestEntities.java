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
import com.brainbooster.gameresult.GameQuestionType;
import com.brainbooster.gameresult.GameResult;
import com.brainbooster.gameresult.QuestionAnswerSide;
import com.brainbooster.gameresult.attempt.GameAttempt;
import com.brainbooster.gameresult.dto.*;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.dto.UserCreationDTO;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.user.dto.UserSummaryDTO;

import java.time.Instant;
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

    private static final Instant TEST_INSTANT_DATE =
            Instant.parse("2026-01-19T23:00:00Z");

    /**
     * Private constructor to block instantiation of utility class.
     */
    private TestEntities() {
    }

    // USER METHODS

    /**
     * Creates a {@link User.UserBuilder} pre-configured with default test data.
     * <p>
     * Defaults: ID=1L, nickname="johndoe", email=""johndoe@example.com", role=USER.
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
                .role(Role.USER)
                .createdAt(TEST_INSTANT_DATE);
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
     *
     * @param userId ID of the user.
     * @param role   role of the user.
     * @return a User entity with the provided ID and role.
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
     *
     * @return an admin User entity.
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
                TEST_INSTANT_DATE
        );
    }

    /**
     * Creates a {@link UserSummaryDTO} for nested flashcard set responses.
     *
     * @return a UserSummaryDTO object.
     */
    public static UserSummaryDTO createUserSummaryDTO() {
        return new UserSummaryDTO(
                "johndoe",
                TEST_INSTANT_DATE
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
     * Creates a fully instantiated {@link Flashcard} entity with custom ID,
     * flashcard set, term, and definition.
     *
     * @param flashcardId  ID of the flashcard.
     * @param flashcardSet flashcard set containing this flashcard.
     * @param term         term value.
     * @param definition   definition value.
     * @return a Flashcard entity with the provided data.
     */
    public static Flashcard createFlashcard(
            Long flashcardId,
            FlashcardSet flashcardSet,
            String term,
            String definition
    ) {
        return flashcardBuilder()
                .flashcardId(flashcardId)
                .flashcardSet(flashcardSet)
                .term(term)
                .definition(definition)
                .build();
    }

    /**
     * Creates a {@link FlashcardCreationDTO} used for testing flashcard creation requests.
     *
     * @return a FlashcardCreationDTO object.
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
     *
     * @return a FlashcardUpdateDTO object.
     */
    public static FlashcardUpdateDTO createFlashcardUpdateDTO() {
        return new FlashcardUpdateDTO(
                "updated_term",
                "updated_definition"
        );
    }

    /**
     * Creates a {@link FlashcardDTO} with default data and starred=false.
     *
     * @return a FlashcardDTO object.
     */
    public static FlashcardDTO createFlashcardDTO() {
        return createFlashcardDTO(false);
    }

    /**
     * Creates a {@link FlashcardDTO} with default data and custom starred status.
     *
     * @param starred whether the flashcard is starred by the user.
     * @return a FlashcardDTO object.
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
     *
     * @return a FlashcardDTO object representing an updated flashcard.
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
     *
     * @param user      user who starred the flashcard.
     * @param flashcard starred flashcard.
     * @return a UserStarredFlashcard relation.
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
                .createdAt(TEST_INSTANT_DATE)
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
                .createdAt(TEST_INSTANT_DATE)
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
                TEST_INSTANT_DATE,
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
                .completedAt(TEST_INSTANT_DATE);
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
     * Creates a {@link SaveGameResultRequest} with custom test data and
     * question-level results.
     *
     * @param setId           ID of the flashcard set.
     * @param mode            game mode.
     * @param score           achieved score.
     * @param totalQuestions  total number of questions.
     * @param durationSeconds optional duration in seconds.
     * @param questionResults question-level results.
     * @return a SaveGameResultRequest object.
     */
    public static SaveGameResultRequest createSaveGameResultRequest(
            Long setId,
            GameMode mode,
            Integer score,
            Integer totalQuestions,
            Integer durationSeconds,
            List<SaveGameQuestionResultRequest> questionResults
    ) {
        return new SaveGameResultRequest(
                setId,
                mode,
                score,
                totalQuestions,
                durationSeconds,
                questionResults
        );
    }

    /**
     * Creates a {@link SaveGameQuestionResultRequest} with default test data.
     *
     * @return a SaveGameQuestionResultRequest object.
     */
    public static SaveGameQuestionResultRequest createSaveGameQuestionResultRequest() {
        return new SaveGameQuestionResultRequest(
                1L,
                "multiple-choice-1-0",
                0,
                GameQuestionType.MULTIPLE_CHOICE,
                QuestionAnswerSide.DEFINITION,
                "test_term",
                "test_definition",
                "test_definition",
                true,
                0
        );
    }

    /**
     * Creates a {@link SaveGameQuestionResultRequest} with custom test data.
     *
     * @param flashcardId   ID of the flashcard used in the question.
     * @param questionKey   client-side question key.
     * @param questionOrder order of the question inside the attempt.
     * @param questionType  type of question.
     * @param answerWith    expected answer side.
     * @param prompt        prompt shown to the user.
     * @param userAnswer    answer provided by the user.
     * @param correctAnswer correct answer.
     * @param wasCorrect    whether the user answered correctly.
     * @param mistakesCount number of mistakes made.
     * @return a SaveGameQuestionResultRequest object.
     */
    public static SaveGameQuestionResultRequest createSaveGameQuestionResultRequest(
            Long flashcardId,
            String questionKey,
            Integer questionOrder,
            GameQuestionType questionType,
            QuestionAnswerSide answerWith,
            String prompt,
            String userAnswer,
            String correctAnswer,
            Boolean wasCorrect,
            Integer mistakesCount
    ) {
        return new SaveGameQuestionResultRequest(
                flashcardId,
                questionKey,
                questionOrder,
                questionType,
                answerWith,
                prompt,
                userAnswer,
                correctAnswer,
                wasCorrect,
                mistakesCount
        );
    }

    /**
     * Creates a correct multiple-choice question result request.
     *
     * @param flashcardId ID of the flashcard used in the question.
     * @return a SaveGameQuestionResultRequest object.
     */
    public static SaveGameQuestionResultRequest createCorrectMultipleChoiceQuestionResultRequest(
            Long flashcardId
    ) {
        return createSaveGameQuestionResultRequest(
                flashcardId,
                "multiple-choice-" + flashcardId + "-0",
                0,
                GameQuestionType.MULTIPLE_CHOICE,
                QuestionAnswerSide.DEFINITION,
                "cat",
                "kot",
                "kot",
                true,
                0
        );
    }

    /**
     * Creates a wrong written question result request.
     *
     * @param flashcardId ID of the flashcard used in the question.
     * @return a SaveGameQuestionResultRequest object.
     */
    public static SaveGameQuestionResultRequest createWrongWrittenQuestionResultRequest(
            Long flashcardId
    ) {
        return createSaveGameQuestionResultRequest(
                flashcardId,
                "written-" + flashcardId + "-0",
                0,
                GameQuestionType.WRITTEN,
                QuestionAnswerSide.DEFINITION,
                "cat",
                "dog",
                "kot",
                false,
                0
        );
    }

    /**
     * Creates a correct multiple-choice game result request with one question-level result.
     *
     * @param setId       ID of the flashcard set.
     * @param flashcardId ID of the flashcard used in the question.
     * @return a SaveGameResultRequest object.
     */
    public static SaveGameResultRequest createMultipleChoiceGameResultRequestWithQuestionResult(
            Long setId,
            Long flashcardId
    ) {
        return createSaveGameResultRequest(
                setId,
                GameMode.MULTIPLE_CHOICE,
                1,
                1,
                15,
                List.of(createCorrectMultipleChoiceQuestionResultRequest(flashcardId))
        );
    }

    /**
     * Creates a written game result request with one wrong question-level result.
     *
     * @param setId       ID of the flashcard set.
     * @param flashcardId ID of the flashcard used in the question.
     * @return a SaveGameResultRequest object.
     */
    public static SaveGameResultRequest createWrittenGameResultRequestWithWrongQuestionResult(
            Long setId,
            Long flashcardId
    ) {
        return createSaveGameResultRequest(
                setId,
                GameMode.WRITTEN,
                0,
                1,
                20,
                List.of(createWrongWrittenQuestionResultRequest(flashcardId))
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
                TEST_INSTANT_DATE
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
                TEST_INSTANT_DATE
        );
    }

    // GAME ATTEMPT METHODS

    /**
     * Creates a {@link GameAttempt.GameAttemptBuilder} pre-configured with default test data.
     * <p>
     * Defaults: ID=1L, user=default User, set=default FlashcardSet,
     * mode=MULTIPLE_CHOICE, score=8, totalQuestions=10, durationSeconds=120.
     *
     * @return a builder instance with default game attempt data.
     */
    public static GameAttempt.GameAttemptBuilder gameAttemptBuilder() {
        return GameAttempt.builder()
                .attemptId(1L)
                .user(createUser())
                .set(createFlashcardSet())
                .mode(GameMode.MULTIPLE_CHOICE)
                .score(8)
                .totalQuestions(10)
                .durationSeconds(120)
                .completedAt(TEST_INSTANT_DATE);
    }

    /**
     * Creates a fully instantiated {@link GameAttempt} entity with default test data.
     *
     * @return a GameAttempt entity.
     * @see #gameAttemptBuilder()
     */
    public static GameAttempt createGameAttempt() {
        return gameAttemptBuilder().build();
    }

    /**
     * Creates a fully instantiated {@link GameAttempt} entity for the given user,
     * flashcard set, and game mode.
     *
     * @param user         owner of the attempt.
     * @param flashcardSet flashcard set related to the attempt.
     * @param mode         game mode related to the attempt.
     * @return a GameAttempt entity.
     */
    public static GameAttempt createGameAttempt(
            User user,
            FlashcardSet flashcardSet,
            GameMode mode
    ) {
        return gameAttemptBuilder()
                .user(user)
                .set(flashcardSet)
                .mode(mode)
                .build();
    }

    /**
     * Creates a {@link GameAttemptDTO} with default test data.
     *
     * @return a GameAttemptDTO object.
     */
    public static GameAttemptDTO createGameAttemptDTO() {
        return new GameAttemptDTO(
                1L,
                1L,
                1L,
                GameMode.MULTIPLE_CHOICE,
                8,
                10,
                120,
                TEST_INSTANT_DATE,
                List.of()
        );
    }

    /**
     * Creates a {@link GameAttemptDTO} from a {@link GameAttempt} entity.
     *
     * @param gameAttempt source entity.
     * @return a GameAttemptDTO object containing data from the entity.
     */
    public static GameAttemptDTO createGameAttemptDTO(GameAttempt gameAttempt) {
        return new GameAttemptDTO(
                gameAttempt.getAttemptId(),
                gameAttempt.getUser().getUserId(),
                gameAttempt.getSet().getSetId(),
                gameAttempt.getMode(),
                gameAttempt.getScore(),
                gameAttempt.getTotalQuestions(),
                gameAttempt.getDurationSeconds(),
                gameAttempt.getCompletedAt(),
                gameAttempt.getQuestionResults().stream()
                        .map(TestEntities::createGameQuestionResultDTO)
                        .toList()
        );
    }

    // GAME QUESTION RESULT METHODS

    /**
     * Creates a {@link GameQuestionResult.GameQuestionResultBuilder}
     * pre-configured with default test data.
     * <p>
     * Defaults: ID=1L, attempt=default GameAttempt, flashcard=default Flashcard,
     * questionType=MULTIPLE_CHOICE, answerWith=DEFINITION, wasCorrect=true.
     *
     * @return a builder instance with default question result data.
     */
    public static GameQuestionResult.GameQuestionResultBuilder gameQuestionResultBuilder() {
        return GameQuestionResult.builder()
                .questionResultId(1L)
                .attempt(createGameAttempt())
                .flashcard(createFlashcard())
                .questionKey("multiple-choice-1-0")
                .questionOrder(0)
                .questionType(GameQuestionType.MULTIPLE_CHOICE)
                .answerWith(QuestionAnswerSide.DEFINITION)
                .prompt("test_term")
                .userAnswer("test_definition")
                .correctAnswer("test_definition")
                .wasCorrect(true)
                .mistakesCount(0)
                .answeredAt(TEST_INSTANT_DATE);
    }

    /**
     * Creates a fully instantiated {@link GameQuestionResult} entity
     * with default test data.
     *
     * @return a GameQuestionResult entity.
     * @see #gameQuestionResultBuilder()
     */
    public static GameQuestionResult createGameQuestionResult() {
        return gameQuestionResultBuilder().build();
    }

    /**
     * Creates a fully instantiated {@link GameQuestionResult} entity for
     * the given game attempt and flashcard.
     *
     * @param gameAttempt related game attempt.
     * @param flashcard   flashcard used in the question.
     * @return a GameQuestionResult entity.
     */
    public static GameQuestionResult createGameQuestionResult(
            GameAttempt gameAttempt,
            Flashcard flashcard
    ) {
        return gameQuestionResultBuilder()
                .attempt(gameAttempt)
                .flashcard(flashcard)
                .build();
    }

    /**
     * Creates a {@link GameQuestionResultDTO} with default test data.
     *
     * @return a GameQuestionResultDTO object.
     */
    public static GameQuestionResultDTO createGameQuestionResultDTO() {
        return new GameQuestionResultDTO(
                1L,
                1L,
                1L,
                "multiple-choice-1-0",
                0,
                GameQuestionType.MULTIPLE_CHOICE,
                QuestionAnswerSide.DEFINITION,
                "test_term",
                "test_definition",
                "test_definition",
                true,
                0,
                TEST_INSTANT_DATE
        );
    }

    /**
     * Creates a {@link GameQuestionResultDTO} from a {@link GameQuestionResult} entity.
     *
     * @param questionResult source entity.
     * @return a GameQuestionResultDTO object containing data from the entity.
     */
    public static GameQuestionResultDTO createGameQuestionResultDTO(
            GameQuestionResult questionResult
    ) {
        return new GameQuestionResultDTO(
                questionResult.getQuestionResultId(),
                questionResult.getAttempt().getAttemptId(),
                questionResult.getFlashcard().getFlashcardId(),
                questionResult.getQuestionKey(),
                questionResult.getQuestionOrder(),
                questionResult.getQuestionType(),
                questionResult.getAnswerWith(),
                questionResult.getPrompt(),
                questionResult.getUserAnswer(),
                questionResult.getCorrectAnswer(),
                questionResult.getWasCorrect(),
                questionResult.getMistakesCount(),
                questionResult.getAnsweredAt()
        );
    }

    // FOLDER METHODS

    /**
     * Creates a {@link Folder.FolderBuilder} pre-configured with default test data.
     *
     * @return a builder instance with default folder data.
     */
    public static Folder.FolderBuilder folderBuilder() {
        return Folder.builder()
                .folderId(1L)
                .user(createUser())
                .name("test_folder_name")
                .description("test_folder_description")
                .createdAt(TEST_INSTANT_DATE)
                .flashcardSets(new HashSet<>())
                .setCount(0L);
    }

    /**
     * Creates a fully instantiated {@link Folder} entity with default test data.
     *
     * @return a Folder entity.
     */
    public static Folder createFolder() {
        return folderBuilder().build();
    }

    /**
     * Creates a folder entity containing one flashcard set.
     *
     * @return a Folder entity with one flashcard set.
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
     *
     * @return a FlashcardSetInFolderDTO object.
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
     *
     * @return a FolderDTO object with one nested flashcard set.
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
     *
     * @return a FolderDTO object without nested flashcard sets.
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
     *
     * @return a FolderCreationDTO object.
     */
    public static FolderCreationDTO createFolderCreationDTO() {
        return new FolderCreationDTO(
                "test_folder_name",
                "test_folder_description"
        );
    }

    /**
     * Creates a {@link FolderUpdateDTO} used for testing folder update requests.
     *
     * @return a FolderUpdateDTO object.
     */
    public static FolderUpdateDTO createFolderUpdateDTO() {
        return new FolderUpdateDTO(
                "Updated Folder",
                "Updated folder description"
        );
    }
}