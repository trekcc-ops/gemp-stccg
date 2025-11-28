package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;

public class TurnLimitRequirement implements Requirement {

    private final int _limitPerTurn;

    public TurnLimitRequirement(int limitPerTurn) {
        _limitPerTurn = limitPerTurn;
    }
    @Override
    public boolean accepts(ActionContext actionContext) {
        return actionContext.getSource().checkTurnLimit(actionContext.getGame(), _limitPerTurn);
    }
}