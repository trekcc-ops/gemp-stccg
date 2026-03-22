package com.gempukku.stccg.requirement;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class EndOfThisTurnCondition implements Condition {

    private int _turnNumber;

    public EndOfThisTurnCondition(DefaultGame cardGame) {
        _turnNumber = cardGame.getGameState().getCurrentTurnNumber();
    }
    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        return cardGame.getGameState().getCurrentTurnNumber() == _turnNumber &&
                cardGame.getCurrentPhase() == Phase.END_OF_TURN;
    }
}