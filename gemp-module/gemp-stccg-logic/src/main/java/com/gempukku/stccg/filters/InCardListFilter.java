package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class InCardListFilter implements CardFilter {

    private final Collection<? extends PhysicalCard> _cardList;

    public InCardListFilter(Collection<? extends PhysicalCard> cardList) {
        _cardList = cardList;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return _cardList.contains(physicalCard);
    }
}