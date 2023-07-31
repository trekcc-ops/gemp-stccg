package com.gempukku.lotro.processes.turn;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.processes.lotronly.skirmish.Skirmish;
import com.gempukku.lotro.game.PlayOrder;
import com.gempukku.lotro.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.processes.GameProcess;

import java.util.List;

public class PlayersPlayPhaseActionsInOrderGameProcess implements GameProcess {
    private final PlayOrder _playOrder;
    private int _consecutivePasses;
    private final GameProcess _followingGameProcess;

    private GameProcess _nextProcess;

    public PlayersPlayPhaseActionsInOrderGameProcess(PlayOrder playOrder, int consecutivePasses, GameProcess followingGameProcess) {
        _playOrder = playOrder;
        _consecutivePasses = consecutivePasses;
        _followingGameProcess = followingGameProcess;
    }

    @Override
    public void process(final DefaultGame game) {
        Skirmish skirmish = game.getGameState().getSkirmish();
        if (game.getGameState().getCurrentPhase() == Phase.SKIRMISH
                && (game.getGameState().getSkirmish().isCancelled()
                || skirmish.getFellowshipCharacter() == null || skirmish.getShadowCharacters().size() == 0)) {
            // If the skirmish is cancelled or one side of the skirmish is missing, no more phase actions can be played
            _nextProcess = _followingGameProcess;
        } else {
            String playerId;
            if (game.getGameState().isConsecutiveAction()) {
                playerId = _playOrder.getLastPlayer();
                game.getGameState().setConsecutiveAction(false);
            } else {
                playerId = _playOrder.getNextPlayer();
            }

            final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(playerId);
            if (playableActions.size() == 0 && game.shouldAutoPass(playerId, game.getGameState().getCurrentPhase())) {
                playerPassed();
            } else {
                game.getUserFeedback().sendAwaitingDecision(playerId,
                        new CardActionSelectionDecision(game, 1, "Play " + game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(_playOrder, 0, _followingGameProcess);
                                    game.getActionsEnvironment().addActionToStack(action);
                                } else {
                                    playerPassed();
                                }
                            }
                        });
            }
        }
    }

    private void playerPassed() {
        _consecutivePasses++;
        if (_consecutivePasses >= _playOrder.getPlayerCount())
            _nextProcess = _followingGameProcess;
        else
            _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(_playOrder, _consecutivePasses, _followingGameProcess);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
