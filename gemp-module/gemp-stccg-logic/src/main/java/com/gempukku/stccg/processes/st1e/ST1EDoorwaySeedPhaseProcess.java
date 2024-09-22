package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.List;
import java.util.Set;

public class ST1EDoorwaySeedPhaseProcess extends ST1EGameProcess {
    private final Set<String> _players;
    private final Set<String> _playersDone;

    public ST1EDoorwaySeedPhaseProcess(Set<String> players, Set<String> playersDone, ST1EGame game) {
        super(game);
        _players = players;
        _playersDone = playersDone;
    }

    @Override
    public void process() {
        for (String _playerId : _players) {
            final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_playerId);

            if (playableActions.isEmpty()) {
                _playersDone.add(_playerId);
            } else {
                _game.getUserFeedback().sendAwaitingDecision(_playerId,
                        new CardActionSelectionDecision(1, "Play " + _game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _game.getActionsEnvironment().addActionToStack(action);
                                } else
                                    _playersDone.add(_playerId);
                            }
                        });
            }
        }
    }

    @Override
    public GameProcess getNextProcess() {
        if (_players.size() != _playersDone.size()) return new ST1EDoorwaySeedPhaseProcess(_players, _playersDone, _game);
        else return new ST1EStartOfMissionPhaseProcess(_game);
    }
}