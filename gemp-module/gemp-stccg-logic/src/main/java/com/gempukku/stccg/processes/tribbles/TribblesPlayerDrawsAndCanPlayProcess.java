package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.TribblesPlayCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.LinkedList;
import java.util.List;

public class TribblesPlayerDrawsAndCanPlayProcess extends TribblesGameProcess {
    public TribblesPlayerDrawsAndCanPlayProcess(TribblesGame game) {
        super(game);
    }

    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException {
        Player currentPlayer = _game.getCurrentPlayer();
        String playerId = currentPlayer.getPlayerId();
        if (currentPlayer.getCardsInDrawDeck().isEmpty()) {
            _game.sendMessage(playerId + " can't draw a card");
            _game.getGameState().setPlayerDecked(cardGame, currentPlayer, true);
        } else {
            TribblesGame thisGame = _game; // to avoid conflicts when decision calls "_game"
            _game.getGameState().playerDrawsCard(cardGame, currentPlayer);
            _game.sendMessage(playerId + " drew a card");
            List<? extends PhysicalCard> playerHand = currentPlayer.getCardsInHand();
            PhysicalCard cardDrawn = playerHand.getLast();
            final List<TopLevelSelectableAction> playableActions = new LinkedList<>();
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
                        new CardActionSelectionDecision(currentPlayer, userMessage, playableActions, _game) {
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
        _game.getGameState().breakChain(_game);
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        return new TribblesEndOfTurnGameProcess(_game);
    }
}