package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.TextUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class RevealTopCardsOfDrawDeckEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private final int _count;
    private final DefaultGame _game;

    public RevealTopCardsOfDrawDeckEffect(ActionContext actionContext, String playerId, int count) {
        super(actionContext);
        _source = actionContext.getSource();
        _game = actionContext.getGame();
        _playerId = playerId;
        _count = count;
    }

    @Override
    public boolean isPlayableInFull() {
        return _game.getGameState().getDrawDeck(_playerId).size() >= _count;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        List<? extends PhysicalCard> deck = _game.getGameState().getDrawDeck(_playerId);
        int count = Math.min(deck.size(), _count);
        LinkedList<PhysicalCard> topCards = new LinkedList<>(deck.subList(0, count));
        if (!topCards.isEmpty()) {
            final ActionOrder playerOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwnerName(), false);

            String nextPlayer;
            while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                        new ArbitraryCardsSelectionDecision(1, _playerId + " revealed card(s) from top of deck", topCards, Collections.emptySet(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });
            }

            _game.sendMessage(_source.getCardLink() + " revealed cards from top of " + _playerId + " deck - " + TextUtils.getConcatenatedCardLinks(topCards));
            for (PhysicalCard topCard : topCards) {
                _game.getActionsEnvironment().emitEffectResult(
                        new RevealCardFromTopOfDeckResult(_playerId, topCard));
            }
        }
        cardsRevealed(topCards);
        return new FullEffectResult(topCards.size() == _count);
    }

    protected abstract void cardsRevealed(List<PhysicalCard> revealedCards);
}