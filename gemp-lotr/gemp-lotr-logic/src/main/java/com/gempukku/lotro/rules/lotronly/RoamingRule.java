package com.gempukku.lotro.rules.lotronly;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.modifiers.lotronly.KeywordModifier;

public class RoamingRule {
    private final ModifiersLogic _modifiersLogic;

    public RoamingRule(ModifiersLogic modifiersLogic) {
        _modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        Filter roamingFilter = Filters.and(CardType.MINION, (Filter) (game, physicalCard) -> (game.getModifiersQuerying().getMinionSiteNumber(game, physicalCard) > game.getGameState().getCurrentSiteNumber()));

        _modifiersLogic.addAlwaysOnModifier(
                new KeywordModifier(null, roamingFilter, Keyword.ROAMING));
    }
}
