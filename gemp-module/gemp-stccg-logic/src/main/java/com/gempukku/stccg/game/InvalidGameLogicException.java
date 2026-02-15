package com.gempukku.stccg.game;

public class InvalidGameLogicException extends InvalidGameOperationException {
    public InvalidGameLogicException(String message) {
        super(message);
    }
}