package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.OptionalTriggerAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class OptionalTriggersRule {
    protected final DefaultActionsEnvironment actionsEnvironment;
    private final DefaultGame _game;

    public OptionalTriggersRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
        _game = actionsEnvironment.getGame();
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, Effect effect) {
                        List<OptionalTriggerAction> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(_game.getGameState().getAllCardsInPlay(), _game, getActivatableCardsFilter(playerId))) {
                            if (!activatableCard.hasTextRemoved()) {
                                final List<? extends OptionalTriggerAction> actions = activatableCard.getBlueprint().getOptionalBeforeTriggers(playerId, _game, effect, activatableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }

                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, EffectResult effectResult) {
                        List<OptionalTriggerAction> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(actionsEnvironment.getGame().getGameState().getAllCardsInPlay(), actionsEnvironment.getGame(), getActivatableCardsFilter(playerId))) {
                            if (!activatableCard.hasTextRemoved()) {
                                final List<? extends OptionalTriggerAction> actions =
                                        actionsEnvironment.getGame().getOptionalAfterTriggerActions(playerId, effectResult, activatableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }
                }
        );
    }

    private Filter getActivatableCardsFilter(String playerId) {
        return Filters.and(Filters.owner(playerId), Filters.active);
    }
}