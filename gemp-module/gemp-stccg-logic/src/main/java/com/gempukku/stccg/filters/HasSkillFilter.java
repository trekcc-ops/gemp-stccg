package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

public class HasSkillFilter implements CardFilter {

    @JsonProperty("skill")
    private final SkillName _skill;

    public HasSkillFilter(SkillName skill) {
        _skill = skill;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.hasSkill(_skill, game);
    }
}