package com.gempukku.lotro.modifiers.cost;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.AttachPermanentAction;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.condition.Condition;

public class ExertTargetExtraPlayCostModifier extends AbstractExtraPlayCostModifier {
    public ExertTargetExtraPlayCostModifier(LotroPhysicalCard source, Filterable affects, Condition condition) {
        super(source, "Exert to play", Filters.and(affects), condition);
    }

    @Override
    public boolean canPayExtraCostsToPlay(DefaultGame game, LotroPhysicalCard card) {
        return true;
    }

    @Override
    public void appendExtraCosts(DefaultGame game, CostToEffectAction action, LotroPhysicalCard card) {
        ((AttachPermanentAction) action).setExertTarget(true);
    }
}
