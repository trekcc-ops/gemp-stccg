package com.gempukku.stccg.actions.targetresolver;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;

public class ReadCardMemoryBlueprint implements TargetResolverBlueprint {

    private final String _memoryId;
    public ReadCardMemoryBlueprint(@JsonProperty(value = "memoryId", required = true)
                              String memoryId) {
        _memoryId = memoryId;
    }

    public ActionCardResolver getTargetResolver(DefaultGame cardGame, GameTextContext context) {
        Collection<Integer> cardIds = context.getCardIdsFromMemory(_memoryId);
        Collection<PhysicalCard> memoryCards = new ArrayList<>();
        for (Integer cardId : cardIds) {
            try {
                memoryCards.add(cardGame.getCardFromCardId(cardId));
            } catch(CardNotFoundException exp) {
                cardGame.sendErrorMessage(exp);
            }
        }
        return new FixedCardsResolver(memoryCards);
    }

    public void addFilter(FilterBlueprint... filterBlueprints) {
    }

    public boolean canBeResolved(DefaultGame cardGame, GameTextContext context) {
        Collection<Integer> cards = context.getCardIdsFromMemory(_memoryId);
        return cards != null && !cards.isEmpty();
    }

}