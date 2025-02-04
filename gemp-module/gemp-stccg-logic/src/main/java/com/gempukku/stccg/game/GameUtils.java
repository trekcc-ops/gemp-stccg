package com.gempukku.stccg.game;

import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.processes.GameProcess;

import java.util.ArrayList;
import java.util.List;

public class GameUtils {

    public static void performRevert(DefaultGame _game, Player player) {
        String playerId = player.getPlayerId();
        final List<Integer> snapshotIds = new ArrayList<>();
        final List<String> snapshotDescriptions = new ArrayList<>();
        for (GameSnapshot gameSnapshot : _game.getSnapshots()) {
            snapshotIds.add(gameSnapshot.getId());
            snapshotDescriptions.add(gameSnapshot.getDescription());
        }
        int numSnapshots = snapshotDescriptions.size();
        if (numSnapshots == 0) {
            checkPlayerAgain(_game);
            return;
        }
        snapshotIds.add(-1);
        snapshotDescriptions.add("Do not revert");

        // Ask player to choose snapshot to revert back to
        _game.getUserFeedback().sendAwaitingDecision(
                new MultipleChoiceAwaitingDecision(player, "Choose game state to revert prior to",
                        snapshotDescriptions.toArray(new String[0]), snapshotDescriptions.size() - 1, _game) {
                    @Override
                    public void validDecisionMade(int index, String result) throws DecisionResultInvalidException {
                        try {
                            final int snapshotIdChosen = snapshotIds.get(index);
                            if (snapshotIdChosen == -1) {
                                checkPlayerAgain(_game);
                                return;
                            }

                            _game.sendMessage(playerId + " attempts to revert game to a previous state");

                            // Confirm with the other player if it is acceptable to revert to the game state
                            // TODO SNAPSHOT - Needs to work differently if more than 2 players
                            final String opponent;
                            String temp_opponent;
                            temp_opponent = _game.getOpponent(playerId);

                            opponent = temp_opponent;
                            Player opponentPlayer = _game.getPlayer(opponent);

                            StringBuilder snapshotDescMsg = new StringBuilder("</br>");
                            for (int i = 0; i < snapshotDescriptions.size() - 1; ++i) {
                                if (i == index) {
                                    snapshotDescMsg.append("</br>").append(">>> Revert to here <<<");
                                }
                                if ((index - i) < 3) {
                                    snapshotDescMsg.append("</br>").append(snapshotDescriptions.get(i));
                                }
                            }
                            snapshotDescMsg.append("</br>");

                            _game.getUserFeedback().sendAwaitingDecision(
                                    new YesNoDecision(opponentPlayer,
                                            "Do you want to allow game to be reverted to the following game state?" +
                                                    snapshotDescMsg, _game) {
                                        @Override
                                        protected void yes() {
                                            _game.sendMessage(opponent + " allows game to revert to a previous state");
                                            _game.requestRestoreSnapshot(snapshotIdChosen);
                                        }

                                        @Override
                                        protected void no() {
                                            _game.sendMessage(opponent + " denies attempt to revert game to a previous state");
                                            checkPlayerAgain(_game);
                                        }
                                    });
                        } catch(PlayerNotFoundException exp) {
                            throw new DecisionResultInvalidException(exp.getMessage());
                        }
                    }
                });
    }


    /**
     * This method if the same player should be asked again to choose an action or pass.
     */
    private static GameProcess checkPlayerAgain(DefaultGame _game) {
        // TODO SNAPSHOT - The SWCCG code is incompatible with the structure of this process
/*        _playOrder.getNextPlayer();
        return new PlayersPlayPhaseActionsInOrderGameProcess(
                _game.getGameState().getPlayerOrder().getPlayOrder(
                        _playOrder.getNextPlayer(), true), _consecutivePasses, _followingGameProcess); */
        return null;
    }


}