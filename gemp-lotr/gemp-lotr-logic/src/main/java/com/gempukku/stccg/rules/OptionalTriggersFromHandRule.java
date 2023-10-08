package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.common.Side;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.OptionalTriggerAction;
import com.gempukku.stccg.effects.EffectResult;
import com.gempukku.stccg.rules.lotronly.LotroGameUtils;

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
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, DefaultGame game, EffectResult effectResult) {
                        List<OptionalTriggerAction> result = new LinkedList<>();
                        final Side side = LotroGameUtils.getSide(game, playerId);
                        for (PhysicalCard responseEvent : Filters.filter(game.getGameState().getHand(playerId), game, side)) {
                            final List<OptionalTriggerAction> actions = responseEvent.getBlueprint().getOptionalInHandAfterTriggers(playerId, game, effectResult, responseEvent);
                            if (actions != null)
                                result.addAll(actions);
                        }
                        return result;
                    }
                });
    }
}
