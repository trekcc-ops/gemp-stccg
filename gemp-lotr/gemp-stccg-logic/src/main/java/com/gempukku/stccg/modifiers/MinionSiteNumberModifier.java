package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;

public class MinionSiteNumberModifier extends AbstractModifier {
    private final Evaluator evaluator;
    private final boolean nonCardTextModifier;

    public MinionSiteNumberModifier(PhysicalCard source, Filterable affectFilter, int modifier) {
        this(source, affectFilter, null, modifier);
    }

    public MinionSiteNumberModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int modifier) {
        this(source, affectFilter, condition, new ConstantEvaluator(modifier));
    }

    public MinionSiteNumberModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        this(source, affectFilter, condition, evaluator, false);
    }

    public MinionSiteNumberModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator, boolean nonCardTextModifier) {
        super(source, null, affectFilter, condition, ModifierEffect.SITE_NUMBER_MODIFIER);
        this.evaluator = evaluator;
        this.nonCardTextModifier = nonCardTextModifier;
    }

    @Override
    public boolean isNonCardTextModifier() {
        return nonCardTextModifier;
    }

    @Override
    public String getText(DefaultGame game, PhysicalCard self) {
        final int value = evaluator.evaluateExpression(game, self);
        if (value >= 0)
            return "Site number +" + value;
        else
            return "Site number " + value;
    }

    @Override
    public int getMinionSiteNumberModifier(DefaultGame game, PhysicalCard physicalCard) {
        return evaluator.evaluateExpression(game, physicalCard);
    }
}
