package com.gempukku.stccg.actions.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.game.DefaultGame;

public class StartTurnResult extends ActionResult {

    @JsonProperty("gameTurnNumber")
    private final int _turnNumber;

    @JsonProperty("playerTurnNumber")
    private final int _playerTurnNumber;

    public StartTurnResult(DefaultGame cardGame, StartTurnAction action) {
        super(cardGame, ActionResultType.STARTED_TURN, action);
        _turnNumber = cardGame.getGameState().getCurrentTurnNumber();
        _playerTurnNumber = cardGame.getGameState().getCurrentPlayerTurnNumber();
    }
}