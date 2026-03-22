package com.gempukku.stccg.player;

import com.gempukku.stccg.game.InvalidGameOperationException;

public class PlayerNotFoundException extends InvalidGameOperationException {
    public PlayerNotFoundException(String message) {
        super(message);
    }
}