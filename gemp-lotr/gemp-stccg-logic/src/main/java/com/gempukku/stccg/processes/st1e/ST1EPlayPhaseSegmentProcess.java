package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.game.GameSnapshot;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.ArrayList;
import java.util.List;

public class ST1EPlayPhaseSegmentProcess extends ST1EGameProcess {
    private final String _playerId;
    private ST1EGameProcess _nextProcess;

    ST1EPlayPhaseSegmentProcess(String playerId, ST1EGame game) {
        super(game);
        _playerId = playerId;
    }
    @Override
    public void process() {
        _game.getGameState().sendMessage("DEBUG: Execute orders phase.");
        _game.getGameState().setCurrentPhase(Phase.EXECUTE_ORDERS);
        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_playerId);
        if (!playableActions.isEmpty() || !_game.shouldAutoPass(_playerId, _game.getGameState().getCurrentPhase())) {
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(1, "Play " +
                            _game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            if ("revert".equalsIgnoreCase(result)) {
                                performRevert(_playerId);
                            } else {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    // Take game snapshot before top-level action performed
                                    String snapshotSourceCardInfo = action.getActionSource() != null ?
                                            (": " + action.getActionSource().getCardLink()) : "";
                                    _game.takeSnapshot(_playerId + ": " + action.getText() +
                                            snapshotSourceCardInfo);

                                    _nextProcess = new ST1EPlayPhaseSegmentProcess(_playerId, _game);
                                    _game.getActionsEnvironment().addActionToStack(action);
                                } else
                                    _nextProcess = new ST1EEndOfTurnProcess(_playerId, _game);
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess() { return _nextProcess; }

    private void performRevert(final String playerId) {
        final List<Integer> snapshotIds = new ArrayList<>();
        final List<String> snapshotDescriptions = new ArrayList<>();
        for (GameSnapshot gameSnapshot : _game.getSnapshots()) {
            snapshotIds.add(gameSnapshot.getId());
            snapshotDescriptions.add(gameSnapshot.getDescription());
        }
        int numSnapshots = snapshotDescriptions.size();
        if (numSnapshots == 0) {
            checkPlayerAgain();
            return;
        }
        snapshotIds.add(-1);
        snapshotDescriptions.add("Do not revert");

        // Ask player to choose snapshot to revert back to
        _game.getUserFeedback().sendAwaitingDecision(playerId,
                new MultipleChoiceAwaitingDecision("Choose game state to revert prior to",
                        snapshotDescriptions.toArray(new String[0]), snapshotDescriptions.size() - 1) {
                    @Override
                    public void validDecisionMade(int index, String result) {
                        final int snapshotIdChosen = snapshotIds.get(index);
                        if (snapshotIdChosen == -1) {
                            checkPlayerAgain();
                            return;
                        }

                        _game.getGameState().sendMessage(playerId + " attempts to revert game to a previous state");

                        // Confirm with the other player if it is acceptable to revert to the game state
                        final String opponent = _game.getOpponent(playerId);
                        StringBuilder snapshotDescMsg = new StringBuilder("</br>");
                        for (int i=0; i<snapshotDescriptions.size() - 1; ++i) {
                            if (i == index) {
                                snapshotDescMsg.append("</br>").append(">>> Revert to here <<<");
                            }
                            if ((index - i) < 3) {
                                snapshotDescMsg.append("</br>").append(snapshotDescriptions.get(i));
                            }
                        }
                        snapshotDescMsg.append("</br>");

                        _game.getUserFeedback().sendAwaitingDecision(opponent,
                                new YesNoDecision("Do you want to allow game to be reverted to the following game state?" + snapshotDescMsg) {
                                    @Override
                                    protected void yes() {
                                        _game.getGameState().sendMessage(opponent + " allows game to revert to a previous state");
                                        _game.requestRestoreSnapshot(snapshotIdChosen);
                                    }
                                    @Override
                                    protected void no() {
                                        _game.getGameState().sendMessage(opponent + " denies attempt to revert game to a previous state");
                                        checkPlayerAgain();
                                    }
                                });
                    }
                });
    }

    /**
     * This method if the same player should be asked again to choose an action or pass.
     */
    private void checkPlayerAgain() { // TODO SNAPSHOT - The SWCCG code is incompatible with the structure of this process
        _nextProcess = new ST1EPlayPhaseSegmentProcess(_playerId, _game);
/*        _playOrder.getNextPlayer();
        _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(
                _game.getGameState().getPlayerOrder().getPlayOrder(
                        _playOrder.getNextPlayer(), true), _consecutivePasses, _followingGameProcess); */
    }

}
