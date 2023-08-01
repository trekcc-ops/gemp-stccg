package com.gempukku.lotro.modifiers.cost;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.effects.AddThreatsEffect;
import com.gempukku.lotro.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.modifiers.condition.Condition;

public class AddUpToThreatsExtraPlayCostModifier extends AbstractExtraPlayCostModifier {
    private final int maxThreatCount;

    public AddUpToThreatsExtraPlayCostModifier(LotroPhysicalCard source, int maxThreatCount, Condition condition, Filterable... affects) {
        super(source, "Add up to " + maxThreatCount + " threat(s)", Filters.and(affects), condition);
        this.maxThreatCount = maxThreatCount;
    }

    @Override
    public void appendExtraCosts(DefaultGame
                                         game, final CostToEffectAction action, final LotroPhysicalCard card) {
        int maxThreats = Math.min(maxThreatCount, Filters.countActive(game, CardType.COMPANION) - game.getGameState().getThreats());
        action.appendCost(
                new PlayoutDecisionEffect(card.getOwner(),
                        new IntegerAwaitingDecision(1, "Choose how many threats to add", 0, maxThreats) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                int threats = getValidatedResult(result);
                                action.appendCost(
                                        new AddThreatsEffect(card.getOwner(), card, threats));
                            }
                        }));
    }

    @Override
    public boolean canPayExtraCostsToPlay(DefaultGame game, LotroPhysicalCard card) {
        return true;
    }
}