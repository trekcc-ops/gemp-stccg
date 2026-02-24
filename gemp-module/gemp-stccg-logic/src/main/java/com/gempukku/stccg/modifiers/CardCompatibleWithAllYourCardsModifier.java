package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.TrueCondition;

public class CardCompatibleWithAllYourCardsModifier extends AbstractModifier {

    private final CardFilter _filter;
    private final String _yourName;

    public CardCompatibleWithAllYourCardsModifier(PhysicalCard performingCard, String yourName, CardFilter filter) {
        super(performingCard, filter, new TrueCondition(), ModifierEffect.COMPATIBILITY_MODIFIER, false);
        _filter = filter;
        _yourName = yourName;
    }

    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return null;
    }

    public boolean cardsAreCompatible(DefaultGame cardGame, PhysicalCard card1, PhysicalCard card2) {
        return (_filter.accepts(cardGame, card1) && Filters.your(_yourName).accepts(cardGame, card2)) ||
                (_filter.accepts(cardGame, card2) && Filters.your(_yourName).accepts(cardGame, card1));
    }
}