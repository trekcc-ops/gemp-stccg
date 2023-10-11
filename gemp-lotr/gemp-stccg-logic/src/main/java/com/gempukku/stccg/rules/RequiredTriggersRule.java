package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.RequiredTriggerAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class RequiredTriggersRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public RequiredTriggersRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(DefaultGame game, Effect effect) {
                        List<RequiredTriggerAction> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getAllCardsInPlay(), game, getActivatableCardsFilter())) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activatableCard)) {
                                final List<? extends RequiredTriggerAction> actions = activatableCard.getBlueprint().getRequiredBeforeTriggers(game, effect, activatableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }

                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(DefaultGame game, EffectResult effectResult) {
                        List<RequiredTriggerAction> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getAllCardsInPlay(), game, getActivatableCardsFilter())) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activatableCard)) {
                                final List<? extends RequiredTriggerAction> actions = activatableCard.getBlueprint().getRequiredAfterTriggers(game, effectResult, activatableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }
                }
        );
    }

    private Filter getActivatableCardsFilter() {
        return Filters.active;
    }
}
