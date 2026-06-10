package com.brainbooster.gameresult;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

@Schema(
        description = "Side of a flashcard that was expected as the answer.",
        allowableValues = {
                "term",
                "definition"
        }
)
public enum QuestionAnswerSide {
    TERM("term"),
    DEFINITION("definition");

    private final String value;

    QuestionAnswerSide(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static QuestionAnswerSide fromValue(String value) {
        return Arrays.stream(values())
                .filter(side -> side.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown question answer side: " + value));
    }
}
