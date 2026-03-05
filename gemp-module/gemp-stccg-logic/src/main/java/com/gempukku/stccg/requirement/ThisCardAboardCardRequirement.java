package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

public class ThisCardAboardCardRequirement implements Requirement {
    private final FilterBlueprint _aboardCardFilter;

    public ThisCardAboardCardRequirement(@JsonProperty("aboardCardFilter")
                                                FilterBlueprint aboardCardFilter
    ) {
        _aboardCardFilter = aboardCardFilter;
    }

    public boolean accepts(GameTextContext context, DefaultGame cardGame) {
        PhysicalCard thisCard = context.card();
        PhysicalCard aboardCard = thisCard.getAboardCard();
        CardFilter aboardCardFilter = _aboardCardFilter.getFilterable(cardGame, context);
        return aboardCard != null && aboardCardFilter.accepts(cardGame, aboardCard);
    }
}