package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gempukku.stccg.game.DefaultGame;

public abstract class NoResponseActionResult extends ActionResult {
    public NoResponseActionResult(DefaultGame cardGame, ActionResultType type, String performingPlayerId, Action action) {
        super(cardGame, type, performingPlayerId, action);
    }

    @Override
    public void initialize(DefaultGame cardGame) {

    }

    @JsonIgnore
    @Override
    public boolean canBeRespondedTo() {
        return false;
    }

}