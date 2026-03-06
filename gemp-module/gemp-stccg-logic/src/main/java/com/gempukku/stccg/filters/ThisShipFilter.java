package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.game.DefaultGame;

public class ThisShipFilter implements CardFilter {

    private final PhysicalCard _contextCard;

    public ThisShipFilter(PhysicalCard contextCard) {
        _contextCard = contextCard;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return filteringCardIsThisShipForContextCard(physicalCard, _contextCard);
    }

    private boolean filteringCardIsThisShipForContextCard(PhysicalCard filteringCard, PhysicalCard contextCard) {
        if (filteringCard.getCardType() != CardType.SHIP) {
            return false;
        } else if (contextCard.getCardType() == CardType.SHIP) {
            return filteringCard == contextCard;
        } else if (contextCard.getParentCard() != null) {
            return filteringCardIsThisShipForContextCard(filteringCard, contextCard.getParentCard());
        } else {
            return false;
        }
    }
}