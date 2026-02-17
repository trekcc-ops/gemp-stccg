package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.game.ST1EGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ToCardDestinationBlueprint implements DestinationBlueprint {

    private final FilterBlueprint _destinationFilter;
    private final boolean _mustBeMatching;

    public ToCardDestinationBlueprint(@JsonProperty(value = "destinationCardFilter") FilterBlueprint destinationFilter,
                                      @JsonProperty(value = "matching") boolean mustBeMatching) {
        _destinationFilter = destinationFilter;
        _mustBeMatching = mustBeMatching;
    }

    public Collection<PhysicalCard> getDestinationOptions(ST1EGame stGame, String performingPlayerName,
                                                          PhysicalCard cardArriving, ActionContext context) {
        CardFilter destination = Filters.and(_destinationFilter.getFilterable(stGame, context));
        if (_mustBeMatching) {
            destination = Filters.and(destination, new MatchingAffiliationFilter(List.of(cardArriving), performingPlayerName));
        }
        Collection<PhysicalCard> result = new ArrayList<>();
        for (PhysicalCard card : Filters.filterCardsInPlay(stGame, destination)) {
            if (card.getCardType() == CardType.PERSONNEL) {
                if (card.getAttachedTo(stGame) != null && !result.contains(card.getAttachedTo(stGame))) {
                    result.add(card.getAttachedTo(stGame));
                }
            } else if (!result.contains(card)) {
                result.add(card);
            }
        }
        return result;
    }

}