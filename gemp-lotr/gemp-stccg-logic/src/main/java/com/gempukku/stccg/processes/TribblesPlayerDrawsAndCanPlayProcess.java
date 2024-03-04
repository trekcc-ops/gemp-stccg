package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.playcard.TribblesPlayCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.TribblesGame;

import java.util.LinkedList;
import java.util.List;

public class TribblesPlayerDrawsAndCanPlayProcess extends GameProcess {
    private final String _playerId;
    private final TribblesGame _game;
    public TribblesPlayerDrawsAndCanPlayProcess(String playerId, TribblesGame game) {
        super();
        _playerId = playerId;
        _game = game;
    }

    @Override
    public void process() {
        if (_game.getGameState().getDrawDeck(_playerId).isEmpty()) {
            _game.getGameState().sendMessage(_playerId + " can't draw a card");
            _game.getGameState().setPlayerDecked(_playerId, true);
        } else {
            _game.getGameState().playerDrawsCard(_playerId);
            _game.getGameState().sendMessage(_playerId + " drew a card");
            List<? extends PhysicalCard> playerHand = _game.getGameState().getHand(_playerId);
            PhysicalCard cardDrawn = playerHand.get(playerHand.size() - 1);
            final List<Action> playableActions = new LinkedList<>();
            if (cardDrawn.canBePlayed()) {
                TribblesPlayCardAction action = new TribblesPlayCardAction((TribblesPhysicalCard) cardDrawn);
                playableActions.add(action);
            }

            if (playableActions.isEmpty() && _game.shouldAutoPass(_playerId, _game.getGameState().getCurrentPhase())) {
                playerPassed(_game);
            } else {
                String userMessage;
                if (playableActions.isEmpty()) {
                    userMessage = "The card drawn can't be played. Click 'Pass' to end your turn.";
                } else {
                    userMessage = "Play card that was just drawn or click 'Pass' to end your turn.";
                }
                _game.getUserFeedback().sendAwaitingDecision(_playerId,
                        new CardActionSelectionDecision(1, userMessage, playableActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _game.getActionsEnvironment().addActionToStack(action);
                                } else
                                    playerPassed(_game);
                            }
                        });
            }
        }
    }

    private void playerPassed(TribblesGame game) {
        game.getGameState().breakChain();
    }

    @Override
    public GameProcess getNextProcess() {
        return new TribblesEndOfTurnGameProcess(_game);
    }
}
