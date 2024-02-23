package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.RequiredType;
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
                    public List<? extends Action> getRequiredAfterTriggers(EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY) {
                            DiscardCardsFromPlayResult discardResult = (DiscardCardsFromPlayResult) effectResult;
                            final PhysicalCard discardedCard = discardResult.getDiscardedCard();
                            Action trigger = discardedCard.getDiscardedFromPlayTriggerAction(RequiredType.REQUIRED);
                            if (trigger != null)
                                return Collections.singletonList(trigger);
                        }
                        return null;
                    }

                    @Override
                    public List<? extends Action> getOptionalAfterTriggerActions(String playerId, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY) {
                            DiscardCardsFromPlayResult discardResult = (DiscardCardsFromPlayResult) effectResult;
                            final PhysicalCard discardedCard = discardResult.getDiscardedCard();
                            if (discardedCard.getOwnerName().equals(playerId)) {
                                Action trigger = discardedCard.getDiscardedFromPlayTriggerAction(RequiredType.OPTIONAL);
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
