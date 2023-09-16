package com.gempukku.lotro.rules.lotronly;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.modifiers.StrengthModifier;

public class EnduringRule {
    private final ModifiersLogic _modifiersLogic;

    public EnduringRule(ModifiersLogic modifiersLogic) {
        _modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        _modifiersLogic.addAlwaysOnModifier(
                new StrengthModifier(null, Filters.and(Filters.wounded, Keyword.ENDURING), null,
                        (game, self) -> 2 * game.getGameState().getWounds(self)));
    }
}
