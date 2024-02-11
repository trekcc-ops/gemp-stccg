package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.game.PlayConditions;

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
                new ChooseAndDiscardCardsFromPlayEffect(game, action, card.getOwnerName(), count, count, cardFilter));
    }

    @Override
    public boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard card) {
        return PlayConditions.canDiscardFromPlay(card, game, count, cardFilter);
    }
}
