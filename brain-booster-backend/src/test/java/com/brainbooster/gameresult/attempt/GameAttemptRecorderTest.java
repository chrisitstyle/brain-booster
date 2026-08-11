package com.brainbooster.gameresult.attempt;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.gameresult.GameMode;
import com.brainbooster.gameresult.dto.SaveGameResultRequest;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.brainbooster.utils.TestEntities.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameAttemptRecorderTest {

    @Mock
    private GameAttemptRepository gameAttemptRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    private GameAttemptRecorder gameAttemptRecorder;

    @BeforeEach
    void setUp() {
        gameAttemptRecorder = new GameAttemptRecorder(
                gameAttemptRepository,
                flashcardRepository);
    }

    @Test
    void shouldCreateGameAttemptWithoutQuestionResults() {
        User user = createUser(2L, Role.USER);
        FlashcardSet flashcardSet =
                createFlashcardSet(11L, user);

        SaveGameResultRequest request = createSaveGameResultRequest(
                11L,
                GameMode.MATCHING,
                8,
                10,
                120
        );

        Instant completedAt = Instant.parse("2026-08-11T10:00:00Z");

        gameAttemptRecorder.recordAttempt(
                user,
                flashcardSet,
                request,
                completedAt);

        ArgumentCaptor<GameAttempt> attemptCaptor = ArgumentCaptor.forClass(GameAttempt.class);

        verify(gameAttemptRepository)
                .save(attemptCaptor.capture());

        GameAttempt savedAttempt = attemptCaptor.getValue();

        assertThat(savedAttempt.getUser())
                .isEqualTo(user);

        assertThat(savedAttempt.getSet())
                .isEqualTo(flashcardSet);

        assertThat(savedAttempt.getMode())
                .isEqualTo(GameMode.MATCHING);

        assertThat(savedAttempt.getScore())
                .isEqualTo(8);

        assertThat(savedAttempt.getTotalQuestions())
                .isEqualTo(10);

        assertThat(savedAttempt.getDurationSeconds())
                .isEqualTo(120);

        assertThat(savedAttempt.getCompletedAt())
                .isEqualTo(completedAt);

        assertThat(savedAttempt.getQuestionResults())
                .isEmpty();

        verifyNoInteractions(flashcardRepository);
    }

    @Test
    void shouldCreateGameAttemptWithQuestionResults() {
        User user = createUser(2L, Role.USER);
        FlashcardSet flashcardSet =
                createFlashcardSet(11L, user);

        Flashcard flashcard = createFlashcard(
                        25L,
                        flashcardSet,
                        "cat",
                        "kot");

        SaveGameResultRequest request = createMultipleChoiceGameResultRequestWithQuestionResult(
                        11L,
                        25L);

        var expectedQuestionResult =
                request.questionResults().getFirst();

        Instant completedAt = Instant.parse("2026-08-11T10:00:00Z");

        when(flashcardRepository.findAllById(Set.of(25L)))
                .thenReturn(List.of(flashcard));

        gameAttemptRecorder.recordAttempt(
                user,
                flashcardSet,
                request,
                completedAt);

        ArgumentCaptor<GameAttempt> attemptCaptor = ArgumentCaptor.forClass(GameAttempt.class);

        verify(gameAttemptRepository).save(attemptCaptor.capture());

        GameAttempt savedAttempt = attemptCaptor.getValue();

        assertThat(savedAttempt.getUser())
                .isEqualTo(user);

        assertThat(savedAttempt.getSet())
                .isEqualTo(flashcardSet);

        assertThat(savedAttempt.getMode())
                .isEqualTo(request.mode());

        assertThat(savedAttempt.getScore())
                .isEqualTo(request.score());

        assertThat(savedAttempt.getTotalQuestions())
                .isEqualTo(request.totalQuestions());

        assertThat(savedAttempt.getDurationSeconds())
                .isEqualTo(request.durationSeconds());

        assertThat(savedAttempt.getCompletedAt())
                .isEqualTo(completedAt);

        assertThat(savedAttempt.getQuestionResults())
                .hasSize(1);

        GameQuestionResult savedQuestionResult = savedAttempt.getQuestionResults().getFirst();

        assertThat(savedQuestionResult.getAttempt())
                .isSameAs(savedAttempt);

        assertThat(savedQuestionResult.getFlashcard())
                .isEqualTo(flashcard);

        assertThat(savedQuestionResult.getQuestionKey())
                .isEqualTo(expectedQuestionResult.questionKey());

        assertThat(savedQuestionResult.getQuestionOrder())
                .isEqualTo(expectedQuestionResult.questionOrder());

        assertThat(savedQuestionResult.getQuestionType())
                .isEqualTo(expectedQuestionResult.questionType());

        assertThat(savedQuestionResult.getAnswerWith())
                .isEqualTo(expectedQuestionResult.answerWith());

        assertThat(savedQuestionResult.getPrompt())
                .isEqualTo(expectedQuestionResult.prompt());

        assertThat(savedQuestionResult.getUserAnswer())
                .isEqualTo(expectedQuestionResult.userAnswer());

        assertThat(savedQuestionResult.getCorrectAnswer())
                .isEqualTo(expectedQuestionResult.correctAnswer());

        assertThat(savedQuestionResult.getWasCorrect())
                .isEqualTo(expectedQuestionResult.wasCorrect());

        assertThat(savedQuestionResult.getMistakesCount())
                .isEqualTo(expectedQuestionResult.mistakesCount());

        assertThat(savedQuestionResult.getAnsweredAt())
                .isEqualTo(completedAt);
    }

    @Test
    void shouldThrowExceptionWhenQuestionResultFlashcardDoesNotExist() {
        User user = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, user);

        SaveGameResultRequest request = createWrittenGameResultRequestWithWrongQuestionResult(
                        11L,
                        99L);

        Instant completedAt = Instant.parse("2026-08-11T10:00:00Z");

        when(flashcardRepository.findAllById(Set.of(99L)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> gameAttemptRecorder.recordAttempt(
                        user,
                        flashcardSet,
                        request,
                        completedAt)
        )
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage(
                        "Flashcard with id: 99 not found"
                );

        verify(gameAttemptRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenQuestionResultFlashcardDoesNotBelongToSet() {
        User user = createUser(2L, Role.USER);

        FlashcardSet selectedSet = createFlashcardSet(11L, user);

        FlashcardSet otherSet = createFlashcardSet(99L, user);

        Flashcard flashcard = createFlashcard(
                        25L,
                        otherSet,
                        "cat",
                        "kot");

        SaveGameResultRequest request = createMultipleChoiceGameResultRequestWithQuestionResult(
                        11L,
                        25L);

        Instant completedAt = Instant.parse("2026-08-11T10:00:00Z");

        when(flashcardRepository.findAllById(Set.of(25L)))
                .thenReturn(List.of(flashcard));

        assertThatThrownBy(() -> gameAttemptRecorder.recordAttempt(
                        user,
                        selectedSet,
                        request,
                        completedAt)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Flashcard with id: 25 does not belong to set: 11"
                );

        verify(gameAttemptRepository, never()).save(any());
    }
}
