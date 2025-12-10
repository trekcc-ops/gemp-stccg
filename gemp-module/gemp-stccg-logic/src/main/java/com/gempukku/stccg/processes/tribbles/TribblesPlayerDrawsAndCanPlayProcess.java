package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.draw.DrawSingleCardAction;
import com.gempukku.stccg.actions.playcard.TribblesPlayCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.processes.GameProcess;

import java.util.LinkedList;
import java.util.List;

public class TribblesPlayerDrawsAndCanPlayProcess extends TribblesGameProcess {
    public TribblesPlayerDrawsAndCanPlayProcess(TribblesGame game) {
        super(game);
    }

    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException {
        Player currentPlayer = cardGame.getCurrentPlayer();
        if (currentPlayer.getCardsInDrawDeck().isEmpty()) {
            _game.getGameState().setPlayerDecked(currentPlayer, true);
        } else {
            TribblesGame thisGame = _game; // to avoid conflicts when decision calls "_game"
            DrawSingleCardAction drawAction = new DrawSingleCardAction(cardGame, currentPlayer);
            drawAction.processEffect(cardGame);
            cardGame.getActionsEnvironment().logCompletedActionNotInStack(drawAction);
            cardGame.sendActionResultToClient();
            List<? extends PhysicalCard> playerHand = currentPlayer.getCardsInHand();
            PhysicalCard cardDrawn = playerHand.getLast();
            final List<TopLevelSelectableAction> playableActions = new LinkedList<>();
            if (cardDrawn.canBePlayed(cardGame)) {
                TribblesPlayCardAction action = new TribblesPlayCardAction(cardGame, (TribblesPhysicalCard) cardDrawn);
                playableActions.add(action);
            }

            if (playableActions.isEmpty() && cardGame.shouldAutoPass(cardGame.getGameState().getCurrentPhase())) {
                playerPassed();
            } else {
                cardGame.sendAwaitingDecision(
                        new ActionSelectionDecision(currentPlayer, DecisionContext.SELECT_TRIBBLES_ACTION, playableActions, _game, false) {
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