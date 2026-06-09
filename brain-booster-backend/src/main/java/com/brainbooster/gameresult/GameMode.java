package com.brainbooster.gameresult;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

@Schema(
        description = "Supported game modes.",
        allowableValues = {
                "multiple-choice",
                "written",
                "matching",
                "custom-test"
        }
)
public enum GameMode {
    MULTIPLE_CHOICE("multiple-choice"),
    WRITTEN("written"),
    MATCHING("matching"),
    CUSTOM_TEST("custom-test");

    private final String value;

    GameMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static GameMode fromValue(String value) {
        return Arrays.stream(values())
                .filter(mode -> mode.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown game mode: " + value));
    }
}
