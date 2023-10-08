package com.gempukku.stccg.rules.lotronly;

import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.modifiers.StrengthModifier;

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
