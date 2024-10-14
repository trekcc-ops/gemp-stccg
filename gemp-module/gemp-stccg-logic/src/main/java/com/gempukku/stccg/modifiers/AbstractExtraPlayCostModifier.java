package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.condition.Condition;

public abstract class AbstractExtraPlayCostModifier extends AbstractModifier implements ExtraPlayCost {

    public AbstractExtraPlayCostModifier(PhysicalCard source, String text, Filterable affectFilter,
                                         Condition condition) {
        super(source, text, affectFilter, condition, ModifierEffect.EXTRA_COST_MODIFIER);
    }

    @Override
    public abstract void appendExtraCosts(DefaultGame game, CostToEffectAction action, PhysicalCard card);

    @Override
    public abstract boolean canPayExtraCostsToPlay(PhysicalCard card);
}