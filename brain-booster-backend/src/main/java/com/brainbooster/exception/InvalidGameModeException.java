package com.brainbooster.exception;

public class InvalidGameModeException extends RuntimeException {

    public InvalidGameModeException(String mode) {super("Invalid game mode: " + mode);}
}
