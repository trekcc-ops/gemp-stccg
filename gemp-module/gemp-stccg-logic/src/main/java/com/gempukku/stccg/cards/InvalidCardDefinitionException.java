package com.gempukku.stccg.cards;

import java.io.IOException;

public class InvalidCardDefinitionException extends IOException {
    public InvalidCardDefinitionException(String message) {
        super(message);
    }

}