package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.ComparatorType;
import com.gempukku.stccg.game.DefaultGame;

public class SkillDotFilter implements CardFilter {

    @JsonProperty("count")
    private final int _count;

    @JsonProperty("comparator")
    private final ComparatorType _comparatorType;

    public SkillDotFilter(int count, ComparatorType comparatorType) {
        _count = count;
        _comparatorType = comparatorType;
    }


    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return _comparatorType.isTrue(physicalCard.getBlueprint().getSkillDotCount(), _count);
    }

}