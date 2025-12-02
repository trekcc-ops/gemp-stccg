package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InCardListFilter implements CardFilter {

    @JsonProperty("cardIdList")
    private final List<Integer> _cardIdList = new ArrayList<>();

    public InCardListFilter(Collection<? extends PhysicalCard> cardList) {
        for (PhysicalCard card : cardList) {
            _cardIdList.add(card.getCardId());
        }
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return _cardIdList.contains(physicalCard.getCardId());
    }
}