package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class SkillDotsLessThanOrEqualToCardFilter implements CardFilter {

    private final int _count;

    public SkillDotsLessThanOrEqualToCardFilter(int count) {
        _count = count;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getBlueprint().getSkillDotCount() <= _count;
    }

}