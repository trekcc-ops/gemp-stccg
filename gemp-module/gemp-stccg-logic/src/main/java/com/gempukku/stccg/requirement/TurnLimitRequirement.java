package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class TurnLimitRequirement implements Requirement {

    private final int _limitPerTurn;

    public TurnLimitRequirement(int limitPerTurn) {
        _limitPerTurn = limitPerTurn;
    }
    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        return actionContext.getSource().checkTurnLimit(cardGame, _limitPerTurn);
    }
}