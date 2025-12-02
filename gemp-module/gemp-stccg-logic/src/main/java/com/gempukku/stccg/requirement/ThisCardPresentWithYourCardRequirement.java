package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

public class ThisCardPresentWithYourCardRequirement implements Requirement {
    private final FilterBlueprint _otherCardFilter;

    public ThisCardPresentWithYourCardRequirement(@JsonProperty("otherCardFilter")
                                                FilterBlueprint otherCardFilter
    ) {
        _otherCardFilter = otherCardFilter;
    }

    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        try {
            PhysicalCard thisCard = actionContext.getPerformingCard(cardGame);
            CardFilter cardFilter = Filters.and(
                    Filters.yourCardsPresentWithThisCard(thisCard),
                    _otherCardFilter.getFilterable(cardGame, actionContext)
            );
            Collection<PhysicalCard> filteredCards = Filters.filter(cardGame, cardFilter);
            return !filteredCards.isEmpty();
        } catch(InvalidGameLogicException exp) {
            return false;
        }
    }
}