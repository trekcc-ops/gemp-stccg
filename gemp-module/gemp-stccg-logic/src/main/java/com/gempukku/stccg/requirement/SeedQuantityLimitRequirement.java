package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class SeedQuantityLimitRequirement implements Requirement {

    private final int _seedQuantityLimit;

    public SeedQuantityLimitRequirement(int limit) {
        _seedQuantityLimit = limit;
    }
    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        try {
            PhysicalCard cardSeeding = actionContext.getPerformingCard(cardGame);
            int copiesSeeded = cardSeeding.getNumberOfCopiesSeededByPlayer(actionContext.getPerformingPlayerId(), cardGame);
            return copiesSeeded < _seedQuantityLimit;
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }
}