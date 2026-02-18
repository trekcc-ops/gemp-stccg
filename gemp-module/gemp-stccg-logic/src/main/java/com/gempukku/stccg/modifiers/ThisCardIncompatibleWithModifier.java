package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.TrueCondition;

public class ThisCardIncompatibleWithModifier extends AbstractModifier {

    public ThisCardIncompatibleWithModifier(PhysicalCard source, CardFilter affectedCards) {
        super(source, affectedCards, new TrueCondition(), ModifierEffect.COMPATIBILITY_MODIFIER, false);
    }

    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return null;
    }

    public boolean cardsAreIncompatible(DefaultGame cardGame, PhysicalCard card1, PhysicalCard card2) {
        return (_cardSource == card1 && _affectedCardsFilter.accepts(cardGame, card2)) ||
                (_cardSource == card2 && _affectedCardsFilter.accepts(cardGame, card1));
    }
}