package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Condition;

public class GainIconModifier extends AbstractModifier implements IconAffectingModifier {
    private final CardIcon _icon;

    public GainIconModifier(PhysicalCard performingCard, Filterable affectFilter, Condition condition, CardIcon icon) {
        super(performingCard, null, Filters.and(affectFilter), condition, ModifierEffect.GAIN_ICON_MODIFIER);
        _icon = icon;
    }

    @Override
    public CardIcon getIcon() {
        return _icon;
    }

    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        String message = "Gains " + _icon.toHTML();
        if (_cardSource != null) {
            message = message + " from " + _cardSource.getCardLink();
        }
        return message;
    }

    @Override
    public boolean hasIcon(PhysicalCard physicalCard, CardIcon icon) {
        return (icon == _icon);
    }
}