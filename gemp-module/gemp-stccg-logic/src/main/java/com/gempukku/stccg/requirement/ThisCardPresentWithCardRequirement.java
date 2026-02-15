package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ThisCardPresentWithCardRequirement implements Requirement {
    private final FilterBlueprint _otherCardFilter;

    public ThisCardPresentWithCardRequirement(@JsonProperty("otherCardFilter")
                                                FilterBlueprint otherCardFilter
    ) {
        _otherCardFilter = otherCardFilter;
    }

    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        PhysicalCard thisCard = actionContext.card();
        CardFilter cardFilter = Filters.and(
                Filters.presentWithThisCard(thisCard.getCardId()),
                _otherCardFilter.getFilterable(cardGame, actionContext)
        );
        Collection<PhysicalCard> filteredCards = Filters.filter(cardGame, cardFilter);
        return !filteredCards.isEmpty();
    }

    public Condition getCondition(ActionContext actionContext, PhysicalCard thisCard, DefaultGame cardGame) {
        CardFilter cardFilter = Filters.and(
                Filters.presentWithThisCard(thisCard.getCardId()),
                _otherCardFilter.getFilterable(cardGame, actionContext)
        );
        return new CardInPlayCondition(cardFilter);
    }
}