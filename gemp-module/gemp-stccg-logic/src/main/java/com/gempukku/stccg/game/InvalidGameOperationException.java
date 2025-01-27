package com.gempukku.stccg.game;

public class InvalidGameOperationException extends Exception {
    public InvalidGameOperationException(String message) {
        super(message);
    }

    public InvalidGameOperationException(Exception exp) {
        super(exp.getMessage());
    }

}