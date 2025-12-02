package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class TurnLimitRequirement implements Requirement {

    private final int _limitPerTurn;

    public TurnLimitRequirement(int limitPerTurn) {
        _limitPerTurn = limitPerTurn;
    }
    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        try {
            return actionContext.getPerformingCard(cardGame).checkTurnLimit(cardGame, _limitPerTurn);
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }
}