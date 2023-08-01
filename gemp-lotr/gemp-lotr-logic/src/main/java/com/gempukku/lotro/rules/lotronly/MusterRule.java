package com.gempukku.lotro.rules.lotronly;

import com.gempukku.lotro.actions.AbstractActionProxy;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.effects.DrawCardsEffect;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.actions.OptionalTriggerAction;
import com.gempukku.lotro.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.effects.EffectResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MusterRule {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public MusterRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(final String playerId, final DefaultGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.START_OF_PHASE
                                && game.getGameState().getCurrentPhase() == Phase.REGROUP
                                && game.getGameState().getHand(playerId).size() > 0) {
                            LotroPhysicalCard firstMuster = Filters.findFirstActive(game, Filters.owner(playerId), Keyword.MUSTER);
                            if (firstMuster != null) {
                                final OptionalTriggerAction action = new OptionalTriggerAction(firstMuster);
                                action.setTriggerIdentifier("muster-"+playerId);
                                action.setVirtualCardAction(true);
                                action.setText("Use Muster");
                                ChooseAndDiscardCardsFromHandEffect effect = new ChooseAndDiscardCardsFromHandEffect(action, playerId, false,
                                        new ConstantEvaluator(0), new Evaluator() {
                                    @Override
                                    public int evaluateExpression(DefaultGame game, LotroPhysicalCard cardAffected) {
                                        return Filters.filterActive(game, Filters.owner(playerId), Keyword.MUSTER).size();
                                    }
                                }) {
                                    @Override
                                    protected void cardsBeingDiscardedCallback(Collection<LotroPhysicalCard> cardsBeingDiscarded) {
                                        if (cardsBeingDiscarded.size() > 0)
                                            action.appendEffect(
                                                    new DrawCardsEffect(action, playerId, cardsBeingDiscarded.size()));
                                    }
                                };
                                effect.setText("Choose cards to discard to Muster (all at once)");
                                action.appendEffect(effect);

                                return Collections.singletonList(action);
                            }
                        }
                        return null;
                    }
                });
    }
}