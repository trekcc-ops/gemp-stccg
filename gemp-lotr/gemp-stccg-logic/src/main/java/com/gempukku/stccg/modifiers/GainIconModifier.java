package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Icon1E;
import com.gempukku.stccg.condition.Condition;

public class GainIconModifier extends AbstractModifier implements IconAffectingModifier {
    private final Icon1E _icon;
    private final ValueSource _valueSource;
    private final ActionContext _context;

    public GainIconModifier(ActionContext context, Filterable affectFilter, Condition condition, Icon1E icon,
                            ValueSource valueSource) {
        super(context.getSource(), null, affectFilter, condition, ModifierEffect.GAIN_ICON_MODIFIER);
        _context = context;
        _icon = icon;
        _valueSource = valueSource;
    }

    @Override
    public Icon1E getIcon() {
        return _icon;
    }

    @Override
    public String getCardInfoText(PhysicalCard affectedCard) {
        return "Gains " + _icon.toHTML() + " from " + _cardSource.getCardLink();
    }

    @Override
    public boolean hasIcon(PhysicalCard physicalCard, Icon1E icon) {
        return (icon == _icon && _valueSource.evaluateExpression(_context, physicalCard) > 0);
    }
}
