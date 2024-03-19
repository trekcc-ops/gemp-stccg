package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class ActivateResponseAbilitiesRule {
    private final ActionsEnvironment actionsEnvironment;
    private final DefaultGame _game;

    public ActivateResponseAbilitiesRule(ActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
        _game = actionsEnvironment.getGame();
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getOptionalBeforeActions(String playerId, Effect effect) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activatableCard :
                                Filters.filter(_game.getGameState().getAllCardsInPlay(), _game,
                                        getActivatableCardsOwnedByPlayer(playerId))) {
                            if (!activatableCard.hasTextRemoved()) {
                                final List<Action> actions =
                                        activatableCard.getOptionalInPlayActions(effect, TriggerTiming.BEFORE);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }

                    @Override
                    public List<? extends Action> getOptionalAfterActions(String playerId, EffectResult effectResult) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(_game.getGameState().getAllCardsInPlay(),
                                _game, getActivatableCardsOwnedByPlayer(playerId))) {
                            if (!activatableCard.hasTextRemoved()) {
                                final List<Action> actions =
                                        activatableCard.getOptionalInPlayActions(effectResult, TriggerTiming.AFTER);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }
                }
        );
    }

    private Filter getActivatableCardsOwnedByPlayer(String playerId) {
        return Filters.and(Filters.owner(playerId), Filters.active);
    }
}