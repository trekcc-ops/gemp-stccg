package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

public class ST1EDoorwaySeedPhaseProcess extends ST1EGameProcess {

    public ST1EDoorwaySeedPhaseProcess(ST1EGame game) {
        super(game.getPlayerIds(), game);
        _playersParticipating.addAll(game.getPlayerIds());
    }

    public ST1EDoorwaySeedPhaseProcess(Collection<String> playersSelecting, ST1EGame game) {
        super(playersSelecting, game);
    }

    @Override
    public void process() {
        Iterator<String> playerIterator = _playersParticipating.iterator();
        while (playerIterator.hasNext()) {
            String playerId = playerIterator.next();
            final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(playerId);

            if (playableActions.isEmpty()) {
                playerIterator.remove();
            } else {
                String message = "Play " + _game.getGameState().getCurrentPhase().getHumanReadable() +  " action or Pass";
                _game.getUserFeedback().sendAwaitingDecision(
                        new CardActionSelectionDecision(_game.getPlayer(playerId), message, playableActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _game.getActionsEnvironment().addActionToStack(action);
                                } else
                                    playerIterator.remove();
                            }
                        });
            }
        }
    }

    @Override
    public GameProcess getNextProcess() {
        if (_playersParticipating.isEmpty()) return new ST1EStartOfMissionPhaseProcess(_game);
        else return new ST1EDoorwaySeedPhaseProcess(_playersParticipating, _game);
    }
}