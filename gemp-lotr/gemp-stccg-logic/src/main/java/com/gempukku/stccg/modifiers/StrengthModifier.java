package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.rules.TextUtils;

public class StrengthModifier extends AbstractModifier {
    private final Evaluator _evaluator;
    private final boolean _nonCardTextModifier;

    public StrengthModifier(ActionContext context, Filterable affectFilter, int modifier) {
        this(context, affectFilter, null, new ConstantEvaluator(context, modifier));
    }

    public StrengthModifier(ActionContext context, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        super(context.getSource(), null, affectFilter, condition, ModifierEffect.STRENGTH_MODIFIER);
        _evaluator = evaluator;
        _nonCardTextModifier = false;
    }


    public StrengthModifier(ModifiersLogic modifiersLogic, Filterable affectFilter, Condition condition,
                            Evaluator evaluator, boolean nonCardTextModifier) {
        super(modifiersLogic.getGame(),affectFilter, condition, ModifierEffect.STRENGTH_MODIFIER);
        _evaluator = evaluator;
        _nonCardTextModifier = nonCardTextModifier;
    }


    @Override
    public String getCardInfoText(PhysicalCard affectedCard) {
        return "STRENGTH " + TextUtils.signed(_evaluator.evaluateExpression(_game, affectedCard)) +
                " from " + _cardSource.getCardLink();
    }

    @Override
    public int getStrengthModifier(PhysicalCard physicalCard) {
        return _evaluator.evaluateExpression(_game, physicalCard);
    }

    @Override
    public boolean isNonCardTextModifier() {
        return _nonCardTextModifier;
    }
}
