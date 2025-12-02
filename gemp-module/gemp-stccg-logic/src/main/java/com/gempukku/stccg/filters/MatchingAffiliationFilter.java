package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;

public class MatchingAffiliationFilter implements CardFilter {

    @JsonProperty("cardIdsToMatch")
    private final Collection<Integer> _cardIdsToMatch = new ArrayList<>();

    public MatchingAffiliationFilter(Collection<PhysicalCard> cardsToMatch) {
        for (PhysicalCard card : cardsToMatch) {
            _cardIdsToMatch.add(card.getCardId());
        }
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        boolean matching = true;
        for (Integer cardId : _cardIdsToMatch) {
            try {
                PhysicalCard cardToMatch = game.getCardFromCardId(cardId);
                if (physicalCard instanceof AffiliatedCard affilCard1 && cardToMatch instanceof AffiliatedCard affilCard2) {
                    if (!affilCard1.matchesAffiliationOf(affilCard2))
                        matching = false;
                } else {
                    matching = false;
                }
            } catch(CardNotFoundException exp) {
                game.sendErrorMessage(exp);
                matching = false;
            }
        }
        return matching;
    }
}