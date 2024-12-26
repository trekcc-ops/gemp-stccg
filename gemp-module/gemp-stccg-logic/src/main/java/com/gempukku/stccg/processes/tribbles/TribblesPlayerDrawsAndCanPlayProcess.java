package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.playcard.TribblesPlayCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.LinkedList;
import java.util.List;

public class TribblesPlayerDrawsAndCanPlayProcess extends TribblesGameProcess {
    public TribblesPlayerDrawsAndCanPlayProcess(TribblesGame game) {
        super(game);
    }

    @Override
    public void process(DefaultGame cardGame) {
        String playerId = _game.getCurrentPlayerId();
        if (_game.getGameState().getDrawDeck(playerId).isEmpty()) {
            _game.sendMessage(playerId + " can't draw a card");
            _game.getGameState().setPlayerDecked(playerId, true);
        } else {
            TribblesGame thisGame = _game; // to avoid conflicts when decision calls "_game"
            _game.getGameState().playerDrawsCard(playerId);
            _game.sendMessage(playerId + " drew a card");
            List<? extends PhysicalCard> playerHand = _game.getGameState().getHand(playerId);
            PhysicalCard cardDrawn = playerHand.getLast();
            final List<Action> playableActions = new LinkedList<>();
            if (cardDrawn.canBePlayed(_game)) {
                TribblesPlayCardAction action = new TribblesPlayCardAction((TribblesPhysicalCard) cardDrawn);
                playableActions.add(action);
            }

            if (playableActions.isEmpty() && _game.shouldAutoPass(_game.getGameState().getCurrentPhase())) {
                playerPassed();
            } else {
                String userMessage;
                if (playableActions.isEmpty()) {
                    userMessage = "The card drawn can't be played. Click 'Pass' to end your turn.";
                } else {
                    userMessage = "Play card that was just drawn or click 'Pass' to end your turn.";
                }
                _game.getUserFeedback().sendAwaitingDecision(
                        new CardActionSelectionDecision(_game.getPlayer(playerId), userMessage, playableActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    thisGame.getActionsEnvironment().addActionToStack(action);
                                } else
                                    playerPassed();
                            }
                        });
            }
        }
    }

    private void playerPassed() {
        _game.getGameState().breakChain();
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        return new TribblesEndOfTurnGameProcess(_game);
    }
}