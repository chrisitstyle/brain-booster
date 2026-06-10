package com.brainbooster.gameresult;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

@Schema(
        description = "Supported question types inside game attempts.",
        allowableValues = {
                "multiple-choice",
                "written",
                "matching",
                "true-false"
        }
)
public enum GameQuestionType {
    MULTIPLE_CHOICE("multiple-choice"),
    WRITTEN("written"),
    MATCHING("matching"),
    TRUE_FALSE("true-false");

    private final String value;

    GameQuestionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static GameQuestionType fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown game question type: " + value));
    }
}
