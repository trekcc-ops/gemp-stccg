package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ST1EExecuteOrdersRule {
    private final DefaultActionsEnvironment actionsEnvironment;
    private final GameState _gameState;

    public ST1EExecuteOrdersRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
        _gameState = actionsEnvironment.getGameState();
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId) {
                        final Player player = _gameState.getPlayer(playerId);
                        final Phase currentPhase = _gameState.getCurrentPhase();
                        if (currentPhase == Phase.EXECUTE_ORDERS) {
                            List<Action> result = new LinkedList<>();
                            Filters.filterYourActive(player).forEach(
                                    card -> result.addAll(card.getPhaseActionsInPlay(player)));
                            return result;
                        }
                        return null;
                    }
                });
    }
}
