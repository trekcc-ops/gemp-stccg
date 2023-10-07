package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.condition.Condition;
import com.gempukku.lotro.game.PlayConditions;

public class DiscardFromPlayExtraPlayCostModifier extends AbstractExtraPlayCostModifier {
    private final int count;
    private final Filterable[] cardFilter;

    public DiscardFromPlayExtraPlayCostModifier(PhysicalCard source, Filterable affects, int count, Condition condition, Filterable... cardFilter) {
        super(source, "Discard card(s) to play", affects, condition);
        this.count = count;
        this.cardFilter = cardFilter;
    }

    @Override
    public void appendExtraCosts(DefaultGame game, CostToEffectAction action, PhysicalCard card) {
        action.appendCost(
                new ChooseAndDiscardCardsFromPlayEffect(action, card.getOwner(), count, count, cardFilter));
    }

    @Override
    public boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard card) {
        return PlayConditions.canDiscardFromPlay(card, game, count, cardFilter);
    }
}
