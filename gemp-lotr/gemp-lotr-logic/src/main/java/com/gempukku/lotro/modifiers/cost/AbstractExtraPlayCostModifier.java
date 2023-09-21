package com.gempukku.lotro.modifiers.cost;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.condition.Condition;
import com.gempukku.lotro.modifiers.AbstractModifier;
import com.gempukku.lotro.modifiers.ExtraPlayCost;
import com.gempukku.lotro.modifiers.ModifierEffect;

public abstract class AbstractExtraPlayCostModifier extends AbstractModifier implements ExtraPlayCost {

    public AbstractExtraPlayCostModifier(LotroPhysicalCard source, String text, Filterable affectFilter, Condition condition) {
        super(source, text, affectFilter, condition, ModifierEffect.EXTRA_COST_MODIFIER);
    }

    @Override
    public abstract void appendExtraCosts(DefaultGame game, CostToEffectAction action, LotroPhysicalCard card);

    @Override
    public abstract boolean canPayExtraCostsToPlay(DefaultGame game, LotroPhysicalCard card);
}
