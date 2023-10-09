package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;
import java.util.Set;

public class ST1EDoorwaySeedPhaseProcess implements GameProcess<ST1EGame> {
    private final Set<String> _players;
    private final Set<String> _playersDone;

    public ST1EDoorwaySeedPhaseProcess(Set<String> players, Set<String> playersDone) {
        _players = players;
        _playersDone = playersDone;
    }

    @Override
    public void process(ST1EGame game) {
        for (String _playerId : _players) {
            final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(_playerId);

            if (playableActions.size() == 0) {
                _playersDone.add(_playerId);
            } else {
                game.getUserFeedback().sendAwaitingDecision(_playerId,
                        new CardActionSelectionDecision(1, "Play " + game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    game.getActionsEnvironment().addActionToStack(action);
                                } else
                                    _playersDone.add(_playerId);
                            }
                        });
            }
        }
    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() {
        if (_players.size() != _playersDone.size()) return new ST1EDoorwaySeedPhaseProcess(_players, _playersDone);
        else return new ST1EStartOfMissionPhaseProcess();
    }
}