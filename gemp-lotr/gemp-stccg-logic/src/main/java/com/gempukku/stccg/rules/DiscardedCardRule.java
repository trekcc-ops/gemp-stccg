package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.OptionalTriggerAction;
import com.gempukku.stccg.actions.RequiredTriggerAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.results.DiscardCardsFromPlayResult;
import com.gempukku.stccg.results.EffectResult;

import java.util.Collections;
import java.util.List;

public class DiscardedCardRule {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public DiscardedCardRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(DefaultGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY) {
                            DiscardCardsFromPlayResult discardResult = (DiscardCardsFromPlayResult) effectResult;
                            final PhysicalCard discardedCard = discardResult.getDiscardedCard();
                            RequiredTriggerAction trigger = discardedCard.getBlueprint().getDiscardedFromPlayRequiredTrigger(game, discardedCard);
                            if (trigger != null)
                                return Collections.singletonList(trigger);
                        }
                        return null;
                    }

                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, DefaultGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY) {
                            DiscardCardsFromPlayResult discardResult = (DiscardCardsFromPlayResult) effectResult;
                            final PhysicalCard discardedCard = discardResult.getDiscardedCard();
                            if (discardedCard.getOwnerName().equals(playerId)) {
                                OptionalTriggerAction trigger = discardedCard.getBlueprint().getDiscardedFromPlayOptionalTrigger(playerId, game, discardedCard);
                                if (trigger != null) {
                                    trigger.setVirtualCardAction(true);
                                    return Collections.singletonList(trigger);
                                }
                            }
                        }
                        return null;
                    }
                });
    }
}
