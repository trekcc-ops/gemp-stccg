package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.player.Player;

public class SeedQuantityLimitRequirement implements Requirement {

    private final int _seedQuantityLimit;

    public SeedQuantityLimitRequirement(int limit) {
        _seedQuantityLimit = limit;
    }
    @Override
    public boolean accepts(ActionContext actionContext) {
        PhysicalCard cardSeeding = actionContext.getSource();
        Player seedingPlayer = actionContext.getPerformingPlayer();
        int copiesSeeded = cardSeeding.getNumberOfCopiesSeededByPlayer(seedingPlayer, actionContext.getGame());
        return copiesSeeded < _seedQuantityLimit;
    }
}