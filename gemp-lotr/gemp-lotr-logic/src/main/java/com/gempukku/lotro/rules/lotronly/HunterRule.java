package com.gempukku.lotro.rules.lotronly;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.modifiers.StrengthModifier;

public class HunterRule {
    private final ModifiersLogic _modifiersLogic;

    public HunterRule(ModifiersLogic modifiersLogic) {
        _modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        _modifiersLogic.addAlwaysOnModifier(
                new StrengthModifier(null, Filters.and(Keyword.HUNTER, Filters.inSkirmishAgainst(Filters.character, Filters.not(Keyword.HUNTER))), null,
                        (game, self) -> game.getModifiersQuerying().getKeywordCount(game, self, Keyword.HUNTER)));
    }
}
