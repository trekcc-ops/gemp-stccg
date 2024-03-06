package com.gempukku.stccg.processes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

import java.util.HashSet;
import java.util.Set;

public class MulliganProcess extends GameProcess {
    private final ActionOrder _actionOrder;

    private GameProcess _nextProcess;
    private final DefaultGame _game;

    public MulliganProcess(ActionOrder actionOrder, DefaultGame game) {
        _actionOrder = actionOrder;
        _game = game;
    }

    @Override
    public void process() {
        final int handSize = _game.getFormat().getHandSize();

        final String nextPlayer = _actionOrder.getNextPlayer();
        if (nextPlayer != null) {
            _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                    new MultipleChoiceAwaitingDecision("Do you wish to mulligan? (Shuffle cards back and draw " + (handSize - 2) + ")", new String[]{"No", "Yes"}) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            if (index == 1) {
                                final GameState gameState = _game.getGameState();
                                gameState.sendMessage(nextPlayer + " mulligans");
                                Set<PhysicalCard> hand = new HashSet<>(gameState.getHand(nextPlayer));
                                gameState.removeCardsFromZone(nextPlayer, hand);
                                for (PhysicalCard card : hand)
                                    gameState.addCardToZone(card, Zone.DRAW_DECK);

                                gameState.shuffleDeck(nextPlayer);
                                for (int i = 0; i < handSize - 2; i++)
                                    gameState.playerDrawsCard(nextPlayer);
                            } else {
                                _game.sendMessage(nextPlayer + " decides not to mulligan");
                            }
                        }
                    });
            _nextProcess = new MulliganProcess(_actionOrder, _game);
        } else {
            _nextProcess = new BetweenTurnsProcess(_game, null);
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
