package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.OptionalTriggerAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.filters.Filters;

import java.util.LinkedList;
import java.util.List;

public class OptionalTriggersFromHandRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public OptionalTriggersFromHandRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, EffectResult effectResult) {
                        List<OptionalTriggerAction> result = new LinkedList<>();
                        for (PhysicalCard responseEvent : Filters.filter(actionsEnvironment.getGame().getGameState().getHand(playerId), actionsEnvironment.getGame())) {
                            final List<OptionalTriggerAction> actions = responseEvent.getBlueprint().getOptionalInHandAfterTriggers(playerId, actionsEnvironment.getGame(), effectResult, responseEvent);
                            if (actions != null)
                                result.addAll(actions);
                        }
                        return result;
                    }
                });
    }
}
