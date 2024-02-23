package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.OptionalTriggerAction;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;

import java.util.LinkedList;
import java.util.List;

public class TribblesOptionalTriggersRule {

    protected final DefaultActionsEnvironment actionsEnvironment;
    private final DefaultGame _game;

    public TribblesOptionalTriggersRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
        _game = actionsEnvironment.getGame();
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getOptionalBeforeTriggers(String playerId, Effect effect) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(_game.getGameState().getAllCardsInPlay(), _game, getActivatableCardsFilter(playerId))) {
                            if (!activatableCard.hasTextRemoved()) {
                                final List<Action> actions = activatableCard.getBeforeTriggerActions(playerId, effect, RequiredType.OPTIONAL);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }
                        return result;
                    }

                    @Override
                    public List<? extends Action> getOptionalAfterTriggerActions(String playerId,
                                                                                                EffectResult effectResult) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(_game.getGameState().getAllCardsInPlay(),
                                _game, getActivatableCardsFilter(playerId))) {
                            if (!activatableCard.hasTextRemoved()) {
                                final List<Action> actions =
                                        activatableCard.getOptionalAfterTriggerActions(playerId, effectResult);
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