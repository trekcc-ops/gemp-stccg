package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.TextUtils;

public class CostModifier extends AbstractModifier {
    private final ValueSource _valueSource;
    private final ActionContext _actionContext;

    public CostModifier(ActionContext actionContext, Filterable affectFilter, Condition condition, ValueSource valueSource) {
        super(actionContext.getSource(), null, affectFilter, condition, ModifierEffect.TWILIGHT_COST_MODIFIER);
        _valueSource = valueSource;
        _actionContext = actionContext;
    }

    @Override
    public String getCardInfoText(PhysicalCard affectedCard) {
        return "Cost " + TextUtils.signed(_valueSource.evaluateExpression(_actionContext, affectedCard)) +
                " from " + _cardSource.getCardLink();
    }

    @Override
    public int getTwilightCostModifier(PhysicalCard physicalCard, PhysicalCard target, boolean ignoreRoamingPenalty) {
        return _valueSource.evaluateExpression(_actionContext, physicalCard);
    }
}
