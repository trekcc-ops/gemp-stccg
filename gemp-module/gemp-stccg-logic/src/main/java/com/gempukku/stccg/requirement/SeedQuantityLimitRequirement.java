package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class SeedQuantityLimitRequirement implements Requirement {

    private final int _seedQuantityLimit;

    public SeedQuantityLimitRequirement(int limit) {
        _seedQuantityLimit = limit;
    }
    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        PhysicalCard cardSeeding = actionContext.card();
        int copiesSeeded = cardSeeding.getNumberOfCopiesSeededByPlayer(actionContext.getPerformingPlayerId(), cardGame);
        return copiesSeeded < _seedQuantityLimit;
    }
}