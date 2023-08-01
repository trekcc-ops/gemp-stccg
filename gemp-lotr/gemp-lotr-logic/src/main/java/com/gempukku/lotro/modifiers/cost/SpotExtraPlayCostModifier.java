package com.gempukku.lotro.modifiers.cost;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.effects.SpotEffect;
import com.gempukku.lotro.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.modifiers.condition.Condition;
import com.gempukku.lotro.game.PlayConditions;

public class SpotExtraPlayCostModifier extends AbstractExtraPlayCostModifier {
    private final int count;
    private final Filterable[] spotFilter;

    public SpotExtraPlayCostModifier(LotroPhysicalCard source, Filterable affects, Condition condition, Filterable ...spotFilter) {
        this(source, affects, condition, 1, spotFilter);
    }

    public SpotExtraPlayCostModifier(LotroPhysicalCard source, Filterable affects, Condition condition, int count, Filterable ...spotFilter) {
        super(source, "Spot to play", Filters.and(affects), condition);
        this.count = count;
        this.spotFilter = spotFilter;
    }

    @Override
    public boolean canPayExtraCostsToPlay(DefaultGame game, LotroPhysicalCard card) {
        return PlayConditions.canSpot(game, count, spotFilter);
    }

    @Override
    public void appendExtraCosts(DefaultGame game, CostToEffectAction action, LotroPhysicalCard card) {
        action.appendCost(
                new SpotEffect(count, spotFilter));
    }
}