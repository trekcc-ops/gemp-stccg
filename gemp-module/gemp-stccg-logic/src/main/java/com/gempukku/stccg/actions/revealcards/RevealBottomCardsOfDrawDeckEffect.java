package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.TextUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class RevealBottomCardsOfDrawDeckEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private final int _count;

    public RevealBottomCardsOfDrawDeckEffect(ActionContext actionContext, String playerId, int count) {
        super(actionContext.getGame(), playerId);
        _source = actionContext.getSource();
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
        LinkedList<PhysicalCard> bottomCards = new LinkedList<>(deck.subList(deck.size() - count, deck.size()));

        if (!bottomCards.isEmpty()) {
            final ActionOrder playerOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwnerName(), false);

            String nextPlayer;
            while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                        new ArbitraryCardsSelectionDecision(1, _playerId+" revealed card(s) from bottom of deck", bottomCards, Collections.emptySet(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });
            }

            _game.sendMessage(_source.getCardLink() + " revealed cards from bottom of " + _playerId + " deck - " + TextUtils.getConcatenatedCardLinks(bottomCards));
        }
        cardsRevealed(bottomCards);
        return new FullEffectResult(bottomCards.size() == _count);
    }

    protected abstract void cardsRevealed(List<PhysicalCard> cards);
}
