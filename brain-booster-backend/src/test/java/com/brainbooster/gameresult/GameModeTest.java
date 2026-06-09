package com.brainbooster.gameresult;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameModeTest {

    @Test
    void shouldCreateGameModeFromJsonValue() {
        assertThat(GameMode.fromValue("multiple-choice"))
                .isEqualTo(GameMode.MULTIPLE_CHOICE);

        assertThat(GameMode.fromValue("written"))
                .isEqualTo(GameMode.WRITTEN);

        assertThat(GameMode.fromValue("matching"))
                .isEqualTo(GameMode.MATCHING);

        assertThat(GameMode.fromValue("custom-test"))
                .isEqualTo(GameMode.CUSTOM_TEST);
    }

    @Test
    void shouldCreateGameModeIgnoringCase() {
        assertThat(GameMode.fromValue("MULTIPLE-CHOICE"))
                .isEqualTo(GameMode.MULTIPLE_CHOICE);
    }

    @Test
    void shouldReturnJsonValue() {
        assertThat(GameMode.MULTIPLE_CHOICE.getValue())
                .isEqualTo("multiple-choice");

        assertThat(GameMode.WRITTEN.getValue())
                .isEqualTo("written");

        assertThat(GameMode.MATCHING.getValue())
                .isEqualTo("matching");

        assertThat(GameMode.CUSTOM_TEST.getValue())
                .isEqualTo("custom-test");
    }

    @Test
    void shouldThrowExceptionForUnknownGameMode() {
        assertThatThrownBy(() -> GameMode.fromValue("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown game mode: unknown");
    }
}
