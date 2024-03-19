package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.SkillType;

public class SpecialDownloadSkill extends Skill {
    private final String _cardName;

    public SpecialDownloadSkill(String cardName) {
        super(SkillType.SPECIAL);
        _cardName = cardName;
    }

        // TODO - Nothing implemented for special download skill yet

/*    protected Action getDownloadAction(PhysicalCard thisCard) {
        return (Action) new SpecialDownloadAction(thisCard, Filters.name(_cardName));
    } */
}
