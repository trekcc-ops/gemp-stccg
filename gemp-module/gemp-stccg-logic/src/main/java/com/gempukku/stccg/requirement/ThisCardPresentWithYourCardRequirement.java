package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.requirement.Requirement;

import java.util.Collection;

public class ThisCardPresentWithYourCardRequirement implements Requirement {
    private final FilterBlueprint _otherCardFilter;

    public ThisCardPresentWithYourCardRequirement(@JsonProperty("otherCardFilter")
                                                FilterBlueprint otherCardFilter
    ) {
        _otherCardFilter = otherCardFilter;
    }

    @Override
    public boolean accepts(ActionContext actionContext) {
        PhysicalCard thisCard = actionContext.getSource();
        CardFilter cardFilter = Filters.and(
                Filters.yourCardsPresentWithThisCard(thisCard),
                _otherCardFilter.getFilterable(actionContext)
        );
        Collection<PhysicalCard> filteredCards = Filters.filter(actionContext.getGame(), cardFilter);
        return !filteredCards.isEmpty();
    }
}