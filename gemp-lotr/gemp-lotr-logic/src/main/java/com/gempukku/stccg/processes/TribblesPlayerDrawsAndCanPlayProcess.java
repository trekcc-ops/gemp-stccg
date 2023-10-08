package com.gempukku.stccg.processes;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TribblesPlayPermanentAction;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.TribblesGame;

import java.util.LinkedList;
import java.util.List;

public class TribblesPlayerDrawsAndCanPlayProcess extends DefaultGameProcess<TribblesGame> {
    private final String _playerId;
    public TribblesPlayerDrawsAndCanPlayProcess(String playerId) {
        _playerId = playerId;
    }

    @Override
    public void process(final TribblesGame game) {
        if (game.getGameState().getDrawDeck(_playerId).size() == 0) {
            game.getGameState().sendMessage(_playerId + " can't draw a card");
            game.getGameState().setPlayerDecked(_playerId, true);
        } else {
            game.getGameState().playerDrawsCard(_playerId);
            game.getGameState().sendMessage(_playerId + " drew a card");
            List<? extends PhysicalCard> playerHand = game.getGameState().getHand(_playerId);
            PhysicalCard cardDrawn = playerHand.get(playerHand.size() - 1);
            final List<Action> playableActions = new LinkedList<>();
            if (game.checkPlayRequirements(cardDrawn)) {
                TribblesPlayPermanentAction action = new TribblesPlayPermanentAction(cardDrawn, Zone.PLAY_PILE);
                playableActions.add(action);
            }

            if (playableActions.size() == 0 && game.shouldAutoPass(_playerId, game.getGameState().getCurrentPhase())) {
                playerPassed(game);
            } else {
                String userMessage;
                if (playableActions.size() == 0) {
                    userMessage = "The card drawn can't be played. Click 'Pass' to end your turn.";
                } else {
                    userMessage = "Play card that was just drawn or click 'Pass' to end your turn.";
                }
                game.getUserFeedback().sendAwaitingDecision(_playerId,
                        new CardActionSelectionDecision(game, 1, userMessage, playableActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    game.getActionsEnvironment().addActionToStack(action);
                                } else
                                    playerPassed(game);
                            }
                        });
            }
        }
    }

    private void playerPassed(DefaultGame game) {
        game.getGameState().playerPassEffect();
    }

    @Override
    public GameProcess getNextProcess() {
        return new TribblesEndOfTurnGameProcess();
    }
}
