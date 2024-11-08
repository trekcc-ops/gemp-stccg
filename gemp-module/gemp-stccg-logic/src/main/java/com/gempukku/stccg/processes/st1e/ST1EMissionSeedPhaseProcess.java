package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.GameUtils;

import java.util.List;

public class ST1EMissionSeedPhaseProcess extends ST1EGameProcess {
    private int _consecutivePasses;

    public ST1EMissionSeedPhaseProcess(ST1EGame game) {
        this(0, game);
    }

    public ST1EMissionSeedPhaseProcess(int consecutivePasses, ST1EGame game) {
        super(game.getCurrentPlayer(), game);
        _consecutivePasses = consecutivePasses;
    }

    @Override
    public void process() {
        String _currentPlayer = _game.getCurrentPlayerId();

        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_currentPlayer);
        ST1EGameState gameState = _game.getGameState();
        Phase currentPhase = gameState.getCurrentPhase();

        if (playableActions.isEmpty() && _game.shouldAutoPass(currentPhase)) {
            _consecutivePasses++;
        } else {
            DefaultGame thisGame = _game;
            String message = "Play " + currentPhase.getHumanReadable() + " action";
            Player player = _game.getCurrentPlayer();
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(player, message, playableActions, true, true) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            if ("revert".equalsIgnoreCase(result))
                                GameUtils.performRevert(thisGame, _currentPlayer);
                            Action action = getSelectedAction(result);
                            thisGame.getActionsEnvironment().addActionToStack(action);
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess() {
        PlayerOrder playerOrder = _game.getGameState().getPlayerOrder();
        if (_consecutivePasses >= playerOrder.getPlayerCount()) {
            playerOrder.setCurrentPlayer(playerOrder.getFirstPlayer());
            return new ST1EStartOfDilemmaSeedPhaseProcess(_game);
        } else {
            playerOrder.advancePlayer();
            return new ST1EMissionSeedPhaseProcess(_consecutivePasses, _game);
        }
    }

    public int getConsecutivePasses() { return _consecutivePasses; }
}