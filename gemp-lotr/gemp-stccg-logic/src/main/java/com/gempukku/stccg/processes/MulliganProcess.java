package com.gempukku.stccg.processes;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

import java.util.HashSet;
import java.util.Set;

public class MulliganProcess extends GameProcess {
    private final PlayOrder _playOrder;

    private GameProcess _nextProcess;
    private DefaultGame _game;

    public MulliganProcess(PlayOrder playOrder, DefaultGame game) {
        _playOrder = playOrder;
        _game = game;
    }

    @Override
    public void process() {
        final int handSize = _game.getFormat().getHandSize();

        final String nextPlayer = _playOrder.getNextPlayer();
        if (nextPlayer != null) {
            _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                    new MultipleChoiceAwaitingDecision(1, "Do you wish to mulligan? (Shuffle cards back and draw " + (handSize - 2) + ")", new String[]{"No", "Yes"}) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            if (index == 1) {
                                final GameState gameState = _game.getGameState();
                                gameState.sendMessage(nextPlayer + " mulligans");
                                Set<PhysicalCard> hand = new HashSet<>(gameState.getHand(nextPlayer));
                                gameState.removeCardsFromZone(nextPlayer, hand);
                                for (PhysicalCard card : hand)
                                    gameState.addCardToZone(_game, card, Zone.DRAW_DECK);

                                gameState.shuffleDeck(nextPlayer);
                                for (int i = 0; i < handSize - 2; i++)
                                    gameState.playerDrawsCard(nextPlayer);
                            } else {
                                _game.getGameState().sendMessage(nextPlayer + " decides not to mulligan");
                            }
                        }
                    });
            _nextProcess = new MulliganProcess(_playOrder, _game);
        } else {
            _nextProcess = new BetweenTurnsProcess(_game, null);
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
