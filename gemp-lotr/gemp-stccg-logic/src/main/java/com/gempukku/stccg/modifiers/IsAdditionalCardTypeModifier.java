package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class IsAdditionalCardTypeModifier extends AbstractModifier {
    private final CardType _cardType;

    public IsAdditionalCardTypeModifier(PhysicalCard source, Filterable affectFilter, CardType cardType) {
        this(source, affectFilter, null, cardType);
    }

    public IsAdditionalCardTypeModifier(PhysicalCard source, Filterable affectFilter, Condition condition, CardType cardType) {
        super(source, "Has additional card type - " + cardType.toString(), affectFilter, condition, ModifierEffect.ADDITIONAL_CARD_TYPE);
        _cardType = cardType;
    }

    @Override
    public boolean isAdditionalCardTypeModifier(DefaultGame game, PhysicalCard physicalCard, CardType cardType) {
        return cardType == _cardType;
    }
}
