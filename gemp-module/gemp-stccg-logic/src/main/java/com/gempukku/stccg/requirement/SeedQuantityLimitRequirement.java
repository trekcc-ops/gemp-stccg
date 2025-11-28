package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public class SeedQuantityLimitRequirement implements Requirement {

    private final int _seedQuantityLimit;

    public SeedQuantityLimitRequirement(int limit) {
        _seedQuantityLimit = limit;
    }
    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        PhysicalCard cardSeeding = actionContext.getSource();
        Player seedingPlayer = actionContext.getPerformingPlayer();
        int copiesSeeded = cardSeeding.getNumberOfCopiesSeededByPlayer(seedingPlayer, cardGame);
        return copiesSeeded < _seedQuantityLimit;
    }
}