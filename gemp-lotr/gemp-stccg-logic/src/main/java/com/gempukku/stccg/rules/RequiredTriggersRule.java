package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.RequiredTriggerAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class RequiredTriggersRule {
    private final DefaultActionsEnvironment actionsEnvironment;
    private final DefaultGame _game;

    public RequiredTriggersRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
        _game = actionsEnvironment.getGame();
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getRequiredBeforeTriggers(Effect effect) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard card : Filters.filter(
                                actionsEnvironment.getGame().getGameState().getAllCardsInPlay(),
                                actionsEnvironment.getGame(), getActivatableCardsFilter())) {
                            if (!card.hasTextRemoved()) {
                                result.addAll(card.getBeforeTriggerActions(effect, RequiredType.REQUIRED));
                            }
                        }

                        return result;
                    }

                    @Override
                    public List<? extends Action> getRequiredAfterTriggers(EffectResult effectResult) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(_game.getGameState().getAllCardsInPlay(), getActivatableCardsFilter())) {
                            if (!activatableCard.hasTextRemoved()) {
                                final List<? extends Action> actions =
                                        activatableCard.getAfterTriggerActions(effectResult, RequiredType.REQUIRED);
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
