package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.ConstantValueSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;

public class GainIconModifier extends AbstractModifier implements IconAffectingModifier {
    private final CardIcon _icon;
    private final ValueSource _valueSource;
    private final ActionContext _context;

    public GainIconModifier(ActionContext context, Filterable affectFilter, Condition condition, CardIcon icon) {
        super(context.getSource(), null, affectFilter, condition, ModifierEffect.GAIN_ICON_MODIFIER);
        _context = context;
        _icon = icon;
        _valueSource = new ConstantValueSource(1);
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
        return (icon == _icon && _valueSource.evaluateExpression(_context, physicalCard) > 0);
    }
}
