package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;

import java.util.LinkedList;
import java.util.List;

public class ST1EPhaseActionsRule {
    private final ActionsEnvironment actionsEnvironment;
    private final GameState _gameState;

    public ST1EPhaseActionsRule(ActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
        _gameState = actionsEnvironment.getGame().getGameState();
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId) {
                        final Player player = _gameState.getPlayer(playerId);
                        final Phase currentPhase = _gameState.getCurrentPhase();
                        List<Action> result = new LinkedList<>();
                        if (currentPhase == Phase.CARD_PLAY || currentPhase == Phase.EXECUTE_ORDERS) {
                            Filters.filterYourActive(player).forEach(
                                    card -> result.addAll(card.getPhaseActionsInPlay(player)));
                        }
                        return result;
                    }
                });
    }
}
