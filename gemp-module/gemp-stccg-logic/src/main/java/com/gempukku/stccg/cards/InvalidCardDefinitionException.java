package com.gempukku.stccg.cards;

public class InvalidCardDefinitionException extends Exception {
    public InvalidCardDefinitionException(String message) {
        super(message);
    }

    public InvalidCardDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
