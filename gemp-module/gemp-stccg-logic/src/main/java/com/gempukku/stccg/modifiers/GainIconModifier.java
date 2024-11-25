package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;

public class GainIconModifier extends AbstractModifier implements IconAffectingModifier {
    private final CardIcon _icon;

    public GainIconModifier(ActionContext context, Filterable affectFilter, Condition condition, CardIcon icon) {
        super(context.getSource(), null, affectFilter, condition, ModifierEffect.GAIN_ICON_MODIFIER);
        _icon = icon;
    }

    @Override
    public CardIcon getIcon() {
        return _icon;
    }

    @Override
    public String getCardInfoText(PhysicalCard affectedCard) {
        return "Gains " + _icon.toHTML() + " from " + _cardSource.getCardLink();
    }

    @Override
    public boolean hasIcon(PhysicalCard physicalCard, CardIcon icon) {
        return (icon == _icon);
    }
}