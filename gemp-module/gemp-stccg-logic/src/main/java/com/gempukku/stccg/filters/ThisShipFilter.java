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
        if (_contextCard.getCardType() == CardType.SHIP) {
            return physicalCard == _contextCard;
        } else {
            return _contextCard.getParentCard() == physicalCard && physicalCard.getCardType() == CardType.SHIP;
        }
    }
}