package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.List;

public class TribblesPlayerPlaysOrDraws extends GameProcess {
    private final String _playerId;
    private GameProcess _nextProcess;
    private final TribblesGame _game;

    public TribblesPlayerPlaysOrDraws(String playerId, GameProcess followingGameProcess, TribblesGame game) {
        _playerId = playerId;
        _nextProcess = followingGameProcess;
        _game = game;
    }

    @Override
    public void process() {
        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_playerId);

        if (playableActions.isEmpty() && _game.shouldAutoPass(_game.getGameState().getCurrentPhase())) {
            _nextProcess = new TribblesPlayerDrawsAndCanPlayProcess(_playerId, _game);
        } else {
            TribblesGame thisGame = _game; // to avoid conflicts when decision calls "_game"
            String userMessage;
            if (playableActions.isEmpty()) {
                userMessage = "No Tribbles can be played. Click 'Pass' to draw a card.";
            } else {
                userMessage = "Select Tribble to play or click 'Pass' to draw a card.";
            }
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(_game.getPlayer(_playerId), userMessage, playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _nextProcess = new TribblesEndOfTurnGameProcess(thisGame);
                                thisGame.getActionsEnvironment().addActionToStack(action);
                            } else
                                _nextProcess = new TribblesPlayerDrawsAndCanPlayProcess(_playerId, thisGame);
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}